/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.module.stats.action.objective.score;

import net.avicus.atlas.module.objectives.score.ScoreObjective;

/**
 * Interface for all actions that involve scores.
 */
public interface ScoreAction {

    ScoreObjective getScoreObjective();
}
