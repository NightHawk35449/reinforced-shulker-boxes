package atonkish.reinfshulker.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfshulker.block.ModBlocks;
import atonkish.reinfshulker.block.entity.ModBlockEntityType;
import atonkish.reinfshulker.block.entity.ReinforcedShulkerBoxBlockEntity;
import atonkish.reinfshulker.item.ModItems;
import atonkish.reinfshulker.stat.ModStats;

// CONFIRMED: pure glue calling into already-ported Mod*.java classes; the
// only vanilla imports are Identifier/DyeColor/Item/Block/BlockEntityType/
// BlockBehaviour.Properties, all already confirmed elsewhere in this port.
// Signature shape cross-checked against
// EkagraTheBeast/reinforced-shulker-boxes@26.2's identical class (same
// method names/params), and against reinforced-barrels' confirmed
// ReinforcedBarrelsRegistry (same registration-forwarding pattern).
public class ReinforcedShulkerBoxesRegistry {
  public static Identifier registerMaterialCleanStat(
      String namespace, ReinforcingMaterial material) {
    return ModStats.registerMaterialClean(namespace, material);
  }

  public static Identifier registerMaterialOpenStat(
      String namespace, ReinforcingMaterial material) {
    return ModStats.registerMaterialOpen(namespace, material);
  }

  public static Block registerMaterialDyeColorBlock(
      String namespace,
      ReinforcingMaterial material,
      DyeColor color,
      BlockBehaviour.Properties settings) {
    return ModBlocks.registerMaterialDyeColor(namespace, material, color, settings);
  }

  public static BlockEntityType<ReinforcedShulkerBoxBlockEntity> registerMaterialBlockEntityType(
      String namespace, ReinforcingMaterial material) {
    return ModBlockEntityType.registerMaterial(namespace, material);
  }

  public static Item registerMaterialDyeColorItem(
      String namespace, ReinforcingMaterial material, DyeColor color, Item.Properties settings) {
    return ModItems.registerMaterialDyeColor(material, color, settings);
  }

  public static void registerMaterialDyeColorItemGroupIcon(
      String namespace, ReinforcingMaterial material, DyeColor color) {
    ModItems.registerMaterialDyeColorItemGroupIcon(material, color);
  }
}
