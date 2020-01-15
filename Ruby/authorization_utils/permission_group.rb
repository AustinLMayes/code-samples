## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: https://github.com/Avicus/Website

## END REPOSITORY HEADER ##

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
