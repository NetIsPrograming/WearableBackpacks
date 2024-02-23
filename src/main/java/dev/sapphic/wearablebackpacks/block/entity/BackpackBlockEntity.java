package dev.sapphic.wearablebackpacks.block.entity;

import dev.sapphic.wearablebackpacks.BackpackLidAnimator;
import dev.sapphic.wearablebackpacks.inventory.Backpack;
import dev.sapphic.wearablebackpacks.BackpackOptions;
import dev.sapphic.wearablebackpacks.BackpackMod;
import dev.sapphic.wearablebackpacks.inventory.BackpackContainer;
import dev.sapphic.wearablebackpacks.inventory.BackpackScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LidOpenable;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BackpackBlockEntity extends LootableContainerBlockEntity implements Backpack, BackpackContainer, ScreenHandlerFactory, LidOpenable {

    private static final int OPENS_DATA_TYPE = 0x0;
    private static final int COLOR_DATA_TYPE = 0x1;
    private static final int EMPTY_FLAG_TYPE = 0x2;
    private static final int GLINT_FLAG_TYPE = 0x3;

    private static final int DEFAULT_COLOR = 0xA06540;
    // private static final int NO_COLOR = 0xFFFFFF + 1; // why have a no color state when you could just have a boolean?
    private static boolean colored = false;

    //private final BackpackLid lid = new BackpackLid(o -> this.event(OPENS_DATA_TYPE, o.openCount()));

    private final BackpackLidAnimator lid = new BackpackLidAnimator();

    private int rows = BackpackOptions.DEFAULT_ROWS;
    private int columns = BackpackOptions.DEFAULT_COLUMNS;
    private @MonotonicNonNull DefaultedList<ItemStack> contents;
    private @Nullable NbtList enchantments;

    private int color = DEFAULT_COLOR;

    private boolean empty;
    private boolean enchanted;

    public BackpackBlockEntity(BlockPos pos, BlockState state) {
        super(BackpackMod.BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BackpackBlockEntity bbe) {
        bbe.lid.step();
    }

    @Override
    public int getRows() {
        return this.rows;
    }

    @Override
    public int getColumns() {
        return this.columns;
    }

    @Override
    public boolean hasGlint() {
        return this.enchanted;
    }

    @Override
    public int getColor() {
        if (!colored) {
            return DEFAULT_COLOR;
        }
        return this.color;
    }

    @Override
    public boolean hasColor() {
        return colored;
    }

    @Override
    public void setColor(final int color) {
        if (this.color != (color & 0xFFFFFF)) {
            colored = true;
            this.color = color & 0xFFFFFF;
            this.event(COLOR_DATA_TYPE, this.color);
            this.markDirty();
        }
    }

    @Override
    public void clearColor() {
        if (colored) {
            this.color = DEFAULT_COLOR;
            this.event(COLOR_DATA_TYPE, DEFAULT_COLOR);
            this.markDirty();
        }
        colored = false;
    }

    @Override
    public DefaultedList<ItemStack> getContents() {
        return this.contents;
    }

    @Override
    public float getLidDelta(float tickDelta) {
        return this.lid.getProgress(tickDelta);
    }

    @Override
    public boolean isEmpty() {
        return this.empty;
    }

    @Override
    public ItemStack removeStack(final int slot, final int amount) {
        final ItemStack stack = super.removeStack(slot, amount);
        this.updateEmptyState();
        return stack;
    }

    @Override
    public ItemStack removeStack(final int slot) {
        final ItemStack stack = super.removeStack(slot);
        this.updateEmptyState();
        return stack;
    }

    @Override
    public void setStack(final int slot, final ItemStack stack) {
        super.setStack(slot, stack);
        this.updateEmptyState();
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return this.contents;
    }

    @Override
    protected void setInvStackList(final DefaultedList<ItemStack> list) {
        throw new UnsupportedOperationException();
    }

    public void readFromStack(final ItemStack stack) {
        this.rows = Backpack.getRows(stack);
        this.columns = Backpack.getColumns(stack);
        this.contents = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        final @Nullable NbtCompound tag = stack.getSubNbt("BlockEntityTag");
        if (tag != null) {
            Inventories.readNbt(tag, this.contents);
        }
        if (stack.hasEnchantments()) {
            this.enchantments = stack.getEnchantments();
        }
        if (Backpack.hasColor(stack)) {
            this.color = Backpack.getColor(stack);
        }
        this.enchanted = this.enchantments != null;
        this.empty = super.isEmpty();
        this.event(COLOR_DATA_TYPE, this.color);
        this.event(GLINT_FLAG_TYPE, this.enchanted ? 1 : 0);
        this.event(EMPTY_FLAG_TYPE, this.empty ? 1 : 0);
        this.markDirty();
    }

    public void writeToStack(final ItemStack stack) {
        if (this.hasColor()) {
            Backpack.setColor(stack, this.color);
        }
        if (this.enchantments != null) {
            stack.getNbt().put("Enchantments", this.enchantments);
        }
        final NbtCompound tag = stack.getOrCreateNbt().getCompound("BlockEntityTag");
        tag.putInt("Rows", this.rows);
        tag.putInt("Columns", this.columns);
    }

    @Override
    public void readNbt(final NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("Rows", NbtElement.INT_TYPE)) {
            this.rows = BackpackOptions.getRows(nbt.getInt("Rows"));
        }
        if (nbt.contains("Columns", NbtElement.INT_TYPE)) {
            this.columns = BackpackOptions.getColumns(nbt.getInt("Columns"));
        }
        this.contents = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(nbt)) {
            Inventories.readNbt(nbt, this.contents);
        }
        this.empty = super.isEmpty();
        if (nbt.contains("Color", NbtElement.INT_TYPE)) {
            this.color = nbt.getInt("Color") & 0xFFFFFF;
        }
        if (nbt.contains("Enchantments", NbtElement.LIST_TYPE)) {
            this.enchantments = nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
            this.enchanted = true;
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt(Backpack.ROWS, this.rows);
        nbt.putInt(Backpack.COLUMNS, this.columns);
        if (this.contents != null) {
            if (!this.serializeLootTable(nbt)) {
                Inventories.writeNbt(nbt, this.contents);
            }
        }
        if (this.hasColor()) {
            nbt.putInt("Color", this.color);
        }
        if (this.enchantments != null) {
            nbt.put("Enchantments", this.enchantments);
        }
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container." + BackpackMod.MOD_ID);
    }

    @Override
    protected ScreenHandler createScreenHandler(final int id, final PlayerInventory inventory) {
        return new BackpackScreenHandler(id, inventory, this);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        //if (this.hasColor()) {
        //    tag.putInt("Color", this.color);
        //}
        //tag.putBoolean("Empty", this.empty);
        //tag.putBoolean("Enchanted", this.enchanted);
        //return tag;
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        //if (tag.contains("Color", NbtType.INT)) {
        //    this.color = tag.getInt("Color") & 0xFFFFFF;
        //}
        //this.empty = tag.getBoolean("Empty");
        //this.enchanted = tag.getBoolean("Enchanted");
        return createNbt();
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == OPENS_DATA_TYPE) {
            this.lid.setOpen(data > 0);
            return true;
        }
        if (type == COLOR_DATA_TYPE) {
            if (data == DEFAULT_COLOR) {
                this.color = DEFAULT_COLOR;
            } else {
                this.color = data & 0xFFFFFF;
            }
            return true;
        }
        if (type == EMPTY_FLAG_TYPE) {
            this.empty = data == 1;
            return true;
        }
        if (type == GLINT_FLAG_TYPE) {
            this.enchanted = data == 1;
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return this.rows * this.columns;
    }

    @Override
    public void onOpen(final PlayerEntity player) {
        if ((this.world != null) && !player.isSpectator()) {
            this.lid.setOpen(true);
            if (this.lid.getOpen()) {
                this.world.playSound(null, this.pos, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                        SoundCategory.BLOCKS, 0.5F, (this.world.random.nextFloat() * 0.1F) + 0.9F
                );
            }
        }
    }

    @Override
    public void onClose(final PlayerEntity player) {
        if ((this.world != null) && !player.isSpectator()) {
            this.lid.setOpen(false);
            if (!this.lid.getOpen()) {
                this.world.playSound(null, this.pos, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                        SoundCategory.BLOCKS, 0.5F, (this.world.random.nextFloat() * 0.1F) + 0.9F
                );
            }
        }
    }

    private void updateEmptyState() {
        this.empty = super.isEmpty();
        this.event(EMPTY_FLAG_TYPE, this.empty ? 1 : 0);
    }

    private void event(final int type, final int data) {
        if (this.world instanceof ServerWorld) {
            this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), type, data);
        }
    }

    @Override
    public float getAnimationProgress(float tickDelta) {
        return lid.getProgress(tickDelta);
    }
}
