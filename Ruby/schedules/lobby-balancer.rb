require 'rufus-scheduler'

schedule = Rufus::Scheduler.singleton

puts 'Lobby Balancer Loaded'

# Task that will dynamically start more lobbies if the current ones are full.
schedule.every('1m') do
  lobbies = Server.where('name LIKE (?)', 'Lobby%')

  online_lobbies = []
  offline_lobbies = []
  online = 0.0
  total = 0.0

  next if lobbies.blank?

  lobbies.each do |s|
    s.online? ? online_lobbies << s : offline_lobbies << s
    online += s.player_count
    total += s.max_players
  end

  ratio = online / total

  next if ratio == 0

  # Server could be restarting
  offline_lobbies.delete_if { |s| s.seconds_since_online != nil && s.seconds_since_online < 120 }

  # If the ratio of lobby players to available slots is at or more 80%.
  # We also keep 2 lobbies online at all times.
  if ratio >= 0.8 || online_lobbies.size < 2
    s = offline_lobbies.first
    next if s.nil?
    $redis.publish('server-actions', {server: s.name, action: 'start'}.to_json)
    $redis.setex("ignored-server.#{s.id}", 15.minutes, true) # Keep alive for 15 mins
  end
end

# Keep at least 2 lobbies online, kill others with no players
schedule.every('5m') do
  lobbies = Server.where('name LIKE (?)', 'Lobby%')

  next if lobbies.blank?

  online_lobbies = []

  lobbies.each do |s|
    online_lobbies << s if s.online?
  end

  online_lobbies.last(online_lobbies.size - 2).each do |s|
    ignored = $redis.get("ignored-server.#{s.id}")
    next if ignored
    $redis.publish('server-actions', {server: s.name, action: 'stop'}.to_json) if s.player_count == 0
  end if online_lobbies.size > 2
end
