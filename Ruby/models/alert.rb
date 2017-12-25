class Alert < ActiveRecord::Base
  acts_as_readable :on => :created_at
  belongs_to :user

  include GraphQL::QLModel

  require 'graphql/custom_field'

  graphql_finders(:user_id, :name)

  graphql_type description: 'Link between a user and a specific achievement.',
               fields: {
                   id: 'The ID of the alert',
                   user_id: 'ID of the user that this alert is for.',
                   name: 'Unique name of the Alert.',
                   message: 'The message which is displayed to the user.',
                   url: 'URL that this alert will direct to when clicked.',
                   created_at: 'When the alert was created.'
               },
               custom_fields: [
                   GraphQL::CustomField.new(:seen, 'If the alert has been read.', GraphQL::BOOLEAN_TYPE, ->(obj, args, ctx) do
                     !obj.unread?(obj.user)
                   end
                   )
               ]

  graphql_query operation: 'allAfter',
                description: 'Find all created after the given date.',
                multi: true,
                arguments: [:created_at],
                resolver: ->(_, args, _) {
                  Alert.where('created_at > (?)', args[:created_at])
                }

  # Send an alert to a user
  # user -> User to alert
  # name -> Name of the alert
  # message -> The message to send to users
  # link -> What the alert should link to on click
  def self.alert(user, name, message, link)
    existing = Alert.where(:user => user).where(:name => name)
    if existing.empty?
      Alert.create(user: user, name: name, message: message, url: link)
    end
  end

  # Delete an alert from a user based on name
  def self.dismiss(user, name)
    Alert.where(:user_id => user.id, :name => name).destroy_all
  end
end
