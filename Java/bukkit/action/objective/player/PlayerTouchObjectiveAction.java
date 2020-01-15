/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.module.stats.action.objective.player;

import java.time.Instant;

import lombok.ToString;
import net.avicus.atlas.module.objectives.Objective;
import net.avicus.atlas.module.stats.action.base.PlayerAction;
import org.bukkit.entity.Player;

/**
 * Action that is triggered when a player touches an objective.
 */
@ToString(callSuper = true)
public abstract class PlayerTouchObjectiveAction extends
        PlayerInteractWithObjectiveAction implements PlayerAction {

    private final boolean helpful;

    public PlayerTouchObjectiveAction(Objective acted, Player actor, Instant when, boolean helpful) {
        super(acted, actor, when);
        this.helpful = helpful;
    }
}
