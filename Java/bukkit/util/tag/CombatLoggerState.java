package network.walrus.ubiquitous.bukkit.tracker.tag;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import network.walrus.common.CommandSender;
import network.walrus.ubiquitous.bukkit.UbiquitousBukkitPlugin;
import network.walrus.ubiquitous.bukkit.tracker.event.tag.TaggedPlayerDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftZombie;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionEffectAddEvent.EffectAddReason;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Wrapper class to represent all combat attributes for a player when they log out during combat.
 * This object is only ever active and up to date when the player this represents is not in a
 * competing state.
 *
 * @author Austin Mayes
 */
public class CombatLoggerState extends PlayerState implements LivingEntity, OfflinePlayer {

  private final OfflinePlayer player;
  private @Nullable Zombie zombie;

  /** @param player to pull data from */
  public CombatLoggerState(Player player) {
    super(player);
    this.player = player;
  }

  /**
   * Apply all data from this state to a specific player
   *
   * @param player to apply this state
   */
  @Override
  public void apply(Player player) {
    super.apply(player);
    removeZombie();
  }

  /**
   * @return a new {@link TaggedPlayerDeathEvent} containing all of the information from this
   *     object.
   */
  public TaggedPlayerDeathEvent createDeathEvent() {
    return new TaggedPlayerDeathEvent(
        this,
        location,
        UbiquitousBukkitPlugin.getInstance()
            .getTrackerSupervisor()
            .getLifetimeManager()
            .getLifetime(this),
        Instant.now(),
        Arrays.asList(inventoryContents),
        Math.min(100, this.xpLevel * 7));
  }

  /**
   * Spawn a zombie at the current location, tag it, and apply attributes to it based on data from
   * this object.
   */
  public void spawn() {
    Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
    this.zombie = zombie;
    String name = player.isOnline() ? ((Player) player).getDisplayName() : player.getName();
    zombie.setCustomName(name);
    zombie.setCustomNameVisible(true);
    zombie.setBaby(false);
    zombie.setMetadata(
        "combat-logger",
        new FixedMetadataValue(UbiquitousBukkitPlugin.getInstance(), player.getUniqueId()));
    EntityZombie entity = ((CraftZombie) zombie).getHandle();
    NBTTagCompound tag = entity.getNBTTag();
    if (tag == null) {
      tag = new NBTTagCompound();
    }
    entity.c(tag);
    tag.setInt("NoAI", 1);
    entity.f(tag);
    LoggerNPCManager.applyState(zombie, this);
    update();
  }

  /** Update the zombie with fresh data from this class. */
  public void update() {
    if (zombie == null) {
      return;
    }
    setAttributes(zombie);
    EntityEquipment equipment = zombie.getEquipment();
    equipment.setItemInHand(this.hand);
    equipment.setArmorContents(this.armorContents);
  }

  /**
   * Subtract the health by a specified amount, and kill the zombie if health is at or under {@code
   * 0}.
   *
   * @param amount to subtract
   */
  public void subtractHealth(double amount) {
    setHealth(Math.max(0, this.health - amount));
    if (this.health <= 0) {
      die();
    }
  }

  private void removeZombie() {
    Zombie zombie = Objects.requireNonNull(this.zombie);
    zombie.remove();
    zombie.removeMetadata("combat-logger", UbiquitousBukkitPlugin.getInstance());
    LoggerNPCManager.clearState(zombie);
    this.zombie = null;
  }

  /** Remove the zombie and drop all items naturally. */
  public void die() {
    removeZombie();
    for (ItemStack item : this.inventoryContents) {
      if (item == null || item.getType() == Material.AIR) continue;
      this.location.getWorld().dropItemNaturally(this.location, item);
    }
    for (ItemStack item : this.armorContents) {
      if (item == null || item.getType() == Material.AIR) continue;
      this.location.getWorld().dropItemNaturally(this.location, item);
    }
    ExperienceOrb orb =
        (ExperienceOrb)
            this.location.getWorld().spawnEntity(this.location, EntityType.EXPERIENCE_ORB);
    orb.setExperience(Math.min(100, this.xpLevel * 7));
  }

  /** Tick away hunger in a natural minecraft progression */
  public void tickHunger() {
    if (this.exhaustion > 0) {
      this.exhaustion -= -0.5f;
    } else if (this.foodLevel > 0) {
      this.foodLevel -= -1;
    } else if (this.health > 1) {
      subtractHealth(1);
    }
  }

