package dev.sapphic.wearablebackpacks.stat;

import dev.sapphic.wearablebackpacks.BackpackMod;
import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public final class BackpackStats implements ModInitializer {
    public static final Identifier OPENED = new Identifier(BackpackMod.MOD_ID, "backpacks_opened");
    public static final Identifier CLEANED = new Identifier(BackpackMod.MOD_ID, "backpacks_cleaned");

    private static void register(final Identifier stat) {
        Stats.CUSTOM.getOrCreateStat(Registry.register(Registries.CUSTOM_STAT, stat, stat));
    }

    @Override
    public void onInitialize() {
        register(OPENED);
        register(CLEANED);
    }
}
