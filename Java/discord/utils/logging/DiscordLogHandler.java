/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: N/A (Private Codebase)
*/

package network.walrus.infrastructure.utils.logging;

import com.google.common.collect.ImmutableMap;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import network.walrus.infrastructure.InfrastructureConfig.Discord;
import network.walrus.infrastructure.Main;
import network.walrus.infrastructure.utils.DiscordLoggingUtils;

/**
 * A log handler which sends all log messages to Discord,
 *
 * @author Austin Mayes
 */
public class DiscordLogHandler extends Handler {

    private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final ImmutableMap<Level, Color> coloredLevels =
            ImmutableMap.<Level, Color>builder()
                    .put(Level.ALL, Color.GREEN)
                    .put(Level.FINEST, Color.CYAN)
                    .put(Level.FINER, Color.CYAN)
                    .put(Level.FINE, Color.CYAN)
                    .put(Level.CONFIG, Color.MAGENTA)
                    .put(Level.INFO, Color.GREEN)
                    .put(Level.WARNING, Color.ORANGE)
                    .put(Level.SEVERE, Color.RED)
                    .build();

    @Override
    public void publish(LogRecord record) {
        if (!Discord.enabled() || !Main.getManager().connected) return;

        MessageEmbed embed =
                DiscordLoggingUtils.generateRichMessage(
                        coloredLevels.get(record.getLevel()),
                        FORMAT.format(new Date()) + " - " + record.getLevel().getName(),
                        record.getMessage(),
                        null,
                        null,
                        null,
                        null,
                        new Field("Logger", record.getLoggerName(), false));
        Main.getManager().getLogChannel().sendMessage(embed).queue();
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
