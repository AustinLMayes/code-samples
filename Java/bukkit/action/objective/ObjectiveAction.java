/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.module.stats.action.objective;

import java.time.Instant;

import lombok.Getter;
import lombok.ToString;
import net.avicus.atlas.module.objectives.Objective;
import net.avicus.atlas.module.stats.action.base.Action;
import net.avicus.compendium.locale.text.LocalizedFormat;

/**
 * Any action that is performed on an objective.
 */
@ToString
public abstract class ObjectiveAction implements Action {

    @Getter
    private final Objective acted;
    @Getter
    private final Instant when;

    public ObjectiveAction(Objective acted, Instant when) {
        this.acted = acted;
        this.when = when;
    }

    public abstract LocalizedFormat actionMessage(boolean plural);
}
