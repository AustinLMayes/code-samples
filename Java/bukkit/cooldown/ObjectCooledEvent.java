package network.walrus.utils.bukkit.cooldown;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a {@link CooldownTracker} finishes cooling an object.
 *
 * @author Austin Mayes
 */
public class ObjectCooledEvent extends Event {

  private static HandlerList handlers = new HandlerList();
  private final Player owner;
  private final Object cooled;

  /**
   * Constructor
   *
   * @param owner of the object
   * @param cooled object which has cooled
   */
  public ObjectCooledEvent(Player owner, Object cooled) {
    this.owner = owner;
    this.cooled = cooled;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public Player getOwner() {
    return owner;
  }

  public Object getCooled() {
    return cooled;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
