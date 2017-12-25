class ServerCategory < ActiveRecord::Base
  has_many :servers

  include Permissions::Editable
  include Permissions::Executable

  serialize :communication_options, JSON
  serialize :tracking_options, JSON
  serialize :infraction_options, JSON

  include GraphQL::QLModel

  graphql_finders(:name)

  graphql_type description: 'A group of servers which share the same general category.',
               fields: {
                   name: 'Name of the category.',
                   communication_options: 'Options related to how servers in this category should communicate both outward and inside the category.',
                   tracking_options: 'Options related to the tracking of stats.',
                   infraction_options: 'Options related to the tracking/enforcement of ',
               }

  # Permissions start

  def self.permission_definition
    {
        :id_based => false,
        :global_options => {
            options: [:true, :false],
        },
        :permissions_sets => [{
                                  :edit => [:name, :communication_options, :tracking_options, :infraction_options],
                                  :actions => [:create, :update, :destroy, :add_member, :remove_member]
                              }]
    }
  end

  def self.perm_fields
    self.permission_definition[:permissions_sets][0][:edit]
  end

  # Permissions end

  # Check if this category has the specified option.
  def has_option?(ident, option)
    if self.respond_to?(ident) && !self.send(ident).nil?
      return self.send(ident)[option] == 'true'
    end
    false
  end

  # Check if this category has the specified option inside of another option.
  def has_option_sub?(ident, sub, option)
    if self.respond_to?(ident) && !self.send(ident).nil?
      begin
        return self.send(ident)[sub][option] == 'true'
      rescue
        return false
      end
    end
    false
  end

  # Check if this category has the specified option inside of another option.
  def get_option(ident, sub)
    if self.respond_to?(ident) && !self.send(ident).nil?
      begin
        return self.send(ident)[sub]
      rescue
        return nil
      end
    end
    nil
  end
end
