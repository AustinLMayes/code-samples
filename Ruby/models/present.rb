class Present < ActiveRecord::Base

  has_many :present_finder

  include Permissions::Editable
  include Permissions::Executable

  # Permissions start

  def self.permission_definition
    {
        :id_based => false,
        :global_options => {
            options: [:true, :false],
        },
        :permissions_sets => [{
                                  :edit => [:slug, :human_name, :human_location, :family],
                                  :actions => [:create, :update, :destroy, :give, :revoke]
                              }]
    }
  end

  def self.perm_fields
    self.permission_definition[:permissions_sets][0][:edit]
  end

  # Permissions end

  include GraphQL::QLModel

  graphql_finders(:slug, :human_name, :human_location)

  graphql_type description: 'Something that can be found in the lobby',
               fields: {
                   slug: 'Slug of the present used in plugins to protect against name changes.',
                   human_name: 'Name of the present used in the UI.',
                   human_location: 'Description of the location of the present used in the UI.',
                   family: 'Family of the present e.g "Christmas 2017"'
               }

end
