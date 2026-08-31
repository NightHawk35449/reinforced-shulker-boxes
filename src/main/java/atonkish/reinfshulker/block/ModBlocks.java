package atonkish.reinfshulker.block;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import atonkish.reinfcore.util.ReinforcingMaterial;

// CONFIRMED registration pattern (Registry/BuiltInRegistries/Registries split,
// ResourceKey.create, BlockBehaviour.Properties.setId(key), Registry.register
// call shape) via decompiled reinforced-chests-4.0.9+26.1.2.jar
// atonkish.reinfchest.block.ModBlocks. Extended here to the
// material -> color -> Block map shape that only reinfshulker needs (the
// original 1.21.11 ModBlocks map-of-maps shape is preserved unchanged).
public class ModBlocks {
  public static final Map<ReinforcingMaterial, Map<DyeColor, Block>> REINFORCED_SHULKER_BOX_MAP =
      new LinkedHashMap<>();
  public static final Map<ReinforcingMaterial, Map<DyeColor, BlockBehaviour.Properties>>
      REINFORCED_SHULKER_BOX_SETTINGS_MAP = new LinkedHashMap<>();

  public static Block registerMaterialDyeColor(
      String namespace,
      ReinforcingMaterial material,
      DyeColor color,
      BlockBehaviour.Properties settings) {
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
      String id =
          color == null
              ? material.getName() + "_shulker_box"
              : color.getName() + "_" + material.getName() + "_shulker_box";
      Block block =
          ModBlocks.register(
              Identifier.fromNamespaceAndPath(namespace, id),
              (blockProperties) -> new ReinforcedShulkerBoxBlock(material, color, blockProperties),
              REINFORCED_SHULKER_BOX_SETTINGS_MAP.get(material).get(color));
      REINFORCED_SHULKER_BOX_MAP.get(material).put(color, block);
    }

    return REINFORCED_SHULKER_BOX_MAP.get(material).get(color);
  }

  private static Block register(
      ResourceKey<Block> key,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties settings) {
    Block block = factory.apply(settings.setId(key));
    return Registry.register(BuiltInRegistries.BLOCK, key, block);
  }

  private static Block register(
      Identifier id,
      Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties settings) {
    return register(keyOf(id), factory, settings);
  }

  private static ResourceKey<Block> keyOf(Identifier id) {
    return ResourceKey.create(Registries.BLOCK, id);
  }
}
