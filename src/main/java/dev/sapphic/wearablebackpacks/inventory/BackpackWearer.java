package dev.sapphic.wearablebackpacks.inventory;

import dev.sapphic.wearablebackpacks.BackpackLidAnimator;
import net.minecraft.entity.LivingEntity;

public interface BackpackWearer {
    static BackpackLidAnimator getBackpackState(LivingEntity entity) {
        return ((BackpackWearer) entity).getBackpackState();
    }

    BackpackLidAnimator getBackpackState();
}
