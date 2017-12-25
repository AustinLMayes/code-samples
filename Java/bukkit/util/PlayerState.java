package net.avicus.warden;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.joda.time.Instant;

/**
 * Captures information about a player for reference at another time.
 */
@Getter
public class PlayerState {

  private final String name;
  private final UUID uuid;
  private final Instant when;
  private final Position position;
  private final Vector velocity;
  private final Optional<ItemStack> itemInHand;
  private final List<ItemStack> armor;
  private final List<PotionEffect> effects;
  private final boolean sprinting;
  private final boolean sneaking;
  private final boolean blocking;
  private final Optional<EntityType> vehicle;

  public PlayerState(String name,
      UUID uuid,
      Instant when,
      Position position,
      Vector velocity,
      Optional<ItemStack> itemInHand,
      List<ItemStack> armor,
      List<PotionEffect> effects,
      boolean sprinting,
      boolean sneaking,
      boolean blocking,
      Optional<EntityType> vehicle) {

    this.name = name;
    this.uuid = uuid;
    this.when = when;
    this.position = position;
    this.velocity = velocity;
    this.itemInHand = itemInHand;
    this.armor = armor;
    this.effects = effects;
    this.sprinting = sprinting;
    this.sneaking = sneaking;
    this.blocking = blocking;
    this.vehicle = vehicle;
  }

  public static PlayerState of(Player player, Instant when) {
    String name = player.getName();
    UUID uuid = player.getUniqueId();
    Position position = Position.of(player);
    Vector velocity = player.getVelocity();
    Optional<ItemStack> item = Optional.ofNullable(player.getItemInHand());
    List<ItemStack> armor = new ArrayList<>();
    for (ItemStack piece : player.getInventory().getArmorContents()) {
      if (piece != null && piece.getType() != Material.AIR) {
        armor.add(piece);
      }
    }
    List<PotionEffect> effects = new ArrayList<>();
    effects.addAll(player.getActivePotionEffects());
    boolean sprinting = player.isSprinting();
    boolean sneaking = player.isSneaking();
    boolean blocking = player.isBlocking();
    Optional<EntityType> vehicle = Optional.empty();
    if (player.getVehicle() != null) {
      vehicle = Optional.of(player.getVehicle().getType());
    }

    return new PlayerState(name, uuid, when, position, velocity, item, armor, effects, sprinting,
        sneaking, blocking, vehicle);
  }

  public static boolean contains(Collection<PlayerState> list, PlayerState who) {
    for (PlayerState player : list) {
      if (player.isSamePlayer(who)) {
        return true;
      }
    }
    return false;
  }

  public boolean isSamePlayer(PlayerState other) {
    return this.uuid.equals(other.uuid);
  }

  public boolean isHoldingWeapon() {
    // Todo: Check material
    return false;
  }

  public Vector eyeLocation() {
    return this.position.getLocation().clone().add(new Vector(0, getHeight(), 0));
  }

  public double getHeight() {
    return isSneaking() ? 1.65 : 1.8;
  }

  /**
   * Searches for blocks (vectors) in the player's line of sight with reasonable accuracy.
   *
   * @param distance How far to search.
   */
  public List<Vector> lineOfSightBlocks(double distance) {
    double accuracy = 0.15;
    return lineOfSightBlocks(distance, accuracy, new Vector(0.2, 0.2, 0.2));
  }

  /**
   * Searches for blocks (vectors) in the player's line of sight.
   *
   * @param distance How far to search.
   * @param accuracy How much to increment the distance each iteration.
   * @param padding The distance to search around the player's line of sight (like a tunnel)
   */
  public List<Vector> lineOfSightBlocks(double distance, double accuracy, Vector padding) {
    List<Vector> blocks = new ArrayList<>();
    List<Vector> points = lineOfSight(distance, accuracy);
    for (Vector point : points) {
      Vector min = point.clone().subtract(padding);
      Vector max = point.clone().add(padding);

      double dx = (max.getX() - min.getX()) / 2.0;
      double dy = (max.getY() - min.getY()) / 2.0;
      double dz = (max.getZ() - min.getZ()) / 2.0;

      for (double x = min.getX(); x <= max.getX(); x += dx) {
        for (double y = min.getY(); y <= max.getY(); y += dy) {
          for (double z = min.getZ(); z <= max.getZ(); z += dz) {
            Vector corner = new Vector(x, y, z);
            Vector block = new Vector(corner.getBlockX(), corner.getBlockY(), corner.getBlockZ());
            if (!blocks.contains(block)) {
              blocks.add(block);
            }
          }
        }
      }
    }

    return blocks;
  }

  public List<Vector> lineOfSight(double distance, double accuracy) {
    Vector start = eyeLocation();
    Vector direction = this.position.getLook().getDirection().clone();

    List<Vector> result = new ArrayList<>();
    for (double i = 0; i <= distance; i += accuracy) {
      Vector vector = direction.clone().multiply(i);
      Vector point = start.clone().add(vector);
      result.add(point);
    }

    return result;
  }

  public double speedMultiplier(Position from, Position to) {
    double mult = 1.0;

    if (from.getBlockBelow().getType() == Material.ICE
        || to.getBlockBelow().getType() == Material.ICE) {
      mult *= 1.4;
    } else if (from.isAirBelow() || to.isAirBelow()) {
      mult *= 1.5;
    }

    if (from.getBlock().getType() == Material.WEB || to.getBlock().getType() == Material.WEB) {
      mult *= 0.12;
    }

    for (PotionEffect effect : this.effects) {
      if (effect.getType() == PotionEffectType.SPEED) {
        mult *= 1.0f + (0.2 * (effect.getAmplifier() + 1));
      } else if (effect.getType() == PotionEffectType.SLOW) {
        mult *= 1.0f - (0.15 * (effect.getAmplifier() + 1));
      }
    }

    if (this.blocking) {
      mult *= 0.5;
    }

    return mult;
  }

  @Override
  public String toString() {
    return "PlayerState(name=" + this.name + ", uuid=" + this.uuid + ", when=" + this.when + ")";
  }
}
