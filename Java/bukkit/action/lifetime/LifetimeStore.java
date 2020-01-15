/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.module.stats.action.lifetime;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import lombok.Getter;
import lombok.ToString;
import net.avicus.atlas.match.Match;
import net.avicus.atlas.module.groups.Competitor;
import net.avicus.atlas.module.groups.GroupsModule;
import net.avicus.atlas.module.objectives.Objective;
import net.avicus.atlas.module.objectives.ObjectivesModule;
import net.avicus.atlas.module.stats.action.base.Action;
import net.avicus.atlas.module.stats.action.lifetime.type.CompetitorLifetime;
import net.avicus.atlas.module.stats.action.lifetime.type.MatchLifetime;
import net.avicus.atlas.module.stats.action.lifetime.type.ObjectiveLifetime;
import net.avicus.atlas.module.stats.action.lifetime.type.PlayerLifetime;
import org.bukkit.entity.Player;

/**
 * Container class used to store all lifetimes for a match.
 */
@Getter
@ToString
public class LifetimeStore {

    // Lifetimes
    private final MatchLifetime matchLifetime;
    private final ArrayListMultimap<UUID, PlayerLifetime> playerLifetimes = ArrayListMultimap
            .create();
    private final HashMap<Competitor, CompetitorLifetime> competitorLifetimes = Maps.newHashMap();
    private final HashMap<Objective, ObjectiveLifetime> objectiveLifetimes = Maps.newHashMap();

    public LifetimeStore(Match match) {
        this.matchLifetime = new MatchLifetime(Instant.now(), match);
        match.getModule(ObjectivesModule.class).ifPresent(objectivesModule -> {
            objectivesModule.getObjectives().forEach(objective -> {
                this.objectiveLifetimes.put(objective, new ObjectiveLifetime(Instant.now(), objective));
            });
        });
        match.getModule(GroupsModule.class).ifPresent(groupsModule -> {
            groupsModule.getCompetitors().forEach(competitor -> {
                this.competitorLifetimes.put(competitor, new CompetitorLifetime(Instant.now(), competitor));
            });
        });
        match.getPlayers().forEach(this::restartLifetime);
    }

    /**
     * Get (or create) a lifetime for a player.
     * If create is false and the player has no current lifetime, the will return null.
     * Callers who set create to true need not check for null values.
     *
     * @param player to get the lifetime for
     * @param create if no lifetime if found, if one should be created
     */
    public PlayerLifetime getCurrentLifetime(Player player, boolean create) {
        if (this.playerLifetimes.containsKey(player.getUniqueId())) {
            return this.playerLifetimes.get(player.getUniqueId())
                    .get(this.playerLifetimes.get(player.getUniqueId()).size() - 1);
        }

        if (create) {
            PlayerLifetime lifetime = restartLifetime(player);
            this.playerLifetimes.put(player.getUniqueId(), lifetime);
            return lifetime;
        }

        return null;
    }

    /**
     * Get (or create) a lifetime for the specified objective.
     *
     * @param objective to get the lifetime for
     */
    public ObjectiveLifetime getCurrentLifetime(Objective objective) {
        if (this.objectiveLifetimes.containsKey(objective)) {
            return this.objectiveLifetimes.get(objective);
        }

        ObjectiveLifetime newLife = new ObjectiveLifetime(Instant.now(), objective);
        this.objectiveLifetimes.put(objective, newLife);
        return newLife;
    }

    /**
     * Get (or create) a lifetime for the specified competitor.
     *
     * @param competitor to get the lifetime for
     */
    public CompetitorLifetime getCurrentLifetime(Competitor competitor) {
        if (this.competitorLifetimes.containsKey(competitor)) {
            return this.competitorLifetimes.get(competitor);
        }

        CompetitorLifetime newLife = new CompetitorLifetime(Instant.now(), competitor);
        this.competitorLifetimes.put(competitor, newLife);
        return newLife;
    }

    /**
     * End a player's current lifetime (if one exists) and start a new one.
     *
     * @param player to restart the lifetime for
     */
    public PlayerLifetime restartLifetime(Player player) {
        PlayerLifetime lifetime = this.getCurrentLifetime(player, false);
        if (lifetime != null) {
            lifetime.end();
        }

        PlayerLifetime newLife = new PlayerLifetime(Instant.now(), player.getUniqueId());

        this.playerLifetimes.put(player.getUniqueId(), newLife);
        return newLife;
    }

    /**
     * Get a player's combined score for their current lifetime.
     *
     * @param uuid to get the score for
     */
    public double getScore(UUID uuid) {
        return this.getPlayerLifetimes().get(uuid).stream().flatMap(l -> l.getActions().stream())
                .mapToDouble(Action::getScore).sum();
    }

    /**
     * Get the most common attribute shared across mutliple action classes for all player lifetimes.
     * This will return null IF no attributes match.
     *
     * @param actionClass type of action filer stream by
     * @param refMethod   function to get an attribute from the specified action
     * @param <N>         type of attribute that should be returned
     * @param <C>         type of action that the stream is iterating through
     */
    @Nullable
    public <N, C extends Action> N mostCommonAttribute(Class<? extends C> actionClass,
                                                       Function<C, N> refMethod) {
        Multiset<N> commons = HashMultiset.create();
        this.getPlayerLifetimes().values().stream()
                .flatMap(listContainer -> listContainer.getActions().stream())
                .collect(Collectors.toList())
                .stream().filter(act -> act.getClass().equals(actionClass))
                .forEach(action -> commons.add(refMethod.apply((C) action)));

        return commons.entrySet()
                .stream()
                .max(Comparator.comparing(Multiset.Entry::getCount)).map(Multiset.Entry::getElement)
                .orElse(null);
    }

    /**
     * See {@link #mostCommonAttribute(Class, Function)}. This just filters by UUID.
     */
    @Nullable
    public <N, C extends Action> N mostCommonAttribute(UUID actor, Class<? extends C> actionClass,
                                                       Function<C, N> refMethod) {
        Multiset<N> commons = HashMultiset.create();
        this.getPlayerLifetimes().get(actor).stream()
                .flatMap(listContainer -> listContainer.getActions().stream())
                .collect(Collectors.toList())
                .stream().filter(act -> act.getClass().equals(actionClass))
                .forEach(action -> commons.add(refMethod.apply((C) action)));

        return commons.entrySet()
                .stream()
                .max(Comparator.comparing(Multiset.Entry::getCount)).map(Multiset.Entry::getElement)
                .orElse(null);
    }
}
