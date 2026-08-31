package atonkish.reinfshulker.block.cauldron;

import java.util.Map;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LayeredCauldronBlock;

import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfshulker.block.ModBlocks;
import atonkish.reinfshulker.block.ReinforcedShulkerBoxBlock;
import atonkish.reinfshulker.item.ModItems;
import atonkish.reinfshulker.stat.ModStats;

// CROSS-REFERENCED against EkagraTheBeast/reinforced-shulker-boxes@26.2 for
// the underlying discovery that cauldron interaction registration in 26.x
// goes through CauldronInteraction.Dispatcher's now-private put(...)
// method, rather than 1.21.11's public
// CauldronBehavior.WATER_CAULDRON_BEHAVIOR map field. Unlike their fork,
// this widens the method via the access widener (see
// reinfshulker.accesswidener) instead of using reflection -- both achieve
// the same result, but the access widener keeps this consistent with the
// rest of the project and avoids a reflective call on every mod
// initialization. UNVERIFIED against the actual 26.1.2 jar.
public class ModCauldronBehavior {
  private static final CauldronInteraction CLEAN_REINFORCED_SHULKER_BOX =
      (state, world, pos, player, hand, stack) -> {
        net.minecraft.world.level.block.Block block =
            net.minecraft.world.level.block.Block.byItem(stack.getItem());
        if (!(block instanceof ReinforcedShulkerBoxBlock reinforcedBlock)) {
          return InteractionResult.TRY_WITH_EMPTY_HAND;
        } else {
          if (!world.isClientSide()) {
            ReinforcingMaterial material = reinforcedBlock.getMaterial();
            player.setItemInHand(
                hand,
                stack.transmuteCopy(
                    ModBlocks.REINFORCED_SHULKER_BOX_MAP.get(material).get((DyeColor) null), 1));
            player.awardStat(ModStats.CLEAN_REINFORCED_SHULKER_BOX_MAP.get(material));
            LayeredCauldronBlock.lowerFillLevel(state, world, pos);
          }

          return InteractionResult.SUCCESS;
        }
      };

  public static void init() {
    for (Map<DyeColor, Item> materialShulkerBoxMap : ModItems.REINFORCED_SHULKER_BOX_MAP.values()) {
      for (DyeColor color : DyeColor.values()) {
        Item item = materialShulkerBoxMap.get(color);
        if (item != null) {
          ((CauldronInteraction.Dispatcher) CauldronInteractions.WATER)
              .put(item, CLEAN_REINFORCED_SHULKER_BOX);
        }
      }

      Item plainItem = materialShulkerBoxMap.get((DyeColor) null);
      if (plainItem != null) {
        ((CauldronInteraction.Dispatcher) CauldronInteractions.WATER)
            .put(plainItem, CLEAN_REINFORCED_SHULKER_BOX);
      }
    }
  }
}
