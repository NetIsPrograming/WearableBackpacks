package dev.sapphic.wearablebackpacks.advancement;

import dev.sapphic.wearablebackpacks.BackpackMod;
import net.fabricmc.api.ModInitializer;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.util.Identifier;

public final class BackpackCriteria implements ModInitializer {
    public static final SimpleCriterion EQUIPPED = criterion("backpack_equipped");
    public static final SimpleCriterion DYED = criterion("backpack_dyed");

    private static SimpleCriterion criterion(final String name) {
        return new SimpleCriterion(new Identifier(BackpackMod.MOD_ID, name));
    }

    @Override
    public void onInitialize() {
        Criteria.register(EQUIPPED);
        Criteria.register(DYED);
    }
}
