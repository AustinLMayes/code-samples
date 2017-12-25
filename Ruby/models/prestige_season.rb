class PrestigeSeason < ActiveRecord::Base
  has_many :prestige_levels, dependent: :destroy, foreign_key: 'season_id'
  has_many :experience_transactions, dependent: :destroy, foreign_key: 'season_id'

  include GraphQL::QLModel

  graphql_finders(:name)

  graphql_type description: 'A period of time in which XP transactions and leveling are grouped together.',
               fields: {
                   name: 'Name of the season.',
                   multiplier: 'Value which all XP transactions inside of this season should be multiplied by.',
                   start_at: 'When the season starts.',
                   end_at: 'When the season ends.',
               }

  # Check if the season is currently active.
  def ongoing?
    Time.now > start_at && Time.now < end_at
  end

  # Get the current season, if there is one.
  def self.current_season
    PrestigeSeason.where('start_at < (?) AND end_at > (?)', Time.now, Time.now).first
  end

  def self.xp(user)
    return 0 if user.nil? || user.is_a?(DummyUser)
    current = PrestigeSeason.current_season
    return 0 if current.nil?
    ExperienceTransaction.where(prestige_season: current, user: user).sum(:amount)
  end
end
