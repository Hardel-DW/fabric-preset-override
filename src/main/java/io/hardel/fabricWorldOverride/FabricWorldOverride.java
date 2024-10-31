package io.hardel.fabricWorldOverride;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

public class FabricWorldOverride implements ModInitializer {
    public static final String MOD_ID = "fabric-world-override";
    public static final String LOCATION = "preset";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            try {
                // Get destination world folder
                Path worldFolder = server.getSavePath(WorldSavePath.ROOT);
                Path lockFile = worldFolder.resolve("preset-lock.txt");

                // Check if world has already been processed
                if (Files.exists(lockFile)) {
                    LOGGER.info("World already processed (preset-lock.txt exists). Skipping preset copy.");
                    return;
                }

                // Get the preset world folder from resources
                Path sourceWorld = FabricLoader.getInstance()
                        .getModContainer(MOD_ID)
                        .orElseThrow()
                        .findPath(LOCATION)
                        .orElseThrow(() -> new IOException("Cannot find overworld preset in resources"));

                // Copy entire folder from jar to world
                LOGGER.info("Starting world files copy");
                Files.walk(sourceWorld)
                        .forEach(source -> {
                            try {
                                Path relativePath = sourceWorld.relativize(source);
                                Path dest = worldFolder.resolve(relativePath);

                                // Créer les dossiers parents si nécessaire
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

                // Créer le fichier preset-lock.txt après une copie réussie
                Files.createFile(lockFile);
            } catch (IOException e) {
                LOGGER.error("Failed to copy preset world files", e);
                LOGGER.error("Stack trace: ", e);
                if (e.getCause() != null) {
                    LOGGER.error("Caused by: ", e.getCause());
                }
            }
        });
    }
}
