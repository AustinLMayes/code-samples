## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: https://github.com/Avicus/Website

## END REPOSITORY HEADER ##

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
