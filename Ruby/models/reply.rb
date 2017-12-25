class Reply < ActiveRecord::Base
  include Permissions::Forums #reply-specific permissions at bottom of class.
  acts_as_readable :on => :created_at

  # Get the current revision of the reply.
  def revision
    if id == nil
      return nil
    end
    revisions.first
  end

  # Helper method to return the user who created the reply.
  def author
    user
  end

  # Get all revisions of this reply, sorted from newest to oldest.
  def revisions
    Revision.where(:reply_id => id).order('created_at DESC')
  end

  # Get the user who authored this reply.
  def user
    User.find_by_id(user_id)
  end

  # Get an application-wide link to the reply.
  def link
    "#{discussion.link}?reply=#{id}"
  end

  # Helper method to get a link to this reply.
  def url
    link
  end

  # Check if the reply is archived.
  def archived?
    revision != nil && revision.archived?
  end

  # Get the body of the reply.
  def body
    revision.body
  end

  # Get when the current revision was updated.
  def updated_at
    revision.created_at
  end

  # Get the parent discussion of this reply.
  def discussion
    Discussion.find_by_id(discussion_id)
  end

  # Get the reply that this reply is referring to.
  def reply
    reply_id.presence ? Reply.find(reply_id) : nil
  end

  # Get all replies that are replying to this reply.
  def replies
    Reply.where(:reply_id => id).order('created_at DESC')
  end

  # Check if a user can edit this reply.
  def can_edit?(user)
    visible = user.has_permission?(:categories, self.discussion.category.id.to_s, :replies, :edit, :normal, 'all')

    if !visible && user == self.user
      visible = user.has_permission?(:categories, self.discussion.category.id.to_s, :replies, :edit, :normal, 'own')
    end

    if visible && archived?
      visible = user.has_permission?(:categories, self.discussion.category.id.to_s, :replies, :edit, :archived, 'all')

      if !visible && user == self.user
        visible = user.has_permission?(:categories, self.discussion.category.id.to_s, :replies, :edit, :archived, 'own')
      end
    end

    visible
  end

  # Check if a user can view this reply.
  def can_view?(user)
    visible = user.has_permission?(:categories, self.discussion.category.id.to_s, :view, :normal, 'all')

    if !visible && user == self.user
      visible = user.has_permission?(:categories, self.discussion.category.id.to_s, :view, :normal, 'own')
    end

    if visible && self.archived?
      visible = user.has_permission?(:categories, self.discussion.category.id.to_s, :view, :archived, 'all')

      if !visible && user == self.user
        visible = user.has_permission?(:categories, self.discussion.category.id.to_s, :view, :archived, 'own')
      end
    end

    visible
  end

  # Check if a user can archive this reply.
  def can_archive?(user, category = self.discussion.category)
    visible = user.has_permission?(:categories, category.id.to_s, :replies, :actions, :archive, 'all')

    if !visible && user == self.user
      visible = user.has_permission?(:categories, category.id.to_s, :replies, :actions, :archive, 'own')
    end

    if visible && self.archived?
      visible = user.has_permission?(:categories, category.id.to_s, :replies, :actions, :archive, 'all')

      if !visible && user == self.user
        visible = user.has_permission?(:categories, category.id.to_s, :replies, :actions, :archive, 'own')
      end
    end

    visible
  end

  # Check if a user can mark this reply as sanctioned.
  def can_sanction?(user, category = self.discussion.category)
    visible = user.has_permission?(:categories, category.id.to_s, :replies, :actions, :sanction, 'all')

    if !visible && user == self.user
      visible = user.has_permission?(:categories, category.id.to_s, :replies, :actions, :sanction, 'own')
    end

    if visible && self.archived?
      visible = user.has_permission?(:categories, category.id.to_s, :replies, :actions, :sanction, 'all')

      if !visible && user == self.user
        visible = user.has_permission?(:categories, category.id.to_s, :replies, :actions, :sanction, 'own')
      end
    end

    visible
  end
end