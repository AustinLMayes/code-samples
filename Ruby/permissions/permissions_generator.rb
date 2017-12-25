# Generates permissions from classes/hashes
class PermissionsGenerator
  require 'permissions/permission_utils.rb'

  def initialize(*args)
    if args.size == 1
      create_clazz(args[0])
    else
      create_hash(*args)
    end
  end

  attr_reader :clazz, :model_name, :id_based, :global_options, :permissions_sets, :parent_group, :permissions

  # Generate permission groups and populate data from a class.
  def create_clazz(clazz)
    @clazz = clazz
    definition = get_class_attribute(:permission_definition)

    if definition.nil?
      raise('Class does not have a permission definition method.')
    end

    # Main variables
    @model_name = clazz.name.underscore.tr('/', '::')
    @id_based = definition[:id_based]
    @id_based = false if @id_based.nil?

    # Global values
    @global_options = definition[:global_options]

    # Permission sets
    @permissions_sets = definition[:permissions_sets]

    def_option = @global_options[:def_option]
    def_option = :flow if def_option.nil?

    @parent_group = write_permission_group(@model_name.downcase, @global_options[:text], @global_options[:desc], @global_options[:options], def_option)

    @permissions = handle_permissions_set(@parent_group, @permissions_sets)
  end

  # Generate permission groups and populate data from supplied data.
  def create_hash(name, id_based, global_options, parent_group, permissions_sets)
    # Main variables
    @model_name = name
    @id_based = id_based

    # Global values
    @global_options = global_options

    # Permission sets
    @permissions_sets = permissions_sets

    @parent_group = parent_group
    @parent_group = write_permission_group(@model_name, @global_options[:text], @global_options[:desc], @global_options[:options], @global_options[:def_option]) if parent_group.nil?

    @permissions = handle_permissions_set(@parent_group, @permissions_sets) unless @permissions_sets.nil? || @permissions_sets == []
  end

=begin
  Reference Hash
  {
    :name => "A Nane",
    :id_based => false,
    :global_options => {
      options: [:true, :false],
      text: "",
      desc: ""
    },
    :permissions_sets  => [{
        :actions => [:edit, :delete],
        :fields => [:name],
        :edit => [:age],
        :view => [:email]
      }]
  }
=end

  # Helper method to get an attribute from a ruby class.
  def get_class_attribute(method, *args)
    if args.length > 0
      @clazz.send(method, args)
    else
      @clazz.send(method)
    end
  end
end
