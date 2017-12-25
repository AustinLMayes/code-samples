class Appeal < ActiveRecord::Base
  has_many :actions
  belongs_to :punishment
  belongs_to :user

  include GraphQL::QLModel

  graphql_finders(:punishment_id, :user_id, :open, :locked, :appealed, :escalated)

  graphql_type description: 'An attempt by a user to get a punishment removed and/or appealed.',
               fields: {
                   punishment_id: 'ID of the punishment that this appeal is for.',
                   user_id: 'ID of the user who started the appeal.',
                   open: 'If the appeal is open for comments.',
                   locked: 'If the appeal is locked from comments.',
                   appealed: 'If the punishment attached to this appeal has been appealed.',
                   escalated: 'If the appeal has been escalated, allowing only higher staff to comment.',
               },
               create: true, update: true

  def self.permission_definition
    {
        :id_based => false,
        :global_options => {
            options: [:all, :own, :false],
        },
        :permissions_sets => [{
                                  options: [:all, :own, :false],
                                  :actions => {
                                      view: [:open, :locked, :closed, :appealed, :escalated],
                                      open: [:locked, :closed, :appealed, :escalated],
                                      comment: [:open, :locked, :closed, :appealed, :escalated],
                                      close: [:open, :locked, :appealed, :escalated],
                                      appeal: [:open, :locked, :closed, :escalated],
                                      unappeal: [:open, :locked, :closed, :appealed, :escalated],
                                      lock: [:open, :closed, :appealed, :escalated],
                                      unlock: [:locked, :appealed, :escalated],
                                      escalate: [:open, :locked, :closed, :appealed],
                                      de_escalate: [:open, :locked, :closed, :appealed, :escalated],
                                      ping_staff: [:open, :locked, :closed, :appealed, :escalated],
                                      staff_note: [:open, :locked, :closed, :appealed, :escalated]
                                  }
                              },
                              {
                                  :options => [:all, :escalated, :none],
                                  :actions => {
                                      notify_on: [:open, :close, :appeal, :escalate, :de_escalate, :comment]
                                  }
                              }
        ]
    }
  end

  # Get a human-friendly string based on the state of the appeal
  def status
    return 'Appealed' if self.punishment.appealed?
    return 'Closed' if !self.open || self.locked
    return 'Escalated' if self.escalated
    return 'Open'
  end

  # Send an alert to users involed in the appeal.
  # This will NOT
  #   Alert the person who performed the action
  #   Alert users who no longer have access to view the appeal.
  def notify(action, actor)
    users = []

    Rank.all_with_permission(:appeals, :notify_on, action, 'all').each do |r|
      r.members.each do |mem|
        next if users.include?(mem)
        users << mem
      end
    end

    Rank.all_with_permission(:appeals, :notify_on, action, 'escalated').each do |r|
      r.members.each do |mem|
        next if users.include?(mem)
        users << mem
      end
    end if self.escalated?

    text = alert_text(action, actor, false)

    # Alert owner of appeal
    self.actions.first.user.alert("Appeal-#{action}:#{self.id}", "#{alert_text(action, actor, true)}", "/appeals/#{self.id}") unless self.actions.first == actor ||
        (action == :staff_note && !can_execute?(self.actions.first.user, :staff_note))
    self.punishment.staff.alert("Appeal-#{action}:#{self.id}", "#{text}", "/appeals/#{self.id}") unless self.punishment.staff == actor || !owns?(self.punishment.staff) ||
        (action == :staff_note && !can_execute?(self.actions.first.user, :staff_note))
    users.each do |u|
      next if u == actor || u == self.actions.first.user || !can_execute?(u, :staff_note)
      u.alert("Appeal-#{action}:#{self.id}", "#{text}", "/appeals/#{self.id}")
    end
  end

  # Get text based on an action used in alerts sent to users.
  def alert_text(action, actor, own)
    text = ''
    owner = own ? 'Your' : "#{self.actions.first.user.username}'s"
    case action
      when :open
        text = "An appeal has been opened by #{actor.username}, and your attention is required."
      when :close
        text = "#{owner} appeal has been closed by #{actor.username}."
      when :lock
        text = "#{owner} appeal has been locked by #{actor.username}."
      when :appeal
        text = "#{owner} appeal has been appealed by #{actor.username}."
      when :escalate
        text = "#{owner} appeal has been escalated by #{actor.username}, and your attention is required."
      when :de_escalate
        text = "#{owner} appeal has been de-escalated by #{actor.username}."
      when :comment
        text = "#{owner} appeal has a new comment by #{actor.username}."
      when :staff_note
        text = "#{owner} appeal has a new staff note by #{actor.username}."
    end

    text
  end

  def owns?(user)
    user == self.punishment.user || (user == self.punishment.staff && self.punishment.can_issue?(user))
  end

  # Check if a user can respond to this appeal.
  def can_respond(user)
    self.can_execute?(user, :open) ||
        self.can_execute?(user, :comment) ||
        self.can_execute?(user, :close) ||
        self.can_execute?(user, :appeal) ||
        self.can_execute?(user, :unappeal) ||
        self.can_execute?(user, :lock) ||
        self.can_execute?(user, :unlock) ||
        self.can_execute?(user, :escalate) ||
        self.can_execute?(user, :de_escalate) ||
        self.can_execute?(user, :staff_note)
  end

  # Check if a user can perform an action.
  # This is used before an appeal is actually created to check if they have appeal permissions based on a punishment.
  def self.can_execute?(user, punishment, action)
    scope = 'all'
    scope = 'own' if user == punishment.user || (user == punishment.staff && punishment.can_issue?(user))
    user.has_permission?(:appeals, action, :open, scope) || (scope == 'own' && user.has_permission?(:appeals, action, :open, 'all'))
  end

  # Check if a user can perform an action.
  def can_execute?(user, action)
    can = nil
    own = owns?(user)
    [[:open, :closed], :appealed, :escalated].each do |state|
      if state.is_a?(Array)
        if can.nil?
          can = user.has_permission?(:appeals, action, state[0], 'all') if self.send(state[0])
        else
          can = can && user.has_permission?(:appeals, action, state[0], 'all') if self.send(state[0])
        end
        can = can || (own && user.has_permission?(:appeals, action, state[0], 'own')) if self.send(state[0])

        if can.nil?
          can = user.has_permission?(:appeals, action, state[1], 'all') if !self.send(state[0])
        else
          can = can && user.has_permission?(:appeals, action, state[1], 'all') if !self.send(state[0])
        end
        can = can || (own && user.has_permission?(:appeals, action, state[1], 'own')) if !self.send(state[0])
      else
        if state == :appealed
          if can.nil?
            can = user.has_permission?(:appeals, action, state, 'all') if self.punishment.appealed?
          else
            can = can && user.has_permission?(:appeals, action, state, 'all') if self.punishment.appealed?
          end
          can = can || (own && user.has_permission?(:appeals, action, state, 'own')) if self.punishment.appealed?
        else
          if can.nil?
            can = user.has_permission?(:appeals, action, state, 'all') if self.send(state)
          else
            can = can && user.has_permission?(:appeals, action, state, 'all') if self.send(state)
          end
          can = can || (own && user.has_permission?(:appeals, action, state, 'own')) if self.send(state)
        end
      end
    end

    if self.locked?
      can = can && user.has_permission?(:appeals, action, :locked, 'all')
      can = can || (own && user.has_permission?(:appeals, action, :locked, 'own'))
    end

    can
  end
end
