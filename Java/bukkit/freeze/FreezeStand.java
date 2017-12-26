package net.avicus.magma.module.freeze;

import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Represents an armor stand attached to a UUID used for freezing.
 */
final class FreezeStand {

  /** UUID of the player that this stand is for. */
  private final UUID targetId;
  /** If the stand has been spawned. */
  private boolean active;

  FreezeStand(final UUID targetId) {
    this.targetId = targetId;
  }

  /**
   * Get a player from the stand's target UUID.
   */
  private Optional<Player> getTarget() {
    return Optional.ofNullable(Bukkit.getPlayer(this.targetId));
  }

  /**
   * Create a stand and attach a player to it.
   */
  void create() {
    if (this.active) {
      return;
    }

    this.getTarget().ifPresent(player -> {
      Entity passenger = player;
      while (passenger.getVehicle() != null) {
        passenger = passenger.getVehicle();
      }

      final ArmorStand stand = (ArmorStand) player.getWorld()
          .spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
      stand.setArms(false);
      stand.setBasePlate(false);
      stand.setVisible(false);
      stand.setGravity(false);
      stand.setSmall(true);
      stand.setPassenger(passenger);
      this.active = true;
    });
  }

  /**
   * Remove a stand and release a player.
   */
  void remove() {
    this.getTarget().ifPresent(player -> {
      Entity vehicle = player;
      while (vehicle.getVehicle() != null) {
        vehicle = vehicle.getVehicle();
      }

      if (vehicle.getType() == EntityType.ARMOR_STAND) {
        vehicle.remove();
      }

      this.active = false;
    });
  }
}
