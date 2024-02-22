package dev.sapphic.wearablebackpacks.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import dev.sapphic.wearablebackpacks.client.render.BackpackBlockRenderer;
import dev.sapphic.wearablebackpacks.inventory.Backpack;
import dev.sapphic.wearablebackpacks.inventory.BackpackWearer;
import dev.sapphic.wearablebackpacks.BackpackMod;
import dev.sapphic.wearablebackpacks.block.BackpackBlock;
import dev.sapphic.wearablebackpacks.client.mixin.DualVertexConsumerAccessor;
import dev.sapphic.wearablebackpacks.client.mixin.MinecraftClientAccessor;
import dev.sapphic.wearablebackpacks.client.mixin.ModelLoaderAccessor;
import dev.sapphic.wearablebackpacks.client.screen.BackpackScreen;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public final class BackpacksClient implements ClientModInitializer {
    public static final Identifier BACKPACK_STATE_CHANGED = new Identifier(BackpackMod.ID, "backpack_state_changed");

    private static final Identifier BACKPACK_LID = new Identifier(BackpackMod.ID, "backpack_lid");
    private static final KeyBinding BACKPACK_KEY_BINDING = new KeyBinding("key." + BackpackMod.ID + ".backpack", GLFW.GLFW_KEY_B, "key.categories.inventory");

    @Override
    public void onInitializeClient() {
        addLidStateDefinitions();

        HandledScreens.register(BackpackMod.BACKPACK_SCREEN_HANDLER, BackpackScreen::new);

        BlockEntityRendererFactories.register(BackpackMod.BLOCK_ENTITY, BackpackBlockRenderer::new);

        KeyBindingHelper.registerKeyBinding(BACKPACK_KEY_BINDING);

        ClientTickEvents.END_CLIENT_TICK.register(BackpacksClient::pollBackpackKey);

        //Registry.register(Registries.BLOCK_ENTITY_TYPE, BackpackMod.ID, BackpackMod.BLOCK_ENTITY);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tint) -> Backpack.getColor(world, pos), BackpackMod.BLOCK);
        ColorProviderRegistry.ITEM.register((stack, tint) -> Backpack.getColor(stack), BackpackMod.ITEM);

        ClientPlayNetworking.registerGlobalReceiver(BACKPACK_STATE_CHANGED, (client, handler, buf, sender) -> {
            final int entityId = buf.readInt();
            final boolean opened = buf.readBoolean();
            client.execute(() -> {
                final @Nullable Entity entity = client.world.getEntityById(entityId);
                if (!(entity instanceof BackpackWearer)) {
                    throw new IllegalStateException(String.valueOf(entity));
                }
                if (opened) {
                    ((BackpackWearer) entity).getBackpackState().opened();
                } else {
                    ((BackpackWearer) entity).getBackpackState().closed();
                }
            });
        });
    }

    @SuppressWarnings("RedundantTypeArguments")
    private static final ImmutableMap<Direction, ModelIdentifier> LID_MODELS = Arrays.stream(Direction.values())
            .filter(Direction.Type.HORIZONTAL).collect(Maps.toImmutableEnumMap(Function.<Direction>identity(), facing ->
                    new ModelIdentifier(BACKPACK_LID, String.format(Locale.ROOT, "facing=%s", facing.asString()))
            ));

    private static final PacketByteBuf EMPTY_PACKET_BUFFER = new PacketByteBuf(Unpooled.EMPTY_BUFFER);

    public static ModelIdentifier getLidModel(final Direction facing) {
        return Objects.requireNonNull(LID_MODELS.getOrDefault(facing, ModelLoader.MISSING_ID));
    }

    public static void renderBackpack(
            final MatrixStack stack, final VertexConsumerProvider pipelines, final ItemStack backpack,
            final LivingEntity entity, final int light, final BipedEntityModel<?> model
    ) {
        final BlockRenderManager manager = MinecraftClient.getInstance().getBlockRenderManager();
        final BlockModels models = manager.getModels();
        final BlockModelRenderer renderer = manager.getModelRenderer();
        final RenderLayer layer = RenderLayer.getArmorCutoutNoCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        final VertexConsumer pipeline = ItemRenderer.getArmorGlintConsumer(pipelines, layer, false, backpack.hasGlint());
        final BakedModel backpackModel = models.getModel(BackpackMod.BLOCK.getDefaultState());
        final BakedModel lidModel = models.getModelManager().getModel(getLidModel(Direction.NORTH));

        final int color = Backpack.getColor(backpack);
        final float red = ((color >> 16) & 255) / 255.0F;
        final float green = ((color >> 8) & 255) / 255.0F;
        final float blue = (color & 255) / 255.0F;

        //noinspection NumericCastThatLosesPrecision
        final float pitch = 180.0F + (entity.isInSneakingPose() ? (float) Math.toDegrees(model.body.pitch) : 0.0F);

        stack.push();
        stack.translate(0.0, entity.isInSneakingPose() ? 0.2 : 0.0, 0.0);
        stack.multiply(RotationAxis.POSITIVE_X.rotation(pitch)); // I stg I HATE rotations :middlefinger:
        stack.scale(0.8F, 0.8F, 0.8F);
        stack.translate(-0.5, -0.5 - (0.0625 * 4), -0.5 - (0.0625 * 5.5));
        renderer.render(stack.peek(), pipeline, null, backpackModel, red, green, blue, light, OverlayTexture.DEFAULT_UV);

        final double xPivot = 0.0;
        final double yPivot = 0.5625;
        final double zPivot = 1.0 - 0.3125;
        //noinspection CastToIncompatibleInterface
        //final float lidDelta = ((BackpackWearer) entity).getBackpackState().lidDelta(tickDelta());

        final Quaternionf rotation = RotationAxis.POSITIVE_X.rotation(45.0F * 1);

        stack.push();
        stack.translate(xPivot, yPivot, zPivot);
        stack.multiply(rotation);
        stack.translate(-xPivot, -yPivot, -zPivot);
        renderer.render(stack.peek(), pipeline, null, lidModel, red, green, blue, light, OverlayTexture.DEFAULT_UV);
        stack.pop();
        stack.pop();
    }

    public static void renderBackpackQuad(final MatrixStack.Entry entry, final VertexConsumer pipeline, final float red, final float green, final float blue, final List<BakedQuad> quads, final int light, final int overlay) {
        final VertexConsumer delegate = ((DualVertexConsumerAccessor) pipeline).getSecond();
        for (final BakedQuad quad : quads) {
            if (quad.hasColor()) {
                final float quadRed = MathHelper.clamp(red, 0.0F, 1.0F);
                final float quadGreen = MathHelper.clamp(green, 0.0F, 1.0F);
                final float quadBlue = MathHelper.clamp(blue, 0.0F, 1.0F);
                pipeline.quad(entry, quad, quadRed, quadGreen, quadBlue, light, overlay);
            } else {
                delegate.quad(entry, quad, 1.0F, 1.0F, 1.0F, light, overlay);
            }
        }
    }

    private static float tickDelta() {
        //noinspection CastToIncompatibleInterface
        final MinecraftClientAccessor mc = (MinecraftClientAccessor) MinecraftClient.getInstance();
        return ((MinecraftClient) mc).isPaused() ? mc.getPausedTickDelta() : mc.getRenderTickCounter().tickDelta;
    }

    private static void addLidStateDefinitions() {
        ModelLoaderAccessor.setStaticDefinitions(
                ImmutableMap.<Identifier, StateManager<Block, BlockState>>builder()
                        .putAll(ModelLoaderAccessor.getStaticDefinitions())
                        .put(BACKPACK_LID, new StateManager.Builder<Block, BlockState>(Blocks.AIR)
                                .add(BackpackBlock.FACING).build(Block::getDefaultState, BlockState::new)
                        ).build()
        );
    }

    private static void pollBackpackKey(final MinecraftClient client) {
        if ((client.player != null) && client.player.getWorld().isClient) {
            while (BACKPACK_KEY_BINDING.wasPressed()) {
                final ItemStack stack = client.player.getEquippedStack(EquipmentSlot.CHEST);
                if (stack.getItem() instanceof BackpackItem) {
                    final float pitch = (client.player.getWorld().random.nextFloat() * 0.1F) + 0.9F;
                    client.player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.5F, pitch);
                    BackpackClientNetwork.tryOpenOwnBackpack();
                }
            }
        }
    }


}
