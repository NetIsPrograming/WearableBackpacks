package dev.sapphic.wearablebackpacks.client;

import dev.sapphic.wearablebackpacks.inventory.BackpackWearer;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

import static dev.sapphic.wearablebackpacks.BackpackMod.BACKPACK_UPDATED;
import static dev.sapphic.wearablebackpacks.BackpackMod.OPEN_OWN_BACKPACK;

public final class BackpackClientNetwork implements ClientModInitializer {

    private static final PacketByteBuf EMPTY_BUFFER = new PacketByteBuf(Unpooled.EMPTY_BUFFER);

    public static void tryOpenOwnBackpack() {
        ClientPlayNetworking.send(OPEN_OWN_BACKPACK, EMPTY_BUFFER);
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                BACKPACK_UPDATED, (client, handler, buf, sender) -> {
                    final int entityId = buf.readInt();
                    final int openCount = buf.readInt();

                    client.execute(() -> {
                        if (client.world != null) {
                            final @Nullable Entity entity = client.world.getEntityById(entityId);
                            if (entity instanceof LivingEntity) {
                                BackpackWearer.getBackpackState((LivingEntity) entity).count(openCount);
                            }
                        }
                    });
                });
    }
}
