class Discussion < ActiveRecord::Base
  include Permissions::Forums
  include CachableModel

  has_many :revisions
  belongs_to :category

  is_impressionable
  acts_as_readable :on => :created_at

  validates :uuid, :uniqueness => true

  # Get the unique ID of this object.
  def to_param
    uuid
  end

  # Helper method to get the author of the discussion.
  def author
    user
  end

  # Get an application-wide link to the discussion.
  def link
    "/forums/discussions/#{uuid}"
  end

  # Get the current revision of the discussion.
  def revision
    revisions.first
  end

  # Get all revisions of the discussion
  # This is sorted from newest to oldest.
  def revisions
    if id == nil
      Revision.where('1=0')
    end
    Revision.where(:discussion_id => id).where('reply_id IS null').order('created_at DESC')
  end

  # Get the number of posts on this discussion a user has not seen.
  def unread_count(user)
    public_replies(user).unread_by(user).size + (unread?(user) ? 1 : 0)
  end

  # Gets all unread posts for a user.
  def get_unread(user)
    list = []
    if unread?(user)
      list.push(self)
    end
    public_replies(user).unread_by(user).each do |reply|
      list.push(reply)
    end
    list
  end

  # Get the total number of views for a discussion.
  def views
    impressionist_count(:filter => :session_hash)
  end

  # The category of the discussion
  def category
    revision.category
  end

  # The category URL of the discussion
  def category_url
    revision.category_url
  end

  # The author of this discussion
  def user
    cached(:user_uncached)
  end

  def user_uncached
    User.find_by_id(user_id)
  end

  # THe title of the discussion
  def title
    revision.title
  end

  # THe body of the discussion
  def body
    revision.body
  end

  # If the discussion is locked
  def locked?
    revision.locked == 1
  end

  # If the discussion is stickied
  def sticky?
    revision.stickied == 1
  end

  # If the discussion is archived
  def archived?
    revision.archived == 1
  end

  # Get all the replies to this discussion
  def replies
    Reply.where(:discussion_id => id).order('created_at ASC')
  end

  # Get the latest reply that a user can see,
  def latest(user)
    public_replies(user).last
  end

  # Get all replies a user can see
  def public_replies(user)
    revisions = Revision.select('reply_id').where(:discussion_id => id).where('reply_id IS NOT NULL').where(:active => 1)

    ids = revisions.map { |r| r.reply_id }
    replies = Reply.where('`replies`.id IN (?)', ids)

    unless !replies.first.nil? && replies.first.can_view?(user)
      exclude = []
      Revision.select(:reply_id).where(:discussion_id => id).where('reply_id IS NOT NULL').where(:active => 1).where(:archived => 1).each do |r|
        exclude.push(r.reply_id)
      end
      if exclude.size > 0
        replies = replies.where('`replies`.id NOT IN (?)', exclude)
      end
    end

    replies
  end
end
