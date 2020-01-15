/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.module.stats.action.objective.score;

import com.google.common.base.Preconditions;

import java.time.Instant;

import lombok.Getter;
import lombok.ToString;
import net.avicus.atlas.module.objectives.Objective;
import net.avicus.atlas.module.objectives.score.ScoreObjective;
import net.avicus.atlas.module.stats.action.objective.player.PlayerInteractWithObjectiveAction;
import net.avicus.atlas.util.Translations;
import net.avicus.compendium.locale.text.LocalizedFormat;
import org.bukkit.entity.Player;

/**
 * Action that is triggered when a player earns a point.
 */
@ToString
public class PlayerEarnPointAction extends PlayerInteractWithObjectiveAction implements
        ScoreAction {

    @Getter
    private final ScoreObjective scoreObjective;

    public PlayerEarnPointAction(Objective acted, Player actor, Instant when) {
        super(acted, actor, when);
        Preconditions.checkArgument(acted instanceof ScoreObjective, "Objective must be a score.");
        this.scoreObjective = (ScoreObjective) acted;
    }

    @Override
    public double getScore() {
        return 4.2;
    }

    @Override
    public String getDebugMessage() {
        return "Earn Point: " + scoreObjective.getName().translateDefault();
    }

    @Override
    public LocalizedFormat actionMessage(boolean plural) {
        if (plural) {
            return Translations.STATS_OBJECTIVES_POINTS_EARNEDPLURAL;
        }
        return Translations.STATS_OBJECTIVES_POINTS_EARNED;
    }
}
