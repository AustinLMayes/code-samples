package net.avicus.atlas.module.stats.action.base;

import org.bukkit.entity.Player;

/**
 * An action performed by a player.
 */
public interface PlayerAction extends Action {

  /**
   * Player who performed the action.
   */
  Player getActor();
}
