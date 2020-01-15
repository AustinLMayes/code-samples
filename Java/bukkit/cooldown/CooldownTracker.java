/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: N/A (Private Codebase)
*/

package network.walrus.utils.bukkit.cooldown;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Generic class used to "cool down" objects in relation to a specific {@link Player}.
 *
 * <p>When an object has finished cooling, a {@link ObjectCooledEvent} will be fired for the object.
 *
 * <p>By itself, this class does nothing. {@link #tick()} must be called each game tick to activate
 * the cooling mechanism.
 *
 * @author Austin Mayes
 */
public class CooldownTracker {

    private final Player player;
    private final WeakHashMap<Object, Instant> objects;

    /**
     * @param player that this instance is for
     */
    public CooldownTracker(Player player) {
        this.player = player;
        this.objects = new WeakHashMap<>();
    }

    /**
     * Should be called each Minecraft tick, handles removal of cooled objects and event call.
     */
    public void tick() {
        Iterator<Map.Entry<Object, Instant>> objIt = objects.entrySet().iterator();
        while (objIt.hasNext()) {
            Map.Entry<Object, Instant> current = objIt.next();
            if (Instant.now().isAfter(current.getValue())) {
                objIt.remove();
                Bukkit.getPluginManager().callEvent(new ObjectCooledEvent(this.player, current.getKey()));
            }
        }
    }

    /**
     * Submit an object to be cooled after a specified duration.
     *
     * @param object to be cooled
     * @param time   the object should take to cool
     */
    public void coolFor(Object object, Duration time) {
        if (objects.containsKey(object)) {
            throw new IllegalStateException(object + " already has an active cooldown");
        }
        if (!time.equals(Duration.ZERO)) {
            objects.put(object, Instant.now().plus(time));
        }
    }

    /**
     * Determine if an object is currently cooling.
     *
     * @param object to check
     * @return if the object is cooling
     */
    public boolean isCooling(Object object) {
        return objects.containsKey(object);
    }

    /**
     * Negated result of {@link #isCooling(Object)}.
     */
    public boolean isNotCooling(Object object) {
        return !isCooling(object);
    }

    /**
     * Get the time when the specified object is set to be cooled, or {@link Optional#empty()} if the
     * object is not cooling.
     *
     * @param object to check cool completion time
     * @return instant when the requested object will completely cool, or empty if no cool duration is
     * set for the object
     */
    public Optional<Instant> expires(Object object) {
        return Optional.ofNullable(objects.get(object));
    }
}
