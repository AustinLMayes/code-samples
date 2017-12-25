# Represents objects where permissions should be based on ownership.
#
# Implementations that have a `perms_ident` method, that will be used to generate the location in the permissions hash
# If this is not present, the class name downcased and pluralized will be used.
#
# Implementations that have the `handle_ids` method set to true will use the object's ID during permissions hash retrieval.
module Permissions::ScopedViewable

  # NOTE: Each class needs to implement the owns?(user) and perms_ident methods in order for scopes to work
  # If the class has no owner, viewable should be used instead

  # Check if a user can view a field.
  # If user is nil, will return false.
  # Field will be stringified.
  def can_view?(user, field)
    return false if user.nil?
    perms_ident = self.respond_to?(:perms_ident) ? self.perms_ident : self.class.name.underscore.downcase.pluralize.to_sym
    handle_ids = self.respond_to?(:handle_ids) ? self.handle_ids : false

    if handle_ids
      value = user.has_permission?(perms_ident, self.id, :view, field.to_s, 'all')
      if !value && self.owns?(user)
        value = user.has_permission?(perms_ident, self.id, :view, field.to_s, 'own')
      end
    else
      value = user.has_permission?(perms_ident, :view, field.to_s, 'all')
      if !value && self.owns?(user)
        value = user.has_permission?(perms_ident, :view, field.to_s, 'own')
      end
    end

    value
  end

  # Checks if a user has permission to view any of the fields defined in the `perm_fields` hash.
  def can_view_any?(user)
    (self.class.perm_fields.collect { |f| f.to_s }).any? { |field| can_view?(user, field) }
  end
end
