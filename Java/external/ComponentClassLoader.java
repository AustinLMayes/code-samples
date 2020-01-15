/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: N/A (Private Codebase)
*/

package network.walrus.games.core.external;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import network.walrus.games.core.GamesPlugin;

/**
 * Helper utilities for loading external classes into the {@link ClassLoader} being used by the root
 * bukkit plugin.
 *
 * @author Austin Mayes
 */
public class ComponentClassLoader {

    /**
     * @throws IOException if url addition fails
     * @see #addURL(URL).
     */
    public static URLClassLoader addFile(String s) throws IOException {
        File f = new File(s);
        return addFile(f);
    }

    /**
     * @throws IOException if url addition fails
     * @see #addURL(URL).
     */
    public static URLClassLoader addFile(File f) throws IOException {
        return addURL(f.toURI().toURL());
    }

    /**
     * Load a URL into the system classloader.
     *
     * @param u to load
     * @return the loader with the loaded URL
     * @throws IOException if url addition fails
     */
    public static URLClassLoader addURL(URL u) throws IOException {
        URLClassLoader sysloader = ((URLClassLoader) GamesPlugin.class.getClassLoader());
        Class sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, u);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }

        return sysloader;
    }
}
