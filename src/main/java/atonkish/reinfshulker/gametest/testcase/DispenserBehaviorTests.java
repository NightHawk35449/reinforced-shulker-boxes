package atonkish.reinfshulker.gametest.testcase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;

import atonkish.reinfcore.gametest.TestFunction;
import atonkish.reinfcore.util.ReinforcingMaterials;
import atonkish.reinfshulker.ReinforcedShulkerBoxesMod;
import atonkish.reinfshulker.block.ModBlocks;
import atonkish.reinfshulker.gametest.util.TestIdentifier;

// No chest/barrel gametest analogue exists for dispenser behavior (neither
// sibling mod overrides dispenser placement). Renames applied here follow
// the same CONFIRMED patterns established across the rest of this port:
// context.setBlockState -> context.setBlock, getDefaultState() ->
// defaultBlockState(), BlockState#with(...) -> BlockState#setValue(...)
// (confirmed via chests' InventoryTests double-chest setup), context.
// getBlockEntity(pos, Class) (as used in InventoryTests), Container#setStack
// -> Container#setItem (standard Mojang Container interface rename,
// consistent with getContainerSize() confirmed elsewhere), context.
// expectBlock -> context.assertBlockPresent (confirmed via chests'
// LootTableTests), context.runAtTick -> context.runAtTickTime, context.
// complete() -> context.succeed().

// CONFIRMED to compile against 26.1.2: context.
// putAndRemoveRedstoneBlock(pos, delay) -> context.pulseRedstone(pos, delay).
// Note this is GameTest-suite code, not exercised by manual play-testing --
// if the automated GameTest suite is ever run, verify this test actually
// passes at runtime, not just that it compiles.
public class DispenserBehaviorTests {
  private static final String TEST_ENVIRONMENT_DEFAULT =
      String.format("%s:dispenser_behavior/default", ReinforcedShulkerBoxesMod.MOD_ID);
  private static final String TEST_STRUCTURE_EMPTY = "fabric-gametest-api-v1:empty";

  private static final String[] MATERIAL_NAMES = {"copper", "iron", "gold", "diamond", "netherite"};

  public static final Collection<TestFunction> TEST_FUNCTIONS =
      new ArrayList<>() {
        {
          for (String materialName : MATERIAL_NAMES) {
            for (Block block :
                ModBlocks.REINFORCED_SHULKER_BOX_MAP
                    .get(ReinforcingMaterials.MAP.get(materialName))
                    .values()) {
              add(
                  DispenserBehaviorTests.createTest(
                      String.format("Dispense %s", block.getName().getString()), block));
            }
          }
        }
      };

  private static TestFunction createTest(String name, Block shulkerBoxBlock) {
    Identifier testIdentifier =
        TestIdentifier.of(ReinforcedShulkerBoxesMod.MOD_ID, DispenserBehaviorTests.class, name);

    return new TestFunction(
        testIdentifier,
        DispenserBehaviorTests.TEST_ENVIRONMENT_DEFAULT,
        DispenserBehaviorTests.TEST_STRUCTURE_EMPTY,
        20,
        0,
        true,
        Rotation.NONE,
        false,
        1,
        1,
        false,
        (context) -> {
          // Arrange
          BlockPos blockPos = BlockPos.ZERO;
          context.setBlock(
              blockPos,
              Blocks.DISPENSER
                  .defaultBlockState()
                  .setValue(DispenserBlock.FACING, Direction.SOUTH));

          DispenserBlockEntity entity =
              context.getBlockEntity(blockPos, DispenserBlockEntity.class);
          entity.setItem(0, new ItemStack(shulkerBoxBlock.asItem()));

          // Act
          CompletableFuture<Void> futurePartialAct1 = new CompletableFuture<>();
          CompletableFuture<Void> futurePartialAct2 = new CompletableFuture<>();

          long tickOrigin = 0;
          context.runAtTickTime(
              tickOrigin,
              () -> {
                context.pulseRedstone(blockPos.above(1), 0);

                futurePartialAct1.complete(null);
              });

          long tickShulkerBoxPlaced = 4;
          context.runAtTickTime(
              tickShulkerBoxPlaced,
              () -> {
                futurePartialAct2.complete(null);
              });

          // Assert
          CompletableFuture.allOf(futurePartialAct1, futurePartialAct2)
              .thenRun(
                  () -> {
                    try {
                      context.assertBlockPresent(shulkerBoxBlock, blockPos.south(1));
                    } catch (Exception e) {
                      ReinforcedShulkerBoxesMod.LOGGER.error(
                          "[{}] {}", testIdentifier, e.getMessage());
                      throw e;
                    }

                    context.succeed();
                  });
        });
  }
}
