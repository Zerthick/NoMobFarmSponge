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

package io.github.zerhtick.nomobfarm;

import com.google.inject.Inject;
import io.github.zerhtick.nomobfarm.data.NoMobFarmDataRegister;
import io.github.zerhtick.nomobfarm.data.NoMobFarmKeys;
import io.github.zerhtick.nomobfarm.data.mutable.NoMobFarmData;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Plugin(
        id = "nomobfarm",
        name = "NoMobFarm",
        description = "A simple Minecraft plugin to prevent Auto-Mobfarms",
        authors = {
                "Zerthick"
        }
)
public class NoMobFarm {

    @Inject
    private Logger logger;
    @Inject
    private PluginContainer instance;
    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    private boolean preventSpawnerExp;
    private boolean preventSpawerDrops;
    private boolean dropRule;

    @Listener
    public void onServerInit(GameInitializationEvent event) {
        NoMobFarmDataRegister.registerData(getInstance());

        ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();

        //Generate default config if it doesn't exist
        if (!defaultConfig.toFile().exists()) {
            Asset defaultConfigAsset = getInstance().getAsset("DefaultConfig.conf").get();
            try {
                defaultConfigAsset.copyToFile(defaultConfig);
                configLoader.save(configLoader.load());
            } catch (IOException e) {
                logger.error("Error loading default config! Error: " + e.getMessage());
            }
        }

        // Load config values
        try {
            CommentedConfigurationNode configNode = configLoader.load();
            preventSpawnerExp = configNode.getNode("PreventSpawnerExp").getBoolean();
            preventSpawerDrops = configNode.getNode("PreventSpawnerDrops").getBoolean();
            dropRule = configNode.getNode("DropRule").getBoolean();
        } catch (IOException e) {
            logger.error("Error loading config! Error: " + e.getMessage());
        }

    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        getLogger().info(
                instance.getName() + " version " + instance.getVersion().orElse("")
                        + " enabled!");
    }


    private void offerTimeData(Entity entity) {
        entity.getOrCreate(NoMobFarmData.class).ifPresent(data -> {
            data.set(NoMobFarmKeys.LAST_PLAYER_HIT_TIME, Instant.now().toEpochMilli());
            entity.offer(data);
        });
    }

    @Listener
    public void onEntityDamage(DamageEntityEvent event, @First EntityDamageSource damageSource) {

        if(event.getTargetEntity() instanceof Player) { // Don't consider players
            return;
        }

        if(damageSource instanceof IndirectEntityDamageSource) {
            IndirectEntityDamageSource indirectDamageSource = (IndirectEntityDamageSource) damageSource;
            if(indirectDamageSource.getIndirectSource() instanceof Player) {
                offerTimeData(event.getTargetEntity());
            }
        } else if(damageSource.getSource() instanceof Player){
            offerTimeData(event.getTargetEntity());
        }

    }

    private void filterSpawnEvent(SpawnEntityEvent event) {
        event.filterEntities(e -> !e.getType().equals(EntityTypes.EXPERIENCE_ORB));
    }

    @Listener
    public void onSpawnEntitySpawner(SpawnEntityEvent event, @Getter("getEntities") List<Entity> entities, @Getter("getContext")EventContext context) {
        context.get(EventContextKeys.SPAWN_TYPE).ifPresent(spawnType -> {
            if(spawnType.equals(SpawnTypes.MOB_SPAWNER)) {
                entities.forEach(entity -> entity.getOrCreate(NoMobFarmData.class).ifPresent(noMobFarmData -> {
                    noMobFarmData.set(NoMobFarmKeys.FROM_SPAWNER, true);
                    entity.offer(noMobFarmData);
                }));
            }
        });

        if(preventSpawnerExp) {
            boolean hasExpOrb = !entities.stream()
                    .map(Entity::getType)
                    .filter(e -> e.equals(EntityTypes.EXPERIENCE_ORB))
                    .collect(Collectors.toList()).isEmpty();

            if (hasExpOrb) {
                Object causeRoot = event.getCause().root();
                if (causeRoot instanceof Entity) {
                    ((Entity) causeRoot).get(NoMobFarmKeys.FROM_SPAWNER).ifPresent(fromSpawner -> {
                        if (fromSpawner) {
                            filterSpawnEvent(event);
                        }
                    });
                }
            }
        }
    }

    @Listener
    public void onDropItem(DropItemEvent.Destruct event, @Getter("getSource") Object source, @Getter("getCause") Cause cause) {

        if(source instanceof Entity) {

            Entity entity = (Entity)source;

            if(entity instanceof Player) { // Don't consider players
                return;
            }

            if(preventSpawerDrops) {
                Optional<Boolean> fromSpawnerOptional = entity.get(NoMobFarmKeys.FROM_SPAWNER); // Check if entity is from a spawner
                if (fromSpawnerOptional.isPresent()) {
                    if (fromSpawnerOptional.get()) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            if(dropRule) {
                Optional<Long> lastPlayerHitTimeOptional = entity.get(NoMobFarmKeys.LAST_PLAYER_HIT_TIME);
                if (lastPlayerHitTimeOptional.isPresent()) {
                    Instant now = Instant.now();
                    Instant lastHit = Instant.ofEpochMilli(lastPlayerHitTimeOptional.get());
                    Duration duration = Duration.between(now, lastHit);
                    if (duration.compareTo(Duration.ofSeconds(5)) > 0) {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public PluginContainer getInstance() {
        return instance;
    }
}
