/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.module.stats.action.lifetime.type;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.ToString;
import net.avicus.atlas.module.stats.action.base.PlayerAction;

@ToString
public class PlayerLifetime extends ActionLifetime<PlayerAction> {

    @Getter
    private final UUID player;

    public PlayerLifetime(Instant start, UUID player) {
        super(start);
        this.player = player;
    }
}
