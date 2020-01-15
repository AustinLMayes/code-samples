package network.walrus.ubiquitous.bukkit.tracker.tag;

import java.util.Collection;
import java.util.Optional;
import network.walrus.ubiquitous.bukkit.UbiquitousBukkitPlugin;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * This class represents a player's state at the time of construction and should be used to preserve
 * when a player's state should be preserved beyond the lifetime of the {@link Player} object.
 *
 * @author Rafi Baum
 */
public class PlayerState {

  protected int maxNoDamageTicks;
  protected int noDamageTicks;
  protected double maxHealth;
  protected double health;
  protected double healthScale;
  protected int xpLevel;
  protected float xpPoints;
  protected float exhaustion;
  protected float saturation;
  protected int foodLevel;
  protected int remainingAir;
  protected int maximumAir;
  protected float fallDistance;
  protected Collection<PotionEffect> effects;
  protected Location location;
  protected int fireTicks;
  protected ItemStack hand;
  protected ItemStack[] inventoryContents;
  protected ItemStack[] armorContents;
  protected boolean frozen;

  /**
   * Construct using the state of a {@link Player}.
   *
   * @param player whose attributes to construct with
   */
  public PlayerState(Player player) {
    this.maxNoDamageTicks = player.getMaximumNoDamageTicks();
    this.noDamageTicks = player.getNoDamageTicks();
    this.maxHealth = player.getMaxHealth();
    this.health = player.getHealth();
    this.healthScale = player.getHealthScale();
    this.xpPoints = player.getExp();
    this.xpLevel = player.getLevel();
    this.exhaustion = player.getExhaustion();
    this.saturation = player.getSaturation();
    this.foodLevel = player.getFoodLevel();
    this.remainingAir = player.getRemainingAir();
    this.maximumAir = player.getMaximumAir();
    this.fallDistance = player.getFallDistance();
    this.effects = player.getActivePotionEffects();
    this.location = player.getLocation();
    this.fireTicks = player.getFireTicks();
    this.hand = player.getItemInHand();
    this.inventoryContents = player.getInventory().getContents();
    this.armorContents = player.getInventory().getArmorContents();
    this.frozen = UbiquitousBukkitPlugin.getInstance().getFreezeManager().isFrozen(player);
  }

  /**
   * Copy constructor.
   *
   * @param state to copy
   */
  public PlayerState(PlayerState state) {
    this.maxNoDamageTicks = state.maxNoDamageTicks;
    this.noDamageTicks = state.noDamageTicks;
    this.maxHealth = state.maxHealth;
    this.health = state.health;
    this.healthScale = state.healthScale;
    this.xpLevel = state.xpLevel;
    this.xpPoints = state.xpPoints;
    this.exhaustion = state.exhaustion;
    this.saturation = state.saturation;
    this.foodLevel = state.foodLevel;
    this.remainingAir = state.remainingAir;
    this.maximumAir = state.maximumAir;
    this.fallDistance = state.fallDistance;
    this.effects = state.effects;
    this.location = state.location;
    this.fireTicks = state.fireTicks;
    this.hand = state.hand;
    this.inventoryContents = state.inventoryContents;
    this.armorContents = state.armorContents;
    this.frozen = state.frozen;
  }

  /**
   * Apply all data from this state to a specific player but teleport the player to a different
   * location, if possible.
   *
   * @param player to apply this state
   * @param location to teleport player
   */
  public void apply(Player player, Optional<Location> location) {
    setAttributes(player, location);
    player.setHealthScale(this.healthScale);
    player.setLevel(this.xpLevel);
    player.setExp(this.xpPoints);
    player.setExhaustion(this.exhaustion);
    player.setSaturation(this.saturation);
    player.setFoodLevel(this.foodLevel);
    player.setItemInHand(this.hand);
    player.getInventory().setContents(this.inventoryContents);
    player.getInventory().setArmorContents(this.armorContents);
    if (this.frozen) {
      UbiquitousBukkitPlugin.getInstance().getFreezeManager().freeze(player);
    }
  }

  /**
   * Apply all data from this state to a specific player.
   *
   * @param player to apply this state
   */
  public void apply(Player player) {
    apply(player, Optional.empty());
  }

  protected void setAttributes(LivingEntity entity, Optional<Location> locationOpt) {
    entity.setMaximumNoDamageTicks(this.maxNoDamageTicks);
    entity.setNoDamageTicks(this.noDamageTicks);
    entity.setMaxHealth(this.maxHealth);
    entity.setHealth(this.health);
    entity.setRemainingAir(this.remainingAir);
    entity.setMaximumAir(this.maximumAir);
    entity.setFallDistance(this.fallDistance);
    entity.getActivePotionEffects().clear();
    for (PotionEffect effect : this.effects) {
      entity.addPotionEffect(effect);
    }
    entity.teleport(locationOpt.orElse(this.location));
    entity.setFireTicks(this.fireTicks);
  }

  protected void setAttributes(LivingEntity entity) {
    setAttributes(entity, Optional.empty());
  }
}
