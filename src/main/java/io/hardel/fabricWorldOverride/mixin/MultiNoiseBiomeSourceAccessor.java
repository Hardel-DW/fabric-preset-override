package io.hardel.fabricWorldOverride.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiNoiseBiomeSource.class)
public interface MultiNoiseBiomeSourceAccessor {
    @Accessor("biomeEntries")
    Either<MultiNoiseUtil.Entries<RegistryEntry<Biome>>, RegistryEntry<MultiNoiseBiomeSourceParameterList>> getBiomeEntries();
} 