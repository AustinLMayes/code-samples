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
