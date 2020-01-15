/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: N/A (Private Codebase)
*/

package network.walrus.infrastructure.utils.logging;

import network.walrus.infrastructure.utils.DiscordLoggingUtils;

/**
 * An exception handler which logs all exceptions to Discord.
 *
 * @author Austin Mayes
 */
public class DiscordExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        DiscordLoggingUtils.logException("Exception from thread: " + t.getName(), e);
    }
}
