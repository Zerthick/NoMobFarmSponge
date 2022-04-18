/*
 * Copyright (C) 2018-2022 Zerthick
 *
 * This file is part of NoMobFarm.
 *
 * NoMobFarm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NoMobFarm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NoSleep.  If not, see <http://www.gnu.org/licenses/>.
 */


package io.github.zerthick.nomobfarm;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.HarvestEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Plugin(
        "nomobfarm"
)
public class NoMobFarm {

    private final PluginContainer instance;
    private final Logger logger;
    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path configPath;

    private boolean preventSpawnerExp;
    private boolean preventSpawerDrops;
    private boolean dropRule;

    private Key<Value<Long>> lastPlayerHitTimeKey;
    private Key<Value<Boolean>> fromSpawner;

    @Inject
    NoMobFarm(final PluginContainer instance, final Logger logger) {
        this.instance = instance;
        this.logger = logger;
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {

        // Log Start Up to Console
        logger.info(
                instance.metadata().name().orElse("Unknown Plugin") + " version " + instance.metadata().version()
                        + " enabled!");

        // Load permission text from config
        Optional<ConfigurationNode> configOptional = loadConfig();

        if (configOptional.isPresent()) {
            ConfigurationNode config = configOptional.get();
            preventSpawnerExp = config.node("preventSpawnerExp").getBoolean();
            preventSpawerDrops = config.node("preventSpawnerDrops").getBoolean();
            dropRule = config.node("dropRule").getBoolean();
        } else {
            logger.error("Unable to load configuration file!");
        }
    }

    @Listener
    public void onRegisterData(final RegisterDataEvent event) {
        // Create custom keys
        lastPlayerHitTimeKey = Key.from(instance, "last_player_hit_time", Long.class);
        event.register(DataRegistration.of(this.lastPlayerHitTimeKey, Entity.class));

        fromSpawner = Key.from(instance, "from_spawner", Boolean.class);
        event.register(DataRegistration.of(this.fromSpawner, Entity.class));
    }

    @Listener
    public void onSpawnEntitySpawn(SpawnEntityEvent event, @Getter("context") EventContext context, @Root Object root) {

        List<Entity> entities = event.entities();

        context.get(EventContextKeys.SPAWN_TYPE).ifPresent(spawnType -> {
            if (spawnType.equals(SpawnTypes.MOB_SPAWNER.get())) {
                entities.forEach(entity -> entity.offer(fromSpawner, true));
            }
        });

        if (preventSpawnerExp) {
            boolean hasExpOrb = entities.stream()
                    .map(Entity::type).anyMatch(e -> e.equals(EntityTypes.EXPERIENCE_ORB.get()));

            if (hasExpOrb) {

                if (root instanceof HarvestEntityEvent) {

                    HarvestEntityEvent harvestEvent = (HarvestEntityEvent) root;

                    Entity entity = harvestEvent.entity();

                    if (entity instanceof ServerPlayer || !(entity instanceof Living)) { // Don't consider players or non-living entities
                        return;
                    }

                    entity.get(fromSpawner).ifPresent(fromSpawner -> {
                        if (fromSpawner) {
                            filterSpawnEvent(event);
                        }
                    });
                }
            }
        }
    }

    @Listener
    public void onDamageEntity(DamageEntityEvent event, @First EntityDamageSource damageSource, @Getter("entity") Entity entity) {

        if (entity instanceof ServerPlayer) { // Don't consider players
            return;
        }

        if (damageSource instanceof IndirectEntityDamageSource) {
            IndirectEntityDamageSource indirectDamageSource = (IndirectEntityDamageSource) damageSource;

            if (indirectDamageSource.indirectSource() instanceof ServerPlayer) {
                entity.offer(lastPlayerHitTimeKey, Instant.now().toEpochMilli());
            }
        } else if (damageSource.source() instanceof ServerPlayer) {
            entity.offer(lastPlayerHitTimeKey, Instant.now().toEpochMilli());
        }
    }

    @Listener
    public void onDropItemEvent(DropItemEvent event, @Getter("source") Object source) {

        if (source instanceof ServerPlayer || !(source instanceof Living)) { // Don't consider players or non-living entities
            return;
        }

        Living entity = (Living) source;

        if (preventSpawerDrops) {
            entity.get(fromSpawner).ifPresent(fromSpawner -> {
                if (fromSpawner) {
                    event.setCancelled(true);
                }
            });
        }

        if (dropRule) {
            Optional<Long> lastPlayerHitTimeOptional = entity.get(lastPlayerHitTimeKey);

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

    private Optional<ConfigurationNode> loadConfig() {
        if (!configPath.toFile().exists()) {
            // Create config if not exists
            instance.openResource(URI.create("default.conf")).ifPresent(r -> {
                try {
                    Files.copy(r, configPath);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            });
        }
        try {
            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().path(configPath).build();
            return Optional.of(loader.load());
        } catch (ConfigurateException e) {
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    private void filterSpawnEvent(SpawnEntityEvent event) {
        event.filterEntities(e -> !e.type().equals(EntityTypes.EXPERIENCE_ORB.get()));
    }
}
