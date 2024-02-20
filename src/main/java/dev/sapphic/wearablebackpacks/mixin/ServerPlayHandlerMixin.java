package dev.sapphic.wearablebackpacks.mixin;

//@Mixin(ServerPlayNetworkHandler.class)
abstract class ServerPlayHandlerMixin {
    // what was this supposed to do?
    /*@Inject(
            method = "onCreativeInventoryAction(Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;)V",
            at = @At(shift = At.Shift.AFTER, value = "INVOKE_ASSIGN", opcode = Opcodes.INVOKEVIRTUAL,
                    target = "Lnet/minecraft/block/entity/BlockEntity;writeNbt(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/nbt/NbtCompound;"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            require = 1, allow = 1)
    private void extractEnchantments(
            final CreativeInventoryActionC2SPacket packet, final CallbackInfo ci, final boolean drop, final ItemStack stack,
            final NbtCompound stackNbt, final BlockPos pos, final BlockEntity be, final NbtCompound blockNbt
    ) {
        if ((be instanceof BackpackBlockEntity) && blockNbt.contains("Enchantments", NbtType.LIST)) {
            stack.getSubNbt("Enchantments", blockNbt.getList("Enchantments", NbtType.COMPOUND));
            blockNbt.remove("Enchantments");
        }
    }*/
}
