class Server < ActiveRecord::Base
  belongs_to :server_category, optional: true
  belongs_to :server_group, optional: true

  include Permissions::Editable
  include Permissions::Executable

  validates_uniqueness_of :name

  include GraphQL::QLModel

  graphql_finders(:name, :host, :server_group_id, :server_category_id)

  graphql_type description: 'A server which users can join.',
               fields: {
                   name: 'Name of the server.',
                   host: 'Host of the box which this server is hosted on.',
                   port: 'Port of the server.',
                   permissible: 'If the server can only be joined by users with the correct permission.',
                   server_group_id: 'ID of the server group this server belongs to.',
                   server_category_id: 'ID of the server category this server belongs to.',
               }, create: true, update: true

  # Get the inferred permission needed to join the server if it is permissible.
  def permission
    permissible ? "hook.server.#{name.downcase}" : ''
  end

  # Get a JSON-formatted version of this server's status from redis.
  def status
    result = $redis.hget('servers', id)
    result = '{}' if result.nil?
    JSON.parse(result)
  end

  # Check if this server is online.
  def online?
    status && status['online']
  end

  # Get the seconds since the server's status was last updated.
  def seconds_since_online
    if status && status['timestamp']
      return Time.now - Time.at(status['timestamp'] / 1000)
    end
    return nil
  end

  # Get all users on this server.
  def players
    if status && status['players']
      status['players'].map { |id| User.find(id) }
    else
      []
    end
  end

  # Get the message inside of the redis status.
  def message
    if status && status.include?('message')
      status['message']
    else
      nil
    end
  end

  # Get the amount of players playing in the game.
  def player_count
    if status && status.include?('player-count')
      status['player-count']
    else
      0
    end
  end

  # Get the total number of players online.
  def real_player_count
    if status && status['players']
      status['players'].size
    else
      0
    end
  end

  # Get the max players allowed on this server.
  def max_players
    if status && status.include?('max-players')
      status['max-players']
    else
      0
    end
  end

  # Permissions start

  def self.permission_definition
    {
        :id_based => false,
        :global_options => {
            options: [:true, :false],
        },
        :permissions_sets => [{
                                  :edit => [:name, :host, :port, :permissible, :auto_deploy, :path, :screen_session],
                                  :actions => [:create, :update, :destroy, :execute_command, :view_console]
                              }]
    }
  end

  def self.perm_fields
    self.permission_definition[:permissions_sets][0][:edit]
  end

  # Permissions end
end
