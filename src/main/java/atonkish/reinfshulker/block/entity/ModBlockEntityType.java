package atonkish.reinfshulker.block.entity;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfshulker.block.ModBlocks;
import atonkish.reinfshulker.mixin.BlockEntityTypeAccessor;

public class ModBlockEntityType {
  public static final Map<ReinforcingMaterial, BlockEntityType<ReinforcedShulkerBoxBlockEntity>>
      REINFORCED_SHULKER_BOX_MAP = new LinkedHashMap<>();

  // CONFIRMED pattern (registration mechanics, imports, Registry.register call
  // shape) via decompiled reinforced-chests-4.0.9+26.1.2.jar
  // atonkish.reinfchest.block.entity.ModBlockEntityType and
  // reinforced-barrels-2.7.6+26.1.2.jar
  // atonkish.reinfbarrel.block.entity.ModBlockEntityType: both sibling mods
  // replaced the old 1.21.11 BlockEntityTypeInvoker (@Invoker into
  // BlockEntityType.create) with Fabric API's FabricBlockEntityTypeBuilder,
  // which needs no access widener entry and accepts a Block... varargs
  // directly. Registries -> net.minecraft.core.registries.BuiltInRegistries
  // and Identifier -> net.minecraft.resources.Identifier are also confirmed
  // via the same decompiled classes.
  public static BlockEntityType<ReinforcedShulkerBoxBlockEntity> registerMaterial(
      String namespace, ReinforcingMaterial material) {
    if (!REINFORCED_SHULKER_BOX_MAP.containsKey(material)) {
      String id = material.getName() + "_shulker_box";
      Collection<Block> blocks = ModBlocks.REINFORCED_SHULKER_BOX_MAP.get(material).values();
      Identifier identifier = Identifier.fromNamespaceAndPath(namespace, id);

      BlockEntityType<ReinforcedShulkerBoxBlockEntity> blockEntityType =
          (BlockEntityType<ReinforcedShulkerBoxBlockEntity>)
              Registry.register(
                  BuiltInRegistries.BLOCK_ENTITY_TYPE,
                  identifier,
                  FabricBlockEntityTypeBuilder.create(
                          (blockPos, blockState) ->
                              new ReinforcedShulkerBoxBlockEntity(material, blockPos, blockState),
                          blocks.toArray(new Block[0]))
                      .build());
      REINFORCED_SHULKER_BOX_MAP.put(material, blockEntityType);

      // INFERRED (High confidence, not independently verified against the
      // actual 26.1.2 jar): BlockEntityType.SHULKER_BOX is assumed to keep
      // its vanilla field name. Neither sibling mod touches the shulker box
      // block entity type, so this specific constant name has no direct
      // 26.1.2 evidence -- confirm with IDE Ctrl+Click before shipping.
      ((BlockEntityTypeAccessor) BlockEntityType.SHULKER_BOX).getValidBlocks().addAll(blocks);
    }

    return REINFORCED_SHULKER_BOX_MAP.get(material);
  }
}
