/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: N/A (Private Codebase)
*/

package network.walrus.ubiquitous.bukkit.chat.filter;

import network.walrus.common.CommandSender;
import network.walrus.ubiquitous.bukkit.UbiquitousPermissions;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Something which takes a {@link String} and manipulates it to fit certain specifications.
 *
 * @author Austin Mayes
 */
public abstract class ChatFilter {

    private final String permission;

    /**
     * @param permission needed for players to bypass this filter
     */
    public ChatFilter(String permission) {
        this.permission = permission;
    }

    /**
     * Filter the specified string
     *
     * @param original to filter
     * @return the filtered string
     */
    public abstract String filter(String original);

    /**
     * Determine if the given sender has the ability to bypass being affected by this filter.
     *
     * @param sender to check
     * @return if the sender can bypass the filter
     */
    public boolean canBypass(CommandSender sender) {
        return sender instanceof ConsoleCommandSender
                || (sender instanceof Player
                && (((Player) sender).hasPermission(UbiquitousPermissions.FILTER_BYPASS_ALL)
                || ((Player) sender).hasPermission(this.permission)));
    }
}
