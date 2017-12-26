package net.avicus.atlas.module.stats.action.base;

import java.time.Instant;

/**
 * Something that happens.
 */
public interface Action {

  /**
   * When the action started.
\   */
  Instant getWhen();

  /**
   * How hard it was to complete the action.
   */
  double getScore();

  /**
   * Message used to describe the action and provide any pertinent data to developers.
   */
  String getDebugMessage();
}
