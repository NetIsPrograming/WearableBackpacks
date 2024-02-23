package dev.sapphic.wearablebackpacks.client.render;

import dev.sapphic.wearablebackpacks.block.BackpackBlock;
import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import dev.sapphic.wearablebackpacks.client.BackpackClientMod;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.joml.Math.cos;
import static org.joml.Math.sin;


public final class BackpackBlockRenderer<T extends BlockEntity> implements BlockEntityRenderer<BackpackBlockEntity> {

    private final BlockModels models;
    private final BlockModelRenderer blockModelRenderer;

    public BackpackBlockRenderer(BlockEntityRendererFactory.Context ctx) {
        super();
        BlockRenderManager renderManager = ctx.getRenderManager();
        this.models = renderManager.getModels();
        this.blockModelRenderer = renderManager.getModelRenderer();
    }

    @Override
    public void render(BackpackBlockEntity backpack, float tickDelta, MatrixStack stack, VertexConsumerProvider pipelines, int light, int overlay) {
        Direction facing = backpack.getCachedState().get(BackpackBlock.FACING);
        VertexConsumer pipeline = ItemRenderer.getDirectItemGlintConsumer(pipelines, TexturedRenderLayers.getEntityCutout(), true, backpack.hasGlint());
        BakedModel backpackModel = models.getModel(backpack.getCachedState());
        BakedModel lidModel = models.getModelManager().getModel(BackpackClientMod.getLidModel(facing));

        Quaternionf lidRotation = specialRotation(45.0F * backpack.getLidDelta(tickDelta), facing.rotateYClockwise().getUnitVector());
        int color = backpack.getColor();
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 25;

        boolean xAxis = facing.getAxis() == Direction.Axis.X;
        boolean inverse = facing.getDirection() == AxisDirection.NEGATIVE;
        double xPivot = (inverse ? (1.0 - 0.3125) : 0.3125) * (xAxis ? 1.0 : 0.0);
        double yPivot = 0.5625;
        double zPivot = (inverse ? (1.0 - 0.3125) : 0.3125) * (xAxis ? 0.0 : 1.0);

        blockModelRenderer.render(stack.peek(), pipeline, null, backpackModel, red, green, blue, light, OverlayTexture.DEFAULT_UV);

        stack.push();
        stack.translate(xPivot, yPivot, zPivot);
        stack.multiply(lidRotation);
        stack.translate(-xPivot, -yPivot, -zPivot);
        blockModelRenderer.render(stack.peek(), pipeline, null, lidModel, red, green, blue, light, OverlayTexture.DEFAULT_UV);
        stack.pop();
    }

    private Quaternionf specialRotation(float rotationAngle, Vector3f axis) {
        Quaternionf result = new Quaternionf();

        rotationAngle *= 0.017453292F; // We always use degrees

        float f = sin(rotationAngle / 2.0F);
        result.x = axis.x * f;
        result.y = axis.y * f;
        result.z = axis.z * f;
        result.w = cos(rotationAngle / 2.0F);
        return result;
    }
}

