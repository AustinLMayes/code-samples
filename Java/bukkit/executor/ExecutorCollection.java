/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.module.executors;

import java.util.LinkedHashSet;

import net.avicus.atlas.module.checks.Check;
import net.avicus.atlas.module.checks.CheckContext;

/**
 * A group of executors to be run together using the same variables.
 */
public class ExecutorCollection extends Executor {

    private final LinkedHashSet<Executor> executors;

    public ExecutorCollection(String id, Check check, LinkedHashSet<Executor> executors) {
        super(id, check);
        this.executors = executors;
    }

    @Override
    public void execute(CheckContext context) {
        this.executors.forEach(executor -> {
            CheckContext dupe = context.duplicate();
            executor.executeChecked(dupe);
        });
    }
}
