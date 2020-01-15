/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

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
