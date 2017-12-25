class Membership < ActiveRecord::Base
  default_scope { order('memberships.id') }

  include GraphQL::QLModel

  graphql_finders(:rank_id, :member_id, :is_purchased)

  graphql_type description: "Represents a user's participation in a rank.",
               fields: {
                   member_id: 'ID of the user who this membership is for.',
                   rank_id: 'ID of the rank which the user belongs to.',
                   expires_at: 'When this membership expires.',
                   is_purchased: 'If this rank was purchased from the store.',
               },
               create: true, update: true

  belongs_to :rank
  belongs_to :member, :class_name => 'User'
  after_commit :flush

  def is_timed?
    !self.expires_at.nil?
  end

  # Flush the cache of the member
  def flush
    member.flush
  end
end
