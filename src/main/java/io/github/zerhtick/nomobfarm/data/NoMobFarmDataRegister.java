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

import io.github.zerhtick.nomobfarm.data.builder.NoMobFarmDataManipulatorBuilder;
import io.github.zerhtick.nomobfarm.data.immutable.ImmutableNoMobFarmData;
import io.github.zerhtick.nomobfarm.data.mutable.NoMobFarmData;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.plugin.PluginContainer;

public class NoMobFarmDataRegister {

    public static void registerData(PluginContainer container) {

        NoMobFarmKeys.init();

        DataRegistration.<NoMobFarmData, ImmutableNoMobFarmData>builder()
                .dataClass(NoMobFarmData.class)
                .immutableClass(ImmutableNoMobFarmData.class)
                .builder(new NoMobFarmDataManipulatorBuilder())
                .manipulatorId("no_mob_farm")
                .dataName("No Mob Farm Data")
                .buildAndRegister(container);
    }

}
