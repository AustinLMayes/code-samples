require_relative "../lib/worker"

class BoostNotifer < Worker

  def initialize(discord)
    super("BoostNotifer", 5.minutes)
    @guild = discord.guild(:main_guild)[:object]
    @where = discord.guild(:staff_guild)[:object].channels.detect { |chan| chan.id == $config.get("discord", "boost_channel").to_i }
    @boosters = []
  end

  def execute(itteration)
    role = @guild.roles.detect{|r| r.name == "Nitro Booster"}
    return if role.nil?
    actual = []
    @guild.members.each do |m|
      actual << {name: m.display_name, id: m.id} if m.role?(role)
    end
    no_longer = @boosters.select{|entry| !actual.include?(entry)}
    new = actual - @boosters
    @boosters = @boosters - no_longer + new
    new.each do |e|
      @where.send_embed do |embed|
        embed.title = "New Boost"
        embed.description = e[:name]
        embed.color = '22d100'
      end
    end
    no_longer.each do |e|
      @where.send_embed do |embed|
        embed.title = "Boost Revoked"
        embed.description = e[:name]
        embed.color = 'b50900'
      end
    end
  end
end
