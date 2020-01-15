package network.walrus.utils.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

/**
 * Utilities for modifying {@link Player} attributes.
 *
 * @author Austin Mayes
 */
public class PlayerUtils {

  /**
   * Play a sound for all players on the server.
   *
   * @param sound to play
   */
  public static void broadcastSound(Sound sound) {
    for (Player player : Bukkit.getOnlinePlayers()) {
      player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
    }
  }

  /**
   * Reset a player to the default state they would be in if they joined a vanilla server.
   *
   * @param player to reset
   */
  public static void reset(Player player) {
    // Attributes
    clearAttributes(player);

    // Game mode
    player.setGameMode(GameMode.SURVIVAL);

    // Health/Food
    resetHealth(player);
    resetFood(player);

    // Movement
    disableFlight(player);
    resetSpeed(player);
    resetVelocity(player);

    // Vehicles
    fullyEject(player);

    // Items/Effects
    clearInventory(player);
    clearEffects(player);
    clearXP(player);

    // Entity Data
    player.setFireTicks(0);
    player.setRemainingAir(20);
    player.setCanPickupItems(true);

    // Fake Weather/Time
    player.resetPlayerWeather();
    player.resetPlayerTime();

    // Remove arrows
    player.setArrowsStuck(0);

    // Spigot
    player.spigot().setCollidesWithEntities(true);
    player.spigot().setAffectsSpawning(true);

    // Skins
    resetSkin(player);
  }

  /**
   * Reset a player's global skin to their real one.
   *
   * <p>This will not remove any viewer-specific fake skins.
   *
   * @param player to reset
   */
  public static void resetSkin(Player player) {
    player.setSkin(player.getRealSkin());
  }

  /**
   * Remove all attributes from a player.
   *
   * @param player to reset
   */
  public static void clearAttributes(Player player) {
    for (Attribute value : Attribute.values()) {
      AttributeInstance instance = player.getAttribute(value);
      if (instance != null) {
        for (AttributeModifier attributeModifier : instance.getModifiers()) {
          instance.removeModifier(attributeModifier);
        }
      }
    }
  }

  /**
   * Disable flight for a player and set them to not flying.
   *
   * @param player to reset
   */
  public static void disableFlight(Player player) {
    player.setAllowFlight(false);
    player.setFlying(false);
  }

  /**
   * Reset speed values for a {@link Player} to their vanilla values.
   *
   * @param player to reset
   */
  public static void resetSpeed(Player player) {
    player.setFlySpeed(0.1F);
    player.setWalkSpeed(0.2F);
  }

  /**
   * Remove all XP from a {@link Player}.
   *
   * @param player to remove XP from
   */
  public static void clearXP(Player player) {
    player.setExp(0);
    player.setTotalExperience(0);
    player.setLevel(0);
  }

  /**
   * Reset health values for a {@link Player} to their vanilla values.
   *
   * @param player to reset
   */
  public static void resetHealth(Player player) {
    player.setMaxHealth(20);
    player.setHealth(player.getMaxHealth());
  }

  /**
   * Reset food values for a {@link Player} to their vanilla values.
   *
   * @param player to reset
   */
  public static void resetFood(Player player) {
    player.setFoodLevel(20);
    player.setSaturation(5);
    player.setExhaustion(0);
  }

  /**
   * Eject a {@link Player} from their current vehicle.
   *
   * @param player to eject
   */
  public static void fullyEject(Player player) {
    player.eject();
    if (player.getVehicle() != null) {
      player.getVehicle().eject();
    }
  }

  /**
   * Remove all {@link PotionEffect}s from a player.
   *
   * @param player to clear
   */
  public static void clearEffects(Player player) {
    for (PotionEffect effect : player.getActivePotionEffects()) {
      player.removePotionEffect(effect.getType());
    }
  }

  /**
   * Close a player's inventory, and then completely clear and update it.
   *
   * @param player to clear
   */
  public static void clearInventory(Player player) {
    player.closeInventory();
    player.getInventory().clear();
    player.getInventory().setArmorContents(null);
    player.updateInventory();
    player.setItemOnCursor(null);
  }

  /**
   * Reset a player's velocity and fall distance.
   *
   * @param player to reset
   */
  public static void resetVelocity(Player player) {
    player.setFallDistance(0);
    player.setVelocity(new Vector());
  }
}
