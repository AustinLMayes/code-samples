class Team < ActiveRecord::Base
  include Permissions::ScopedEditable
  include Permissions::ScopedExecutable

  validates_uniqueness_of :title, :case_sensitive => false
  validates_uniqueness_of :tag, :case_sensitive => false
  validates_size_of :title, :within => 2..16
  validates_size_of :tagline, :within => 1..32
  validates_size_of :tag, :within => 3..4
  validates :tag, :format => {:with => /\A[0-9a-zA-Z]*\Z/, :message => 'contains invalid characters.'}
  validates :title, :format => {:with => /\A[0-9a-zA-Z-_ ]*\Z/, :message => 'contains invalid characters.'}

  validate :check_title_tag

  has_many :reserved_slots, :dependent => :destroy

  include GraphQL::QLModel

  graphql_finders(:title, :tag)

  graphql_type description: 'A group of people which can participate in tournaments and reserve servers.',
               fields: {
                   title: 'The title of the team.',
                   tag: 'The tag of the team.',
                   tagline: 'The tagline of the team.',
                   about: 'The about section on the team page in raw HTML.'
               }, create: true, update: true

  # Permissions start

  def self.permission_definition
    {
        :id_based => false,
        :global_options => {
            :text => 'Tournament Teams',
            options: [:true, :false],
        },
        :permissions_sets => [{
                                  :actions => [:update, :delete, :set_role, :invite, :kick, :reserve, :register],
                                  :edit => [:title, :tag, :tagline, :about],
                                  :options => [:all, :own, :false], # Own = leader of (normal members can only edit if :all).
                              },
                              {
                                  :actions => {
                                      text: 'Global Actions',
                                      desc: 'Users (with and without teams).',
                                      translate_text: '{1}',
                                      translate_desc: 'Allow users to {1} teams.',
                                      :global => [:create, :join, :view_reservation_count_on]
                                  }
                              }
        ]
    }
  end

  def self.perm_fields
    self.permission_definition[:permissions_sets][0][:edit]
  end

  # Permissions end

  def check_title_tag
    errors.add(:tag, 'tag reserved') if tag == 'GLBL'
    errors.add(:title, 'title reserved') if title == 'Miscellany'
  end

  # Team used for tournaments with open registration. Never has members.
  def self.global_team
    t = Team.find_by_title('Miscellany')
    if t.nil?
      t = Team.new(
          title: 'Miscellany',
          tag: 'GLBL',
          tagline: 'Everyone, yet no one at the same time.',
          about: 'A place for friends and foes alike.'
      )
      t.save(validate: false)
    end
    t
  end

  def global?
    tag == 'GLBL'
  end

  # Get all reservations this team has made for scrimmage servers.
  def reservations
    ReservedSlot.where(team: self).count
  end

  # Get an HTML formatted link to the team.
  def link
    "<a href='#{path}'>#{title}</a>"
  end

  # Get the team's slug.
  def slug
    title.downcase.gsub(' ', '-')
  end

  # Get an application-wide link to the team.
  def path
    "/teams/#{id}"
  end

  # Get a set of the IDs of the members of this team.
  def user_ids
    users.select(:id).pluck(:id)
  end

  # Get the role of a user on the team.
  def get_role(user)
    list = members.where(:user_id => user.id)
    if list.size == 0
      nil
    else
      list.first.role.downcase
    end
  end

  # Check if a user is invited to the team.
  def is_invited(user)
    invites.each do |invite|
      if invite.user_id == user.id
        return true
      end
    end
    false
  end

  # Get all users that are members of the team.
  def users
    if members.size > 0
      User.where('id IN (?)', members.pluck(:user_id))
    else
      return User.where('1 = 0')
    end
  end

  # Get all accepted members of the team.
  def members
    TeamMember.where(:team_id => id).where(:accepted => 1).order('role ASC,accepted_at ASC')
  end

  # Get all invited members of the team.
  def invites
    TeamMember.where(:team_id => id).where(:accepted => 0).order('role ASC,accepted_at ASC')
  end

  # Get the total of time online for all members of the team.
  def time_online
    seconds = 0
    users.select(:id, :time_online).each do |user|
      seconds += user.time_online + Session.select(:duration).where(:user_id => user.id).sum(:duration)
    end
    seconds
  end

  # Abilities

  def is_leader?(user)
    get_role(user) == 'leader'
  end

  def owns?(user)
    is_leader?(user)
  end

  def can_execute_global?(user, *action)
    action.flatten!
    return user.has_permission?(:teams, :global, action, true)
  end
end
