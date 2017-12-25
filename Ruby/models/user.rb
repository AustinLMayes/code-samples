require 'graphql/ql_model'

class User < ActiveRecord::Base
  include CachableModel

  acts_as_reader

  has_one :user_detail, :dependent => :destroy
  has_many :memberships, :foreign_key => 'member_id', :dependent => :destroy
  has_many :ranks, :through => :memberships, :source => :rank
  has_many :subscriptions, :dependent => :destroy
  has_many :revisions, :dependent => :destroy
  has_many :discussions, :dependent => :destroy
  has_many :leaderboard_entries, :dependent => :destroy
  has_many :experience_leaderboard_entries, :dependent => :destroy
  has_many :alerts, :dependent => :destroy
  has_many :sessions, :dependent => :destroy
  has_many :appeals, :dependent => :destroy
  has_many :prestige_levels, :dependent => :destroy
  has_many :experience_transactions, :dependent => :destroy

  include GraphQL::QLModel

  graphql_finders(:username, :uuid, :locale, :mc_version, :discord_id)

  graphql_type description: 'A person who has logged into the server at least once.',
               fields: {
                   username: 'The username of the user when they last logged in.',
                   uuid: 'The Minecraft UUID of the user.',
                   locale: 'The Minecrft locale the user had set when they last logged in.',
                   tracker: 'Tracking information for the user.',
                   mc_version: 'The last version of Minecraft the user logged in with.',
                   verify_key: 'The verification key assigned to the user during registration.',
                   verify_key_success: 'If the user successfullly verified their identitity with the server during registration.',
                   discord_id: 'The discord client ID of the user.'
               }, create: true

  # Helper method to find a user by username.
  def self.find_by_name(query)
    find_by_username(query)
  end

  # Get all of the user's forum subscriptions
  def subscriptions
    user_s = Subscription.select(:discussion_id).where(:user_id => id)
    user_s.map { |s| s.discussion_id }
  end

  # Authenticate the user with the supplied passwor.
  def authenticate(password_input)
    if self.password_secure.presence
      BCrypt::Password.new(self.password_secure) == password_input
    else
      salt = $avicus['salt']
      self.password == Digest::MD5.hexdigest(salt + password_input)
    end
  end

  # Chek if the user has a password aka is registered on the website.
  def is_registered?
    return self.password_secure.presence || self.password.presence
  end

  # Get all friendships that are accepted for this user.
  def friends
    User.where(:id => Friend.select(:friend_id).where(:user_id => id, :accepted => 1).map(&:friend_id))
  end

  # Get all friendships that are requested for this user.
  def requests
    User.where(:id => Friend.select(:friend_id).where(:user_id => id, :accepted => 0).map(&:friend_id))
  end

  # Check if a user is subscribed to a post.
  def is_subscribed?(post)
    subscriptions.where(:post => post).count > 0
  end

  # Get a user's details.
  def details
    user = UserDetail.find_by_id(id)
    if user == nil
      user = UserDetail.new(:id => id)
      user.save
    end
    user
  end

  # Send an alert to this user.
  def alert(name, message, url)
    Alert.alert(self, name, message, url)
  end

  # Dismiss an alert from this user.
  def dismiss_alert(name)
    Alert.dismiss(self, name)
  end

  # Get all unread alerts for this user.
  def unread_alerts
    Alert.where(:user_id => id).unread_by(self).order('created_at DESC')
  end

  # Get this user's team.
  def team
    list = TeamMember.where(:user_id => id).where(:accepted => 1)
    list.each do |member|
      return member.team
    end
    nil
  end

  # Get this user's last IP.
  def last_ip
    sessions.size > 0 ? sessions.last.ip : ip
  end

  # Helper method to get the user's username.
  def name
    username
  end

  # Get an application-wide link to the user.
  def path
    "/#{name}"
  end

  # Get the user's avatar.
  def avatar(size=128)
    # parts = ('a'..'z').to_a.each_slice(6.5).to_a
    # letter = name[0..0].downcase

    # if parts[0].include?(letter)
    #   "https://minotar.net/helm/#{name}/#{size}.png"
    # elsif parts[1].include?(letter)
    #   "https://minotar.net/helm/#{name}/#{size}.png"
    # elsif parts[2].include?(letter)
    #   "https://crafatar.com/avatars/#{uuid}?helm&size=#{size}"
    # else
    #   "https://crafatar.com/avatars/#{uuid}?helm&size=#{size}"
    # end
    mc = "https://crafatar.com/avatars/#{uuid}?helm&size=#{size}"
    if cached(:details).avatar == 'Gravatar' && cached(:details).email_status == 1
      md5 = Digest::MD5.hexdigest(details.cached(:email))
      URI.encode "https://www.gravatar.com/avatar/#{md5}?s=#{size}&default=mm"
    else
      mc
    end
  end

  # Check if the user has a rank which is staff.
  def is_staff
    Rank.select(:id, :is_staff).where(:is_staff => true).each do |rank|
      return true if rank.members.include?(self)
    end
    false
  end

  # Get the highest rank a user has.
  def highest_priority_rank
    highest = cached(:ranks).sort_by { |rank| rank.priority }.reverse.first
    highest.nil? ? Rank.default_rank : highest
  end

  # Get all punishments issued to the user.
  def punishments
    Punishment.where(:user_id => id)
  end

  ICONS = {}
  # https://www.w3schools.com/colors/color_tryit.asp
  COLORS = %w(AliceBlue AntiqueWhite Aqua Aquamarine Azure Beige Bisque Black BlanchedAlmond Blue BlueViolet Brown BurlyWood CadetBlue Chartreuse Chocolate Coral CornflowerBlue Cornsilk Crimson Cyan DarkBlue DarkCyan DarkGoldenRod DarkGray DarkGrey DarkGreen DarkKhaki DarkMagenta DarkOliveGreen DarkOrange DarkOrchid DarkRed DarkSalmon DarkSeaGreen DarkSlateBlue DarkSlateGray DarkSlateGrey DarkTurquoise DarkViolet DeepPink DeepSkyBlue DimGray DimGrey DodgerBlue FireBrick FloralWhite ForestGreen Fuchsia Gainsboro GhostWhite Gold GoldenRod Gray Grey Green GreenYellow HoneyDew HotPink IndianRed Indigo Ivory Khaki Lavender LavenderBlush LawnGreen LemonChiffon LightBlue LightCoral LightCyan LightGoldenRodYellow LightGray LightGrey LightGreen LightPink LightSalmon LightSeaGreen LightSkyBlue LightSlateGray LightSlateGrey LightSteelBlue LightYellow Lime LimeGreen Linen Magenta Maroon MediumAquaMarine MediumBlue MediumOrchid MediumPurple MediumSeaGreen MediumSlateBlue MediumSpringGreen MediumTurquoise MediumVioletRed MidnightBlue MintCream MistyRose Moccasin NavajoWhite Navy OldLace Olive OliveDrab Orange OrangeRed Orchid PaleGoldenRod PaleGreen PaleTurquoise PaleVioletRed PapayaWhip PeachPuff Peru Pink Plum PowderBlue Purple RebeccaPurple Red RosyBrown RoyalBlue SaddleBrown Salmon SandyBrown SeaGreen SeaShell Sienna Silver SkyBlue SlateBlue SlateGray SlateGrey Snow SpringGreen SteelBlue Tan Teal Thistle Tomato Turquoise Violet Wheat White WhiteSmoke Yellow YellowGreen)

  def custom_badge
    return '' if details.custom_badge_icon.nil? || details.custom_badge_icon.empty?
    color = (details.custom_badge_color.nil? ||details.custom_badge_color.empty?) ? 'black' : details.custom_badge_color
    "<i class='fa #{details.custom_badge_icon}' rel='tooltip' style='color: #{color}' data-original-title='This is a custom badge! Purchase a donor rank to get one!'></i>"
  end

  def to_param
    name
  end

  # Check if a user has a permission
  def has_permission?(*args)
    args.flatten!
    $avicus['override-perms'] || cached(:has_permission_uncached, *args)
  end

  # Check if a user has a permission regardless of the value in the cache.
  def has_permission_uncached(*args)
    args.flatten!
    check = args.last
    last = false

    puts args

    user_ranks = ranks.to_a.flatten
    user_ranks.unshift(Rank.default_rank)

    user_ranks.each do |rank|
      last = rank.has_permission?(args)

      if last && (check != false || check != 'false')
        return true
      end
    end

    last
  end

  # Get a permission's value
  def get_permission(*args)
    args.flatten!
    $avicus['override-perms'] || cached(:get_permission_uncached, *args)
  end

  # Get a permission's value
  def get_permission_uncached(*args)
    args.flatten!
    last = false

    puts args

    user_ranks = ranks.to_a.flatten
    user_ranks.unshift(Rank.default_rank)

    user_ranks.reverse!

    user_ranks.each do |rank|
      last = rank.get_permission_inherit(args)

      if last != nil && !last.to_s.empty? && last.to_s != 'flow'
        return last
      end
    end

    last
  end
end
