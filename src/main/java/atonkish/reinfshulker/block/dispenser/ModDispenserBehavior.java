package atonkish.reinfshulker.block.dispenser;

import java.util.Map;

import net.minecraft.core.dispenser.ShulkerBoxDispenseBehavior;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;

import atonkish.reinfshulker.item.ModItems;

// CONFIRMED: net.minecraft.core.dispenser.ShulkerBoxDispenseBehavior
// compiles against the actual 26.1.2 jar, and dispenser/dropper placement
// of reinforced shulker boxes has been runtime-tested and works correctly.
public interface ModDispenserBehavior {
  public static void init() {
    for (Map<DyeColor, Item> materialShulkerBoxMap : ModItems.REINFORCED_SHULKER_BOX_MAP.values()) {
      for (Item item : materialShulkerBoxMap.values()) {
        DispenserBlock.registerBehavior(item, new ShulkerBoxDispenseBehavior());
      }
    }
  }
}
