/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.hook.discord.utils;

import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

public class UserUtils {

    public static boolean hasRole(User user, Role role) {
        if (role == null) {
            return false;
        }

        return role.getGuild().getMember(user).getRoles().contains(role);
    }

    public static boolean hasRoleOrHigher(User user, Role role) {
        if (role == null) {
            return false;
        }

        Role highest = role.getGuild().getMember(user).getRoles().get(0);
        if (highest == null) {
            return false;
        }

        return highest.getPosition() >= role.getPosition();
    }
}
