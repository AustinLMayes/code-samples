/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: N/A (Private Codebase)
*/

package network.walrus.games.core.external;

import network.walrus.games.core.GamesPlugin;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Class to represent a jar that is loaded externally.
 *
 * @author Austin Mayes
 */
public abstract class ExternalComponent {

  private final GamesPlugin plugin;

  /**
   * Constructor.
   *
   * @param plugin which owns the facet
   */
  public ExternalComponent(GamesPlugin plugin) {
    this.plugin = plugin;
  }

  /** Called when main bukkit plugin is enabled. */
  public void onEnable() {}

  /** Called when main bukkit plugin is disabled. */
  public void onDisable() {}

  /**
   * Load in config data specific to the component from the main config.
   *
   * @param section containing all configuration data for the component
   */
  public void loadConfig(ConfigurationSection section) {}

  public GamesPlugin getPlugin() {
    return plugin;
  }
}
