/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.module.stats.action.objective.player;

import java.time.Instant;

import lombok.Getter;
import lombok.ToString;
import net.avicus.atlas.module.objectives.Objective;
import net.avicus.atlas.module.stats.action.base.PlayerAction;
import net.avicus.atlas.module.stats.action.objective.ObjectiveAction;
import org.bukkit.entity.Player;

/**
 * Action that is triggered when a player interacts with an objective.
 */
@ToString(callSuper = true)
public abstract class PlayerInteractWithObjectiveAction extends ObjectiveAction implements
        PlayerAction {

    @Getter
    private final Player actor;

    public PlayerInteractWithObjectiveAction(Objective acted, Player actor, Instant when) {
        super(acted, when);
        this.actor = actor;
    }
}
