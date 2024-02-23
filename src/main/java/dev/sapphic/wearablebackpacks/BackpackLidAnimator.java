package dev.sapphic.wearablebackpacks;

import net.minecraft.util.math.MathHelper;

public class BackpackLidAnimator {
    private boolean open;
    private float progress;
    private float lastProgress;

    public void step() {
        this.lastProgress = this.progress;
        float f = 0.2F;
        if (!this.open && this.progress > 0.0F) {
            this.progress = Math.max(this.progress - f, 0.0F);
        } else if (this.open && this.progress < 1.0F) {
            this.progress = Math.min(this.progress + f, 1.0F);
        }
    }

    public float getProgress(float delta) {
        return MathHelper.lerp(delta, this.lastProgress, this.progress);
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean getOpen() {
        return this.open;
    }
}
