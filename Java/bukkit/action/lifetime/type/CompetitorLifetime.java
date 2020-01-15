/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.module.stats.action.lifetime.type;

import java.time.Instant;

import lombok.Getter;
import lombok.ToString;
import net.avicus.atlas.module.groups.Competitor;
import net.avicus.atlas.module.stats.action.base.CompetitorAction;

@ToString
public class CompetitorLifetime extends ActionLifetime<CompetitorAction> {

    @Getter
    private final Competitor competitor;

    public CompetitorLifetime(Instant start, Competitor competitor) {
        super(start);
        this.competitor = competitor;
    }
}
