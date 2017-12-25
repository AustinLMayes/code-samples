# Represents a permission that is generated from a model or from code before it is converted to a hash.
class Permission

  attr_reader :ident, :text, :desc, :options, :default

  def initialize(ident, text, desc, options, default = :flow)
    @ident = ident
    @text = text
    @desc = desc
    @options = options
    @default = default.nil? ? :flow : default
  end

  # Add this permission to a group.
  def add_to_group(group)
    group.members << self
    @parent = group
  end

  # Generate a hash-safe path based on parents and ident's of this permission.
  def hash_path
    res = []
    if @parent.nil?
      res << ident.to_s.pluralize
    else
      res << @parent.hash_path
      res << ident
    end
    res
  end

end
