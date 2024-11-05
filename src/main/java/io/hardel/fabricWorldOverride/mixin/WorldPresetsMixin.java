package io.hardel.fabricWorldOverride.mixin;

import io.hardel.fabricWorldOverride.FabricWorldOverride;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static io.hardel.fabricWorldOverride.FabricWorldOverride.isCustomPreset;


@Mixin(WorldPresets.class)
public class WorldPresetsMixin {
    
    @Inject(
        method = "getWorldPreset",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void onGetWorldPreset(DimensionOptionsRegistryHolder registry, CallbackInfoReturnable<Optional<RegistryKey<WorldPreset>>> cir) {
        Optional<DimensionOptions> overworldOpt = registry.getOrEmpty(DimensionOptions.OVERWORLD);
        if (overworldOpt.isPresent()) {
            if (overworldOpt.get().chunkGenerator() instanceof NoiseChunkGenerator noiseGenerator) {
                if (isCustomPreset(noiseGenerator)) {
                    cir.setReturnValue(Optional.of(FabricWorldOverride.CUSTOM));
                }
            }
        }
    }
} 