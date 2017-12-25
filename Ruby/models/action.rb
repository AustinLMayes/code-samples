class Action < ActiveRecord::Base
  belongs_to :user
  belongs_to :appeal

  # Get a user-friendly string based on the action which is performed.
  def action_text
    case self.action.to_sym
      when :open
        'opened'
      when :appeal
        'accepted'
      when :unappeal
        'unappealed'
      when :close
        'closed'
      when :lock
        'locked'
      when :unlock
        'unlocked'
      when :escalate
        'escalated'
      when :de_escalate
        'un-escalated'
      when :comment
        'commented on'
      when :staff_note
        'left a staff note on'
      else
        raise Exception
    end
  end

  # Get a label style based on the action which is performed.
  def action_style_class
    case self.action.to_sym
      when :open
        'label-success'
      when :appeal
        'label-success'
      when :unappeal
        'label-important'
      when :close
        'label-important'
      when :lock
        'label-important'
      when :unlock
        'label-success'
      when :escalate
        'label-warning'
      when :de_escalate
        'label-danger'
      when :comment
        'label-info'
      when :staff_note
        'label-warning'
      else
        raise Exception
    end
  end

  # Performs numerous updates on appeals based on the action which is performed.
  def update_appeal_state(author)
    case self.action.to_sym
      when :open
        self.appeal.locked = false
        self.appeal.open = true
        if (self.appeal.punishment.staff.nil? || !self.appeal.owns?(self.appeal.punishment.staff)) && !self.appeal.escalated?
          self.appeal.escalated = true
          Action.create(appeal: self.appeal,
                        user: author,
                        action: :escalate,
                        text: '[AUTOMATIC] Escalated since the staff member who issued the punishment can no longer be of assistance.')
        end
      when :appeal
        self.appeal.open = false
        self.appeal.punishment.appealed = true
        self.appeal.punishment.save
        Action.create(appeal: self.appeal,
                      user: author,
                      action: :close,
                      text: '[AUTOMATIC] Closed since the appeal was accepted. Enjoy playing on the Avicus network!')
      when :unappeal
        self.appeal.open = true
        self.appeal.locked = false
        self.appeal.punishment.appealed = false
        self.appeal.punishment.save
        Action.create(appeal: self.appeal,
                      user: author,
                      action: :open,
                      text: '[AUTOMATIC] Re-opened since the punishment was unappealed')
      when :close
        self.appeal.open = false
      when :lock
        self.appeal.locked = true
        self.appeal.open = false
      when :unlock
        self.appeal.locked = false
        self.appeal.open = true
      when :escalate
        self.appeal.locked = false
        self.appeal.open = true
        self.appeal.escalated = true
      when :de_escalate
        self.appeal.locked = false
        self.appeal.open = true
        self.appeal.escalated = false
      when :comment
      when :staff_note

      else
        raise Exception
    end

    self.appeal.save
    self.appeal.notify(self.action.to_sym, author)
  end
end
