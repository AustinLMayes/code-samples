package net.avicus.atlas.module.stats.action.objective.player;

import java.time.Instant;
import lombok.ToString;
import net.avicus.atlas.module.objectives.Objective;
import net.avicus.atlas.module.stats.action.base.PlayerAction;
import org.bukkit.entity.Player;

/**
 * Action that is triggered when a player completes an objective.
 */
@ToString(callSuper = true)
public abstract class PlayerCompleteObjectiveAction extends
    PlayerInteractWithObjectiveAction implements PlayerAction {

  public PlayerCompleteObjectiveAction(Objective acted, Player actor, Instant when) {
    super(acted, actor, when);
  }
}
