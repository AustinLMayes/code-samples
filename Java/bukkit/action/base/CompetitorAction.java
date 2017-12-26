package net.avicus.atlas.module.stats.action.base;

import net.avicus.atlas.module.groups.Competitor;

/**
 * An action performed by a competitor.
 */
public interface CompetitorAction extends Action {

  /**
   * Competitor who performed the action.
   */
  Competitor getActor();
}
