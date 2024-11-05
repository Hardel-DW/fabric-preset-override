package io.hardel.fabricWorldOverride;

import io.hardel.fabricWorldOverride.mixin.MultiNoiseBiomeSourceAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Unique;

import java.io.IOException;
import java.nio.file.*;

public class FabricWorldOverride implements ModInitializer {
    public static final String MOD_ID = "fabric-world-override";
    public static final String LOCATION = "preset";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final RegistryKey<WorldPreset> CUSTOM = RegistryKey.of(RegistryKeys.WORLD_PRESET, Identifier.of("fabric-world-override:custom"));

    @Override
    public void onInitialize() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            LOGGER.info("World loaded: " + world.getRegistryKey().getValue());
            if (!world.getRegistryKey().equals(World.OVERWORLD)) {
                LOGGER.info("Skipping non-overworld world");
                return;
            }

            ChunkGenerator generator = world.getChunkManager().getChunkGenerator();
            LOGGER.info("Chunk generator: " + generator.getClass().getName());
            if (generator instanceof NoiseChunkGenerator noiseGenerator) {
                LOGGER.info("Noise generator: " + noiseGenerator.getClass().getName());
                if (isCustomPreset(noiseGenerator)) {
                    LOGGER.info("Custom preset detected. Copying world files...");
                    try {
                        Path worldFolder = server.getSavePath(WorldSavePath.ROOT);
                        LOGGER.info("World folder: " + worldFolder);
                        Path lockFile = worldFolder.resolve("preset-lock.txt");
                        LOGGER.info("Lock file: " + lockFile);

                        if (Files.exists(lockFile)) {
                            LOGGER.info("World already processed (preset-lock.txt exists). Skipping preset copy.");
                            return;
                        }

                        Path sourceWorld = FabricLoader.getInstance()
                            .getModContainer(MOD_ID)
                            .orElseThrow()
                            .findPath(LOCATION)
                            .orElseThrow(() -> new IOException("Cannot find overworld preset in resources"));

                        LOGGER.info("Starting world files copy for custom preset");
                        Files.walk(sourceWorld)
                            .forEach(source -> {
                                try {
                                    Path relativePath = sourceWorld.relativize(source);
                                    Path dest = worldFolder.resolve(relativePath);

                                    if (!Files.exists(dest.getParent())) {
                                        Files.createDirectories(dest.getParent());
                                    }

                                    if (Files.isRegularFile(source)) {
                                        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                                    }
                                } catch (IOException e) {
                                    LOGGER.error("Failed to copy file: " + source, e);
                                }
                            });

                        Files.createFile(lockFile);
                    } catch (IOException e) {
                        LOGGER.error("Failed to copy preset world files", e);
                        LOGGER.error("Stack trace: ", e);
                        if (e.getCause() != null) {
                            LOGGER.error("Caused by: ", e.getCause());
                        }
                    }
                }
            }
        });
    }

    @Unique
    public static boolean isCustomPreset(NoiseChunkGenerator generator) {
        BiomeSource biomeSource = generator.getBiomeSource();
        if (biomeSource instanceof MultiNoiseBiomeSource multiNoiseBiomeSource) {
            return ((MultiNoiseBiomeSourceAccessor) multiNoiseBiomeSource).getBiomeEntries().right()
                .map(entry -> entry.matchesKey(RegistryKey.of(
                    RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, Identifier.of("fabric-world-override", "custom")
                )))
                .orElse(false);
        }
        return false;
    }
}
