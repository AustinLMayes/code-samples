## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

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
