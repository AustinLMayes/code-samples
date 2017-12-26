package net.avicus.atlas.module.stats.action.objective.competitor;

import java.time.Instant;
import lombok.ToString;
import net.avicus.atlas.module.groups.Competitor;
import net.avicus.atlas.module.objectives.Objective;
import net.avicus.atlas.module.stats.action.base.CompetitorAction;

/**
 * Action that is triggered when a competitor completes an objective.
 */
@ToString(callSuper = true)
public abstract class CompetitorCompleteObjectiveAction extends
    CompetitorInteractWithObjectiveAction implements CompetitorAction {

  public CompetitorCompleteObjectiveAction(Objective acted, Competitor actor, Instant when) {
    super(acted, actor, when);
  }
}
