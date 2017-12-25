require 'rufus-scheduler'

schedule = Rufus::Scheduler.singleton

puts 'Server Group Balancer Loaded'

# Task that will dynamically start more servers in the same group if the current ones are full.
schedule.every('1m') do
  groups = ServerGroup.all

  groups.each do |group|
    next unless group.should_be_up?
    servers = group.servers
    next if servers.blank?
    online_servers = []
    offline_servers = []
    online = 0.0
    total = 0.0

    servers.each do |s|
      s.online? ? online_servers << s : offline_servers << s
      online += s.player_count
      total += s.max_players
    end

    ratio = online / total

    # Server could be restarting
    offline_servers.delete_if { |s| s.seconds_since_online != nil && s.seconds_since_online < 120 }

    next if ratio == 0
    # If the ratio of playing players to available slots is at or more than 80%.
    # We also keep 1 server online at all times.
    if ratio >= 0.8 || online_servers.size < 1
      s = offline_servers.first
      next if s.nil?
      $redis.publish('server-actions', {server: s.name, action: 'start'}.to_json)
      $redis.setex("ignored-server.#{s.id}", 15.minutes, true) # Keep alive for 15 mins
    end
  end
end

schedule.every('7m') do
  groups = ServerGroup.all
  groups.each do |group|
    servers = group.servers
    next if servers.blank?

    online_servers = []
    servers.each do |s|
      online_servers << s if s.online?
    end
    next if online_servers.empty?

    if group.should_be_up?
      AnnounceUtils.join_server(
          online_servers.sample, '§6' + group.name +
          ' §a is up and available to join NOW!' +
          ' §bThis server is up temporary, so join while you can!'
      )
    end
  end
end

# Keep at least 1 server online, kill others with no players
schedule.every('5m') do
  groups = ServerGroup.all

  groups.each do |group|
    servers = group.servers
    next if servers.blank?

    unless group.should_be_up?
      $redis.publish('restart', {group: group.id}.to_json)
      next
    end

    online_servers = []
    servers.each do |s|
      online_servers << s if s.online?
    end

    online_servers.last(online_servers.size - 1).each do |s|
      ignored = $redis.get("ignored-server.#{s.id}")
      next if ignored
      $redis.publish('server-actions', {server: s.name, action: 'stop'}.to_json) if s.real_player_count == 0
    end if online_servers.size > 1
  end
end
