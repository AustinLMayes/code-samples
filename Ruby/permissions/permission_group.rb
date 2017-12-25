# Represents a group of permissions
# Members can also be groups.
class PermissionGroup

  attr_reader :ident, :text, :desc, :options, :def_option
  attr_accessor :members

  def initialize(ident, text, desc, options, def_option)
    @ident = ident
    @text = text
    @desc = desc
    @options = options
    @def_option = def_option.nil? ? :flow : def_option
    @members = []
  end

  # Add this permission group to a group.
  def add_to_group(group)
    group.members << self
    @parent = group
  end

  # Generate a hash-safe path based on parents and ident's of this permission.
  def hash_path
    res = []
    if @parent.nil?
      i = ident == :api ? ident.to_s : ident.to_s.pluralize
      res << i
    else
      res << @parent.hash_path
      res << ident
    end
    res
  end
end
