## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: https://github.com/Avicus/Website

## END REPOSITORY HEADER ##

module Admin::RanksHelper
  # Generate HTML for the admin panel based on data from a permissions group.
  def perm_group(parent, id_based = false, id = nil)
    res = ''
    if parent.is_a?(PermissionsGenerator)
      name = parent.parent_group.text
      name = parent.model_name if name.nil?
      res += content_tag(:h3, titleize(name.pluralize))
      parent.parent_group.members.each do |mem|
        res += content_tag(:div, perm_group(mem, parent.id_based, id), class: ['row-fluid'])
      end
    elsif parent.is_a?(PermissionGroup) && parent.members != []
      res += content_tag(:h3, titleize(parent.text).html_safe + ' ' + content_tag(:small, parent.desc))
      parent.members.each do |mem|
        if mem.is_a?(PermissionGroup) && mem.members != []
          res += content_tag(:div, perm_group(mem, id_based, id), class: ['row-fluid'], style: 'padding-left: 11px')
        end
        if mem.is_a?(Permission)
          res += render partial: 'admin/ranks/permparts/selector', locals: {text: mem.text, desc: mem.desc, default: parent.def_option, options: mem.options, path: generate_path(id_based, id, mem.hash_path), string_path: "[#{generate_path(id_based, id, mem.hash_path).join('][')}]"}
        end
      end
    elsif parent.is_a?(Permission)
      res += render partial: 'admin/ranks/permparts/selector', locals: {text: parent.text, desc: parent.desc, default: parent.default, options: parent.options, path: generate_path(id_based, id, parent.hash_path), string_path: "[#{generate_path(id_based, id, parent.hash_path).join('][')}]"}
    end
    res.html_safe
  end

  # Generate a hash path with an optional ID inserted at the appropriate place.
  def generate_path(id_based, id, *path)
    path.flatten!
    if id_based
      path.insert(1, "#{id}") # Add the ID to the hash
    end
    path
  end
end
