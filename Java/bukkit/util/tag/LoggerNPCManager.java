package network.walrus.ubiquitous.bukkit.tracker.tag;

import com.google.common.collect.Maps;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;

/**
 * Manager which keeps track of players assigned to {@link CombatLoggerState}s.
 *
 * @author Austin Mayes
 */
public class LoggerNPCManager {

  private static final Map<Zombie, CombatLoggerState> states = Maps.newHashMap();

  /**
   * @param entity to check
   * @return if the supplied in a combat logger npc
   */
  public static boolean isNPC(Entity entity) {
    return entity instanceof Zombie && entity.hasMetadata("combat-logger");
  }

  /**
   * @param entity to check
   * @return the combat logger state instance attached to the NPC
   */
  public static CombatLoggerState getState(Entity entity) {
    if (entity instanceof Zombie) {
      return states.get(entity);
    }
    return null;
  }

  static void applyState(Zombie zombie, CombatLoggerState state) {
    states.put(zombie, state);
  }

  static void clearState(Zombie zombie) {
    states.remove(zombie);
  }
}
