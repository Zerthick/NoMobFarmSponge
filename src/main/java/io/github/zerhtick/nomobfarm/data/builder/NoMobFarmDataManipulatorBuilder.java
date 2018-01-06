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

package io.github.zerhtick.nomobfarm.data.builder;

import io.github.zerhtick.nomobfarm.data.NoMobFarmKeys;
import io.github.zerhtick.nomobfarm.data.immutable.ImmutableNoMobFarmData;
import io.github.zerhtick.nomobfarm.data.mutable.NoMobFarmData;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class NoMobFarmDataManipulatorBuilder extends AbstractDataBuilder<NoMobFarmData> implements DataManipulatorBuilder<NoMobFarmData, ImmutableNoMobFarmData>{

    public NoMobFarmDataManipulatorBuilder() {
        super(NoMobFarmData.class, 1);
    }

    @Override
    public NoMobFarmData create() {
        return new NoMobFarmData();
    }

    @Override
    public Optional<NoMobFarmData> createFrom(DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

    @Override
    protected Optional<NoMobFarmData> buildContent(DataView container) throws InvalidDataException {

        if(container.contains(NoMobFarmKeys.LAST_PLAYER_HIT_TIME) && container.contains(NoMobFarmKeys.FROM_SPAWNER)) {
            final long lastPlayerHitTime = container.getLong(NoMobFarmKeys.LAST_PLAYER_HIT_TIME.getQuery()).orElse(0L);
            final boolean fromSpawner = container.getBoolean(NoMobFarmKeys.FROM_SPAWNER.getQuery()).orElse(false);
            return Optional.of(new NoMobFarmData(lastPlayerHitTime, fromSpawner));
        }

        return Optional.empty();
    }
}
