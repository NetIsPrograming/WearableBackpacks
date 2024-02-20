package dev.sapphic.wearablebackpacks;

import dev.sapphic.wearablebackpacks.block.BackpackBlock;
import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import dev.sapphic.wearablebackpacks.inventory.BackpackScreenHandler;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import dev.sapphic.wearablebackpacks.recipe.BackpackDyeingRecipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class BackpackMod implements ModInitializer {
    public static final String ID = "wearablebackpacks";

    private static final Identifier backpack = new Identifier(ID, "backpack");

    public static final Block BLOCK = new BackpackBlock(FabricBlockSettings.create().mapColor(MapColor.CLEAR).strength(0.5F, 0.5F).sounds(BlockSoundGroup.WOOL));
    public static final BlockEntityType<BackpackBlockEntity> BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(BackpackBlockEntity::new, BLOCK).build();
    public static final Item ITEM = new BackpackItem(BLOCK, new Item.Settings());

    public static final ScreenHandlerType<BackpackScreenHandler> BACKPACK_SCREEN_HANDLER = new ScreenHandlerType<>(BackpackScreenHandler::new, FeatureFlags.VANILLA_FEATURES);

    public static final Identifier OPEN_OWN_BACKPACK = new Identifier(ID, "open_own_backpack");

    public static final Identifier BACKPACK_UPDATED = new Identifier(ID, "backpack_updated");

    @Override
    public void onInitialize() {
        BackpackOptions.init(FabricLoader.getInstance().getConfigDir().resolve(ID + ".json"));

        Registry.register(Registries.BLOCK, backpack, BLOCK);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, backpack, BLOCK_ENTITY);
        Registry.register(Registries.ITEM, backpack, ITEM);
        //  Too hacky
        //  Item.BLOCK_ITEMS.put(BLOCK, ITEM);

        Registry.register(Registries.SCREEN_HANDLER, backpack, BACKPACK_SCREEN_HANDLER);

        Registry.register(Registries.RECIPE_SERIALIZER, BackpackDyeingRecipe.ID, BackpackDyeingRecipe.SERIALIZER);
    }
}
