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

package io.github.zerhtick.nomobfarm.data.immutable;

import io.github.zerhtick.nomobfarm.data.NoMobFarmKeys;
import io.github.zerhtick.nomobfarm.data.mutable.NoMobFarmData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

public class ImmutableNoMobFarmData extends AbstractImmutableData<ImmutableNoMobFarmData, NoMobFarmData> {

    private final long lastPlayerHitTime;
    private final boolean fromSpawner;

    public ImmutableNoMobFarmData() {
        this(0, false);
    }

    public ImmutableNoMobFarmData(long lastPlayerHitTime, boolean fromSpawner) {
        this.lastPlayerHitTime = lastPlayerHitTime;
        this.fromSpawner = fromSpawner;
        registerGetters();
    }

    protected ImmutableValue<Long> lastPlayerHitTime() {
        return Sponge.getRegistry()
                .getValueFactory()
                .createValue(NoMobFarmKeys.LAST_PLAYER_HIT_TIME, lastPlayerHitTime, 0L)
                .asImmutable();
    }

    protected ImmutableValue<Boolean> fromSpawner() {
        return Sponge.getRegistry()
                .getValueFactory()
                .createValue(NoMobFarmKeys.FROM_SPAWNER, fromSpawner, false)
                .asImmutable();
    }

    private long getLastPlayerHitTime() {
        return lastPlayerHitTime;
    }

    private boolean isFromSpawner() {
        return fromSpawner;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(NoMobFarmKeys.LAST_PLAYER_HIT_TIME, this::getLastPlayerHitTime);
        registerKeyValue(NoMobFarmKeys.LAST_PLAYER_HIT_TIME, this::lastPlayerHitTime);
        registerFieldGetter(NoMobFarmKeys.FROM_SPAWNER, this::isFromSpawner);
        registerKeyValue(NoMobFarmKeys.FROM_SPAWNER, this::fromSpawner);
    }

    @Override
    public NoMobFarmData asMutable() {
        return new NoMobFarmData(lastPlayerHitTime, fromSpawner);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(NoMobFarmKeys.LAST_PLAYER_HIT_TIME, lastPlayerHitTime)
                .set(NoMobFarmKeys.FROM_SPAWNER, fromSpawner);
    }
}