  @Override
  public double getEyeHeight() {
    return Objects.requireNonNull(this.zombie).getEyeHeight();
  }

  @Override
  public double getEyeHeight(boolean b) {
    return Objects.requireNonNull(this.zombie).getEyeHeight(b);
  }

  @Override
  public Location getEyeLocation() {
    return Objects.requireNonNull(this.zombie).getEyeLocation();
  }

  @Override
  public List<Block> getLineOfSight(HashSet<Byte> hashSet, int i) {
    return Objects.requireNonNull(this.zombie).getLineOfSight(hashSet, i);
  }

  @Override
  public List<Block> getLineOfSight(Set<Material> set, int i) {
    return Objects.requireNonNull(this.zombie).getLineOfSight(set, i);
  }

  @Override
  public Block getTargetBlock(HashSet<Byte> hashSet, int i) {
    return Objects.requireNonNull(this.zombie).getTargetBlock(hashSet, i);
  }

  @Override
  public Block getTargetBlock(Set<Material> set, int i) {
    return Objects.requireNonNull(this.zombie).getTargetBlock(set, i);
  }

  @Override
  public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hashSet, int i) {
    return Objects.requireNonNull(this.zombie).getLastTwoTargetBlocks(hashSet, i);
  }

  @Override
  public List<Block> getLastTwoTargetBlocks(Set<Material> set, int i) {
    return Objects.requireNonNull(this.zombie).getLastTwoTargetBlocks(set, i);
  }

  @Override
  public Egg throwEgg() {
    return Objects.requireNonNull(this.zombie).throwEgg();
  }

  @Override
  public Snowball throwSnowball() {
    return Objects.requireNonNull(this.zombie).throwSnowball();
  }

  @Override
  public Arrow shootArrow() {
    return Objects.requireNonNull(this.zombie).shootArrow();
  }

  @Override
  public int getRemainingAir() {
    return this.remainingAir;
  }

  @Override
  public void setRemainingAir(int i) {
    this.remainingAir = i;
    update();
  }

  @Override
  public int getMaximumAir() {
    return this.maximumAir;
  }

  @Override
  public void setMaximumAir(int i) {
    this.maximumAir = i;
    update();
  }

  @Override
  public int getMaximumNoDamageTicks() {
    return this.maxNoDamageTicks;
  }

  @Override
  public void setMaximumNoDamageTicks(int i) {
    this.maxNoDamageTicks = i;
    update();
  }

  @Override
  public double getLastDamage() {
    return 0;
  }

  @Override
  public void setLastDamage(double v) {}

  @Override
  public int getNoDamageTicks() {
    return this.noDamageTicks;
  }

  @Override
  public void setNoDamageTicks(int i) {
    this.noDamageTicks = i;
    update();
  }

  @Override
  public Player getKiller() {
    return null;
  }

  @Override
  public boolean addPotionEffect(PotionEffect potionEffect) {
    return addPotionEffect(potionEffect, EffectAddReason.CUSTOM);
  }

  @Override
  public boolean addPotionEffect(PotionEffect potionEffect, boolean b) {
    return addPotionEffect(potionEffect, b, EffectAddReason.CUSTOM);
  }

  @Override
  public boolean addPotionEffects(Collection<PotionEffect> collection) {
    return addPotionEffects(collection, EffectAddReason.CUSTOM);
  }

  @Override
  public boolean addPotionEffect(PotionEffect potionEffect, EffectAddReason reason) {
    boolean bool = this.getActivePotionEffects().add(potionEffect);
    update();
    return bool;
  }

  @Override
  public boolean addPotionEffect(PotionEffect potionEffect, boolean b, EffectAddReason reason) {
    boolean bool = this.getActivePotionEffects().add(potionEffect);
    update();
    return bool;
  }

  @Override
  public boolean addPotionEffects(Collection<PotionEffect> collection, EffectAddReason reason) {
    boolean bool = getActivePotionEffects().addAll(collection);
    update();
    return bool;
  }

  @Override
  public boolean hasPotionEffect(PotionEffectType potionEffectType) {
    for (PotionEffect e : this.getActivePotionEffects()) {
      if (e.getType() == potionEffectType) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void removePotionEffect(PotionEffectType potionEffectType) {
    this.getActivePotionEffects().removeIf(e -> e.getType() == potionEffectType);
    update();
  }

  @Override
  public Collection<PotionEffect> getActivePotionEffects() {
    return this.effects;
  }

  @Override
  public void setPotionParticles(boolean b) {
    Objects.requireNonNull(zombie).setPotionParticles(b);
  }

  @Override
  public boolean hasLineOfSight(Entity entity) {
    return Objects.requireNonNull(zombie).hasLineOfSight(entity);
  }

  @Override
  public boolean getRemoveWhenFarAway() {
    return Objects.requireNonNull(zombie).getRemoveWhenFarAway();
  }

  @Override
  public void setRemoveWhenFarAway(boolean b) {
    Objects.requireNonNull(zombie).setRemoveWhenFarAway(b);
  }

  @Override
  public EntityEquipment getEquipment() {
    return Objects.requireNonNull(zombie).getEquipment();
  }

  @Override
  public boolean getCanPickupItems() {
    return Objects.requireNonNull(zombie).getCanPickupItems();
  }

  @Override
  public void setCanPickupItems(boolean b) {
    Objects.requireNonNull(zombie).setCanPickupItems(b);
  }

  @Override
  public boolean isLeashed() {
    return Objects.requireNonNull(zombie).isLeashed();
  }

  @Override
  public Entity getLeashHolder() throws IllegalStateException {
    return Objects.requireNonNull(zombie).getLeashHolder();
  }

  @Override
  public boolean setLeashHolder(Entity entity) {
    return Objects.requireNonNull(zombie).setLeashHolder(entity);
  }

  @Override
  public int getArrowsStuck() {
    return Objects.requireNonNull(zombie).getArrowsStuck();
  }

  @Override
  public void setArrowsStuck(int i) {
    Objects.requireNonNull(zombie).setArrowsStuck(i);
  }

  @Override
  public AttributeInstance getAttribute(Attribute attribute) {
    return null;
  }

  @Override
  public void damage(double v) {
    subtractHealth(v);
    update();
  }

  @Override
  public void damage(double v, Entity entity) {}

  @Override
  public double getHealth() {
    return this.health;
  }

  @Override
  public void setHealth(double v) {
    this.health = v;
    update();
  }

  @Override
  public double getMaxHealth() {
    return this.maxHealth;
  }

  @Override
  public void setMaxHealth(double v) {
    this.maxHealth = v;
    update();
  }

  @Override
  public void resetMaxHealth() {
    this.maxHealth = 20;
    update();
  }

  @Override
  public Location getLocation() {
    return Objects.requireNonNull(zombie).getLocation();
  }

  @Override
  public Location getLocation(Location location) {
    return Objects.requireNonNull(zombie).getLocation(location);
  }

  @Override
  public Vector getVelocity() {
    return Objects.requireNonNull(zombie).getVelocity();
  }

  @Override
  public void setVelocity(Vector vector) {
    Objects.requireNonNull(zombie).setVelocity(vector);
  }

  @Override
  public float getKnockbackReduction() {
    return Objects.requireNonNull(zombie).getKnockbackReduction();
  }

  @Override
  public void setKnockbackReduction(float v) {
    Objects.requireNonNull(zombie).setKnockbackReduction(v);
  }

  @Override
  public boolean isOnGround() {
    return Objects.requireNonNull(zombie).isOnGround();
  }

  @Override
  public World getWorld() {
    return Objects.requireNonNull(zombie).getWorld();
  }

  @Override
  public boolean teleport(Location location) {
    boolean res = Objects.requireNonNull(zombie).teleport(location);
    update();
    return res;
  }

  @Override
  public boolean teleport(Location location, TeleportCause teleportCause) {
    boolean res = Objects.requireNonNull(zombie).teleport(location, teleportCause);
    update();
    return res;
  }

  @Override
  public boolean teleport(Entity entity) {
    boolean res = Objects.requireNonNull(zombie).teleport(entity);
    update();
    return res;
  }

  @Override
  public boolean teleport(Entity entity, TeleportCause teleportCause) {
    boolean res = Objects.requireNonNull(zombie).teleport(entity, teleportCause);
    update();
    return res;
  }

  @Override
  public List<Entity> getNearbyEntities(double v, double v1, double v2) {
    return Objects.requireNonNull(zombie).getNearbyEntities(v, v1, v2);
  }

  @Override
  public int getEntityId() {
    return Objects.requireNonNull(zombie).getEntityId();
  }

  @Override
  public int getFireTicks() {
    return this.fireTicks;
  }

  @Override
  public void setFireTicks(int i) {
    this.fireTicks = i;
    update();
  }

  @Override
  public int getMaxFireTicks() {
    return Objects.requireNonNull(zombie).getMaxFireTicks();
  }

  @Override
  public void remove() {
    Objects.requireNonNull(zombie).remove();
    update();
  }

  @Override
  public boolean isDead() {
    return zombie == null || zombie.isDead();
  }

  @Override
  public boolean isValid() {
    return Objects.requireNonNull(zombie).isValid();
  }

  @Override
  public Server getServer() {
    return Bukkit.getServer();
  }

  @Override
  public Entity getPassenger() {
    return Objects.requireNonNull(zombie).getPassenger();
  }

  @Override
  public boolean setPassenger(Entity entity) {
    boolean res = Objects.requireNonNull(zombie).setPassenger(entity);
    update();
    return res;
  }

  @Override
  public boolean isEmpty() {
    return Objects.requireNonNull(zombie).isEmpty();
  }

  @Override
  public boolean eject() {
    boolean res = Objects.requireNonNull(zombie).eject();
    update();
    return res;
  }

  @Override
  public float getFallDistance() {
    return this.fallDistance;
  }

  @Override
  public void setFallDistance(float v) {
    this.fallDistance = v;
    update();
  }

  @Override
  public EntityDamageEvent getLastDamageCause() {
    return Objects.requireNonNull(zombie).getLastDamageCause();
  }

  @Override
  public void setLastDamageCause(EntityDamageEvent entityDamageEvent) {
    // TODO
  }

  @Override
  public UUID getUniqueId() {
    return player.getUniqueId();
  }

  @Override
  public int getTicksLived() {
    return Objects.requireNonNull(zombie).getTicksLived();
  }

  @Override
  public void setTicksLived(int i) {
    Objects.requireNonNull(zombie).setTicksLived(i);
  }

  @Override
  public void playEffect(EntityEffect entityEffect) {
    throw new UnsupportedOperationException();
  }

  @Override
  public EntityType getType() {
    return EntityType.PLAYER;
  }

  @Override
  public boolean isInsideVehicle() {
    return Objects.requireNonNull(zombie).isInsideVehicle();
  }

  @Override
  public boolean leaveVehicle() {
    boolean res = Objects.requireNonNull(zombie).leaveVehicle();
    update();
    return res;
  }

  @Override
  public Entity getVehicle() {
    return Objects.requireNonNull(zombie).getVehicle();
  }

  @Override
  public String getCustomName() {
    return Objects.requireNonNull(zombie).getCustomName();
  }

  @Override
  public void setCustomName(String s) {
    Objects.requireNonNull(zombie).setCustomName(s);
  }

  @Override
  public boolean isCustomNameVisible() {
    return Objects.requireNonNull(zombie).isCustomNameVisible();
  }

  @Override
  public void setCustomNameVisible(boolean b) {
    Objects.requireNonNull(zombie).setCustomNameVisible(b);
  }

  @Override
  public Chunk getChunk() {
    return location.getChunk();
  }

  @Override
  public Spigot spigot() {
    return Objects.requireNonNull(zombie).spigot();
  }

  @Override
  public void sendMessage(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendMessage(String[] strings) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    return Objects.requireNonNull(zombie).getName();
  }

  @Override
  public String getName(CommandSender commandSender) {
    return Objects.requireNonNull(zombie).getName(commandSender);
  }

  @Override
  public void setFakeName(CommandSender commandSender, String s) {
    Objects.requireNonNull(zombie).setFakeName(commandSender, s);
  }

  @Override
  public String getFakeName(CommandSender commandSender) {
    return Objects.requireNonNull(zombie).getFakeName(commandSender);
  }

  @Override
  public boolean hasFakeName(CommandSender commandSender) {
    return Objects.requireNonNull(zombie).hasFakeName(commandSender);
  }

  @Override
  public void clearFakeNames() {
    Objects.requireNonNull(zombie).clearFakeNames();
  }

  @Override
  public void setFakeDisplayName(CommandSender commandSender, String s) {
    Objects.requireNonNull(zombie).setFakeDisplayName(commandSender, s);
  }

  @Override
  public String getFakeDisplayName(CommandSender commandSender) {
    return Objects.requireNonNull(zombie).getFakeDisplayName(commandSender);
  }

  @Override
  public boolean hasFakeDisplayName(CommandSender commandSender) {
    return Objects.requireNonNull(zombie).hasFakeDisplayName(commandSender);
  }

  @Override
  public void clearFakeDisplayNames() {
    Objects.requireNonNull(zombie).clearFakeDisplayNames();
  }

  @Override
  public String getPlayerListName(CommandSender commandSender) {
    return Objects.requireNonNull(zombie).getPlayerListName(commandSender);
  }

  @Override
  public String getDisplayName(CommandSender commandSender) {
    return Objects.requireNonNull(zombie).getDisplayName(commandSender);
  }

  @Override
  public void setMetadata(String s, MetadataValue metadataValue) {
    Objects.requireNonNull(zombie).setMetadata(s, metadataValue);
    update();
  }

  @Override
  public MetadataValue getMetadata(String s, Plugin plugin) {
    return Objects.requireNonNull(zombie).getMetadata(s, plugin);
  }

  @Override
  public List<MetadataValue> getMetadata(String s) {
    return Objects.requireNonNull(zombie).getMetadata(s);
  }

  @Override
  public boolean hasMetadata(String s) {
    return Objects.requireNonNull(zombie).hasMetadata(s);
  }

  @Override
  public void removeMetadata(String s, Plugin plugin) {
    Objects.requireNonNull(zombie).removeMetadata(s, plugin);
    update();
  }

  @Override
  public boolean isPermissionSet(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isPermissionSet(Permission permission) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasPermission(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasPermission(Permission permission) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PermissionAttachment addAttachment(Plugin plugin) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PermissionAttachment addAttachment(Plugin plugin, int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeAttachment(PermissionAttachment permissionAttachment) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAttachments(Plugin plugin) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAttachments(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAttachments(Permission permission) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAttachments(Plugin plugin, String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAttachments(Plugin plugin, Permission permission) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void recalculatePermissions() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<PermissionAttachmentInfo> getEffectivePermissions() {
    throw new UnsupportedOperationException();
  }

  @Override
  public PermissionAttachmentInfo getEffectivePermission(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<PermissionAttachmentInfo> getAttachments() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<PermissionAttachmentInfo> getAttachments(Plugin plugin) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<PermissionAttachmentInfo> getAttachments(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<PermissionAttachmentInfo> getAttachments(Permission permission) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<PermissionAttachmentInfo> getAttachments(Plugin plugin, String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<PermissionAttachmentInfo> getAttachments(Plugin plugin, Permission permission) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isOp() {
    return player.isOp();
  }

  @Override
  public void setOp(boolean b) {
    player.setOp(b);
  }

  @Override
  public <T extends Projectile> T launchProjectile(Class<? extends T> aClass) {
    return Objects.requireNonNull(zombie).launchProjectile(aClass);
  }

  @Override
  public <T extends Projectile> T launchProjectile(Class<? extends T> aClass, Vector vector) {
    return Objects.requireNonNull(zombie).launchProjectile(aClass, vector);
  }

  @Override
  public boolean isOnline() {
    return true;
  }

  @Override
  public boolean isBanned() {
    return player.isBanned();
  }

  @Override
  public void setBanned(boolean b) {
    player.setBanned(b);
  }

  @Override
  public boolean isWhitelisted() {
    return player.isWhitelisted();
  }

  @Override
  public void setWhitelisted(boolean b) {
    player.setWhitelisted(b);
  }

  @Override
  public Player getPlayer() {
    return player.getPlayer();
  }

  @Override
  public long getFirstPlayed() {
    return player.getFirstPlayed();
  }

  @Override
  public long getLastPlayed() {
    return player.getLastPlayed();
  }

  @Override
  public boolean hasPlayedBefore() {
    return player.hasPlayedBefore();
  }

  @Override
  public Location getBedSpawnLocation() {
    return player.getBedSpawnLocation();
  }

  @Override
  public Map<String, Object> serialize() {
    return player.serialize();
  }

  public ItemStack[] getInventoryContents() {
    return inventoryContents;
  }

  public ItemStack[] getArmorContents() {
    return armorContents;
  }

  public boolean isFrozen() {
    return frozen;
  }

  public void setFrozen(boolean frozen) {
    this.frozen = frozen;
  }
}
