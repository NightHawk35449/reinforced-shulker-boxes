package atonkish.reinfshulker.gametest.testcase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;

import atonkish.reinfcore.gametest.TestFunction;
import atonkish.reinfcore.util.ReinforcingMaterials;
import atonkish.reinfshulker.ReinforcedShulkerBoxesMod;
import atonkish.reinfshulker.block.ModBlocks;
import atonkish.reinfshulker.gametest.util.TestIdentifier;

// No chest/barrel gametest analogue (neither sibling has PushReaction.
// DESTROY block-piston interaction to test). Renames applied consistently
// with the rest of this port. context.expectItemAt(item, pos, count) (a
// 3-arg, no-radius overload) has no confirmed 26.1.2 evidence -- only the
// 4-arg radius-based form (context.assertItemEntityCountIs(item, pos,
// radius, count), CONFIRMED via chests' LootTableTests) is verified, so
// this uses that with a 1.0 radius default matching chests' usage. If the
// 3-arg no-radius overload does exist in 26.1.2, either form is fine here.
public class PistonBehaviorTests {
  private static final String TEST_ENVIRONMENT_DEFAULT =
      String.format("%s:piston_behavior/default", ReinforcedShulkerBoxesMod.MOD_ID);
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
                  PistonBehaviorTests.createTest(
                      String.format("Piston breaks %s", block.getName().getString()), block));
            }
          }
        }
      };

  private static TestFunction createTest(String name, Block shulkerBoxBlock) {
    Identifier testIdentifier =
        TestIdentifier.of(ReinforcedShulkerBoxesMod.MOD_ID, PistonBehaviorTests.class, name);

    return new TestFunction(
        testIdentifier,
        PistonBehaviorTests.TEST_ENVIRONMENT_DEFAULT,
        PistonBehaviorTests.TEST_STRUCTURE_EMPTY,
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
          context.setBlock(blockPos, shulkerBoxBlock);
          context.setBlock(blockPos.south(1), Blocks.PISTON);

          // Act
          CompletableFuture<Void> futurePartialAct1 = new CompletableFuture<>();
          CompletableFuture<Void> futurePartialAct2 = new CompletableFuture<>();

          long tickOrigin = 0;
          context.runAtTickTime(
              tickOrigin,
              () -> {
                context.pulseRedstone(blockPos.south(1).above(1), 1);

                futurePartialAct1.complete(null);
              });

          long tickShulkerBoxBreaking = 2;
          context.runAtTickTime(
              tickShulkerBoxBreaking,
              () -> {
                futurePartialAct2.complete(null);
              });

          // Assert
          CompletableFuture.allOf(futurePartialAct1, futurePartialAct2)
              .thenRun(
                  () -> {
                    try {
                      context.assertBlockPresent(Blocks.AIR, blockPos);
                      context.assertItemEntityCountIs(shulkerBoxBlock.asItem(), blockPos, 1.0, 1);
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
