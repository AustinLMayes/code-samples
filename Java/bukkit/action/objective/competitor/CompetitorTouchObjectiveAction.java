/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.module.stats.action.objective.competitor;

import java.time.Instant;

import lombok.ToString;
import net.avicus.atlas.module.groups.Competitor;
import net.avicus.atlas.module.objectives.Objective;
import net.avicus.atlas.module.stats.action.base.CompetitorAction;

/**
 * Action that is triggered when a competitor touches an objective.
 */
@ToString(callSuper = true)
public abstract class CompetitorTouchObjectiveAction extends
        CompetitorInteractWithObjectiveAction implements CompetitorAction {

    public CompetitorTouchObjectiveAction(Objective acted, Competitor actor, Instant when) {
        super(acted, actor, when);
    }
}
