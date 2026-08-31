package atonkish.reinfshulker.item;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import atonkish.reinfcore.item.ModItemGroup;
import atonkish.reinfcore.item.ModItemGroups;
import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfshulker.block.ModBlocks;

// CONFIRMED registration pattern (Registry/BuiltInRegistries/Registries split,
// CreativeModeTabEvents replacing the old Fabric API ItemGroupEvents,
// Item.Properties.setId(key)/useBlockDescriptionPrefix(), BlockItem
// registerBlocks(Item.BY_BLOCK, item)) via decompiled
// reinforced-chests-4.0.9+26.1.2.jar atonkish.reinfchest.item.ModItems.
// ItemGroupEvents -> CreativeModeTabEvents and ModifyEntries -> ModifyOutput
// are also documented Fabric API 26.1 renames (Fabric Docs "Porting to
// Fabric API 26.1"). Extended here to the material -> color -> Item map
// shape only reinfshulker needs; the 1.21.11 map-of-maps shape is preserved.
public class ModItems {
  public static final Map<ReinforcingMaterial, Map<DyeColor, Item>> REINFORCED_SHULKER_BOX_MAP =
      new LinkedHashMap<>();
  public static final Map<ReinforcingMaterial, Map<DyeColor, Item.Properties>>
      REINFORCED_SHULKER_BOX_SETTINGS_MAP = new LinkedHashMap<>();

  public static Item registerMaterialDyeColor(
      ReinforcingMaterial material, DyeColor color, Item.Properties settings) {
    if (!REINFORCED_SHULKER_BOX_SETTINGS_MAP.containsKey(material)) {
      REINFORCED_SHULKER_BOX_SETTINGS_MAP.put(material, new LinkedHashMap<>());
    }

    if (!REINFORCED_SHULKER_BOX_MAP.containsKey(material)) {
      REINFORCED_SHULKER_BOX_MAP.put(material, new LinkedHashMap<>());
    }

    if (!REINFORCED_SHULKER_BOX_SETTINGS_MAP.get(material).containsKey(color)) {
      REINFORCED_SHULKER_BOX_SETTINGS_MAP.get(material).put(color, settings);
    }

    if (!REINFORCED_SHULKER_BOX_MAP.get(material).containsKey(color)) {
      Item item =
          ModItems.register(
              ModBlocks.REINFORCED_SHULKER_BOX_MAP.get(material).get(color),
              REINFORCED_SHULKER_BOX_SETTINGS_MAP.get(material).get(color));

      // CreativeModeTabs.FUNCTIONAL_BLOCKS is CONFIRMED via reinforced-chests'
      // 26.1.2 ModItems. CreativeModeTabs.COLORED_BLOCKS is INFERRED (same
      // ItemGroups.* -> CreativeModeTabs.* class rename applied to the field
      // name that was ItemGroups.COLORED_BLOCKS in 1.21.11) but not
      // independently confirmed -- neither sibling mod registers into the
      // colored-blocks tab. Verify with IDE Ctrl+Click on CreativeModeTabs.
      CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COLORED_BLOCKS)
          .register(output -> output.accept((ItemLike) item));
      CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
          .register(output -> output.accept((ItemLike) item));
      CreativeModeTabEvents.modifyOutputEvent(ModItemGroups.REINFORCED_STORAGE)
          .register(output -> output.accept((ItemLike) item));

      REINFORCED_SHULKER_BOX_MAP.get(material).put(color, item);
    }

    return REINFORCED_SHULKER_BOX_MAP.get(material).get(color);
  }

  public static void registerMaterialDyeColorItemGroupIcon(
      ReinforcingMaterial material, DyeColor color) {
    Item item = REINFORCED_SHULKER_BOX_MAP.get(material).get(color);
    CreativeModeTabEvents.modifyOutputEvent(ModItemGroups.REINFORCED_STORAGE)
        .register(
            output -> {
              CreativeModeTab tab =
                  BuiltInRegistries.CREATIVE_MODE_TAB.getValue(ModItemGroups.REINFORCED_STORAGE);
              ModItemGroup.setIcon(tab, (ItemLike) item);
            });
  }

  private static ResourceKey<Item> keyOf(ResourceKey<Block> blockKey) {
    return ResourceKey.create(Registries.ITEM, blockKey.identifier());
  }

  public static Item register(Block block, Item.Properties settings) {
    return register(block, BlockItem::new, settings);
  }

  private static Item register(
      Block block, BiFunction<Block, Item.Properties, Item> factory, Item.Properties settings) {
    return register(
        keyOf(block.builtInRegistryHolder().key()),
        itemSettings -> factory.apply(block, itemSettings),
        settings.useBlockDescriptionPrefix());
  }

  private static Item register(
      ResourceKey<Item> key, Function<Item.Properties, Item> factory, Item.Properties settings) {
    Item item = factory.apply(settings.setId(key));
    if (item instanceof BlockItem blockItem) {
      blockItem.registerBlocks(Item.BY_BLOCK, item);
    }

    return Registry.register(BuiltInRegistries.ITEM, key, item);
  }
}
