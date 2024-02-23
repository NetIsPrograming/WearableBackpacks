package dev.sapphic.wearablebackpacks.client.network;

import dev.sapphic.wearablebackpacks.inventory.BackpackWearer;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.checkerframework.checker.nullness.qual.Nullable;

import static dev.sapphic.wearablebackpacks.BackpackMod.*;
import static dev.sapphic.wearablebackpacks.BackpackMod.LOGGER;

public final class BackpackClientNetwork implements ClientModInitializer {

    private static final PacketByteBuf EMPTY_BUFFER = new PacketByteBuf(Unpooled.EMPTY_BUFFER);

    public static void tryOpenOwnBackpack() {
        ClientPlayNetworking.send(OPEN_OWN_BACKPACK, EMPTY_BUFFER);
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                BACKPACK_UPDATED, (client, handler, buf, sender) -> {
                    LOGGER.info(client.toString());
                    LOGGER.info(handler.toString());
                    LOGGER.info(buf.toString());
                    LOGGER.info(sender.toString());

                    int entityId = buf.readInt();


                    client.execute(() -> {
                        if (client.world != null) {
                            @Nullable
                            Entity entity = client.world.getEntityById(entityId);
                            if (entity instanceof ServerPlayerEntity) {

                                // I don't know why the backpack is purple all the time and I dont know why the lid isn't working
                            }
                        }
                    });
                }
        );
    }
}
