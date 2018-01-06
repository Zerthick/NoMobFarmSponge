/*
 * Copyright (C) 2018  Zerthick
 *
 * This file is part of NoMobFarm.
 *
 * NoMobFarm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * NoMobFarm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NoMobFarm.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.zerhtick.nomobfarm.data;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.TypeTokens;

public class NoMobFarmKeys {

    public static Key<Value<Long>> LAST_PLAYER_HIT_TIME;
    public static Key<Value<Boolean>> FROM_SPAWNER;

    public static void init() {
        LAST_PLAYER_HIT_TIME = Key.builder()
                .type(TypeTokens.LONG_VALUE_TOKEN)
                .query(DataQuery.of("LastPlayerHitTime"))
                .id("nomobfarm:last_player_hit_time")
                .name("Last Player Hit Time")
                .build();
        FROM_SPAWNER = Key.builder()
                .type(TypeTokens.BOOLEAN_VALUE_TOKEN)
                .query(DataQuery.of("FromSpawner"))
                .id("nomobfarm:from_spawner")
                .name("From Spawner")
                .build();
    }

}
