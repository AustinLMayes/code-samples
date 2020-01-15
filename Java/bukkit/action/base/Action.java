/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.module.stats.action.base;

import java.time.Instant;

/**
 * Something that happens.
 */
public interface Action {

    /**
     * When the action started.
     * \
     */
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
