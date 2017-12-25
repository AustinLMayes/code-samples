class Category < ActiveRecord::Base
  include Permissions::Forums
  include Permissions::Editable

  belongs_to :forum
  has_many :discussions

  after_destroy :clean_up

  serialize :tags

  # Permissions start

  def self.permission_definition
    {
        :id_based => false,
        :global_options => {
            :text => 'Admin - Categories',
            options: [:true, :false],
        },
        :permissions_sets => [{
                                  :edit => [:name, :priority, :forum_id, :desc, :tags],
                                  :actions => [:create, :update, :destroy]
                              }]
    }
  end

  # Delete dependant classes which should no longer exist if this is deleted.
  def clean_up
    Discussion.where(category_id: self.id).each do |d|
      Reply.where(discussion_id: d.id).destroy_all
      Revision.where(discussion_id: d.id).destroy_all
      d.destroy
    end
  end

  def self.perm_fields
    self.permission_definition[:permissions_sets][0][:edit]
  end

  def can_mass_moderate?(user)
    user.has_permission?(:categories, self.id, :mass_moderate, true)
  end

  # Admin panel
  def admin_can_execute?(user, *action)
    action.flatten!
    return false if user.nil?

    return user.has_permission?(:categories, :actions, action, true)
  end

  # Permissions end
end
