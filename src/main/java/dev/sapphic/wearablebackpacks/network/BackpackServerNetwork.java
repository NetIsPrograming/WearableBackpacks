package dev.sapphic.wearablebackpacks.network;

import dev.sapphic.wearablebackpacks.inventory.BackpackWearer;
import dev.sapphic.wearablebackpacks.BackpackMod;
import dev.sapphic.wearablebackpacks.inventory.WornBackpack;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import static dev.sapphic.wearablebackpacks.BackpackMod.*;

public final class BackpackServerNetwork implements ModInitializer {


    /*public static void backpackUpdated(final LivingEntity entity) {
        final ByteBuf buf = Unpooled.buffer(Integer.BYTES * 2, Integer.BYTES * 2);
        buf.writeInt(entity.getId());
        buf.writeInt(BackpackWearer.getBackpackState(entity).openCount());
        sendToAllPlayers(entity, new PacketByteBuf(buf.asReadOnly()));
    }

    private static void sendToAllPlayers(final LivingEntity entity, final PacketByteBuf buf) {
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayNetworking.send((ServerPlayerEntity) entity, BACKPACK_UPDATED, buf);
        }
        for (final ServerPlayerEntity player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, BACKPACK_UPDATED, buf);
        }
    }*/ // why does this exist again?

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(OPEN_OWN_BACKPACK, (server, player, handler, buf, sender) -> {
            server.execute(() -> {
                ItemStack stack = player.getEquippedStack(EquipmentSlot.CHEST);
                if (stack.getItem() == BackpackMod.ITEM) {
                    player.openHandledScreen(WornBackpack.of(player, stack));
                    BackpackWearer.getBackpackState(player).setOpen(true);
                }
            });
        });

        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            if (entity instanceof ServerPlayerEntity) {
                ByteBuf buf = Unpooled.buffer(Integer.BYTES, Integer.BYTES);
                buf.writeInt(entity.getId());

                ServerPlayNetworking.send(player, BACKPACK_UPDATED, new PacketByteBuf(buf.asReadOnly()));
            }
        });
    }
}
