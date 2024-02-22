package dev.sapphic.wearablebackpacks.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.sapphic.wearablebackpacks.BackpackMod;
import dev.sapphic.wearablebackpacks.inventory.BackpackScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class BackpackScreen extends HandledScreen<BackpackScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(BackpackMod.ID, "textures/gui/container/backpack.png");

    public BackpackScreen(final BackpackScreenHandler menu, final PlayerInventory inventory, final Text name) {
        super(menu, inventory, name);
        this.backgroundWidth = (7 * 2) + (Math.max(menu.getColumns(), 9) * 18);
        this.backgroundHeight = 114 + (menu.getRows() * 18);
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    private static void drawTextureSpecial(int x, int y, int z, int w, int h, float u, float v, int rw, int rh, DrawContext ctx) {
        ctx.drawTexture(BackpackScreen.TEXTURE, x, x + w, y, y + h, z, rw, rh, u, v, 64, 64);
    }

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        //MatrixStack matrixStack = context.getMatrices();
        //noinspection ConstantConditions
        this.client.getTextureManager().bindTexture(TEXTURE);

        final int bgW = this.x + this.backgroundWidth;
        final int bgH = this.y + this.backgroundHeight;
        final int fillW = this.backgroundWidth - (4 * 2);
        final int fillH = this.backgroundHeight - (4 * 2);

        ctx.drawTexture(TEXTURE, this.x, this.y, 0, 18.0F, 0.0F, 4, 10, 64, 64); // TOP LEFT
        ctx.drawTexture(TEXTURE, bgW - 4, this.y, 0, 18.0F + 14.0F, 0.0F, 4, 10, 64, 64); // TOP RIGHT
        ctx.drawTexture(TEXTURE, this.x, bgH - 4, 0, 18.0F, 14.0F, 4, 10, 64, 64); //BOTTOM LEFT
        ctx.drawTexture(TEXTURE, bgW - 4, bgH - 4, 0, 18.0F + 14.0F, 14.0F, 4, 10, 64, 64); // BOTTOM RIGHT

        drawTextureSpecial(this.x + 4, this.y, 0, fillW, 4, 18 + 4, 0, 10, 4, ctx); // TOP
        drawTextureSpecial(this.x, this.y + 4, 0, 4, fillH, 18, 4, 4, 10, ctx); // LEFT
        drawTextureSpecial(this.x + 4, bgH - 4, 0, fillW, 4, 18 + 4, 14, 10, 4, ctx); // BOTTOM
        drawTextureSpecial(bgW - 4, this.y + 4, 0, 4, fillH, 18 + 14, 4, 4, 10, ctx); // RIGHT
        drawTextureSpecial(this.x + 4, this.y + 4, 0, fillW, fillH, 22, 4, 10, 10, ctx); // FILL

        for (final Slot slot : this.getScreenHandler().slots) {
            final int x = (this.x + slot.x) - 1;
            final int y = (this.y + slot.y) - 1;
            ctx.drawTexture(TEXTURE, x, y, 0, 0.0F, 0.0F, 18, 18, 64, 64);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
