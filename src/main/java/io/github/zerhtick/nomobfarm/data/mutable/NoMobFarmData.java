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

package io.github.zerhtick.nomobfarm.data.mutable;

import io.github.zerhtick.nomobfarm.data.NoMobFarmKeys;
import io.github.zerhtick.nomobfarm.data.immutable.ImmutableNoMobFarmData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class NoMobFarmData extends AbstractData<NoMobFarmData, ImmutableNoMobFarmData> {

    private long lastPlayerHitTime;
    private boolean fromSpawner;

    public NoMobFarmData() {
        this(0, false);
    }

    public NoMobFarmData(long lastPlayerHitTime, boolean fromSpawner) {
        this.lastPlayerHitTime = lastPlayerHitTime;
        this.fromSpawner = fromSpawner;
        registerGettersAndSetters();
    }

    protected Value<Long> lastPlayerHitTime() {
        return Sponge.getRegistry()
                .getValueFactory()
                .createValue(NoMobFarmKeys.LAST_PLAYER_HIT_TIME, lastPlayerHitTime, 0L);
    }

    protected Value<Boolean> fromSpawner() {
        return Sponge.getRegistry()
                .getValueFactory()
                .createValue(NoMobFarmKeys.FROM_SPAWNER, fromSpawner, false);
    }

    private long getLastPlayerHitTime() {
        return lastPlayerHitTime;
    }

    private void setLastPlayerHitTime(long lastPlayerHitTime) {
        this.lastPlayerHitTime = lastPlayerHitTime;
    }

    private boolean isFromSpawner() {
        return fromSpawner;
    }

    private void setFromSpawner(boolean fromSpawner) {
        this.fromSpawner = fromSpawner;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(NoMobFarmKeys.LAST_PLAYER_HIT_TIME, this::getLastPlayerHitTime);
        registerFieldSetter(NoMobFarmKeys.LAST_PLAYER_HIT_TIME, this::setLastPlayerHitTime);
        registerKeyValue(NoMobFarmKeys.LAST_PLAYER_HIT_TIME, this::lastPlayerHitTime);
        registerFieldGetter(NoMobFarmKeys.FROM_SPAWNER, this::isFromSpawner);
        registerFieldSetter(NoMobFarmKeys.FROM_SPAWNER, this::setFromSpawner);
        registerKeyValue(NoMobFarmKeys.FROM_SPAWNER, this::fromSpawner);
    }

    @Override
    public Optional<NoMobFarmData> fill(DataHolder dataHolder, MergeFunction overlap) {

        dataHolder.get(NoMobFarmData.class).ifPresent(data -> {
            NoMobFarmData finalData = overlap.merge(this, data);
            setLastPlayerHitTime(finalData.getLastPlayerHitTime());
        });

        return Optional.of(this);
    }

    @Override
    public Optional<NoMobFarmData> from(DataContainer container) {
        container.getLong(NoMobFarmKeys.LAST_PLAYER_HIT_TIME.getQuery())
                .ifPresent(this::setLastPlayerHitTime);
        container.getBoolean(NoMobFarmKeys.FROM_SPAWNER.getQuery())
                .ifPresent(this::setFromSpawner);
        return Optional.of(this);
    }

    @Override
    public NoMobFarmData copy() {
        return new NoMobFarmData(lastPlayerHitTime, fromSpawner);
    }

    @Override
    public ImmutableNoMobFarmData asImmutable() {
        return new ImmutableNoMobFarmData(lastPlayerHitTime, fromSpawner);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(NoMobFarmKeys.LAST_PLAYER_HIT_TIME, getLastPlayerHitTime())
                .set(NoMobFarmKeys.FROM_SPAWNER, isFromSpawner());
    }
}
