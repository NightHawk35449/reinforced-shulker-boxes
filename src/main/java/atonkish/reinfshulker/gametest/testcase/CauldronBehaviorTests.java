package atonkish.reinfshulker.gametest.testcase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

import atonkish.reinfcore.gametest.TestFunction;
import atonkish.reinfcore.util.ReinforcingMaterials;
import atonkish.reinfshulker.ReinforcedShulkerBoxesMod;
import atonkish.reinfshulker.block.ModBlocks;
import atonkish.reinfshulker.block.ReinforcedShulkerBoxBlock;
import atonkish.reinfshulker.gametest.util.MockServerPlayerHelper;
import atonkish.reinfshulker.gametest.util.TestIdentifier;
import atonkish.reinfshulker.item.ModItems;
import atonkish.reinfshulker.stat.ModStats;

// No chest/barrel gametest analogue (neither sibling registers cauldron
// interactions). Confirmed renames applied consistently with the rest of
// this port: Stats.CUSTOM.getOrCreateStat -> Stats.CUSTOM.get,
// context.assertEquals -> context.assertValueEqual, getStatHandler()
// .getStat(stat) -> getStats().getValue(stat), player.getMainHandStack() ->
// player.getMainHandItem() (Mojang naming consistent with the confirmed
// setItemInHand/getItemInHand rename), player.setStackInHand ->
// player.setItemInHand, Hand -> InteractionHand.

// INFERRED (moderate confidence, not confirmed by any sibling): the
// vanilla cauldron fluid-level property Properties.LEVEL_3 is accessed
// here as LayeredCauldronBlock.LEVEL directly on the block class, matching
// this port's own ModCauldronBehavior (which calls
// LayeredCauldronBlock.lowerFillLevel(state, world, pos)) rather than a
// generic BlockStateProperties constant -- verify against the actual
// 26.1.2 LayeredCauldronBlock source if this doesn't compile.
public class CauldronBehaviorTests {
  private static final String TEST_ENVIRONMENT_DEFAULT =
      String.format("%s:cauldron_behavior/default", ReinforcedShulkerBoxesMod.MOD_ID);
  private static final String TEST_STRUCTURE_EMPTY = "fabric-gametest-api-v1:empty";

  private static final String[] MATERIAL_NAMES = {"copper", "iron", "gold", "diamond", "netherite"};

  public static final Collection<TestFunction> TEST_FUNCTIONS =
      new ArrayList<>() {
        {
          for (String materialName : MATERIAL_NAMES) {
            for (DyeColor color : DyeColor.values()) {
              ReinforcedShulkerBoxBlock shulkerBoxBlock =
                  (ReinforcedShulkerBoxBlock)
                      ModBlocks.REINFORCED_SHULKER_BOX_MAP
                          .get(ReinforcingMaterials.MAP.get(materialName))
                          .get(color);

              add(
                  CauldronBehaviorTests.createTest(
                      String.format("Clean %s", shulkerBoxBlock.getName().getString()),
                      shulkerBoxBlock));
            }
          }
        }
      };

  private static TestFunction createTest(String name, ReinforcedShulkerBoxBlock shulkerBoxBlock) {
    Identifier testIdentifier =
        TestIdentifier.of(ReinforcedShulkerBoxesMod.MOD_ID, CauldronBehaviorTests.class, name);

    return new TestFunction(
        testIdentifier,
        CauldronBehaviorTests.TEST_ENVIRONMENT_DEFAULT,
        CauldronBehaviorTests.TEST_STRUCTURE_EMPTY,
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
              Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));

          ServerPlayer player =
              MockServerPlayerHelper.spawn(
                  context, GameType.SURVIVAL, Vec3.atCenterOf((Vec3i) blockPos.south(4)));
          player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(shulkerBoxBlock.asItem()));

          Stat<Identifier> stat =
              Stats.CUSTOM.get(
                  ModStats.CLEAN_REINFORCED_SHULKER_BOX_MAP.get(shulkerBoxBlock.getMaterial()));

          // Act
          CompletableFuture<Void> futurePartialAct1 = new CompletableFuture<>();
          CompletableFuture<Void> futurePartialAct2 = new CompletableFuture<>();

          Map<String, Integer> statMap = new HashMap<String, Integer>();
          String statMapKeyBeforeCleaning = "beforeCleaning";
          String statMapKeyAfterCleaning = "afterCleaning";

          long tickOrigin = 0;
          context.runAtTickTime(
              tickOrigin,
              () -> {
                statMap.put(statMapKeyBeforeCleaning, player.getStats().getValue(stat));

                context.useBlock(blockPos, (Player) player);

                futurePartialAct1.complete(null);
              });

          long tickShulkerBoxCleaning = 1;
          context.runAtTickTime(
              tickShulkerBoxCleaning,
              () -> {
                statMap.put(statMapKeyAfterCleaning, player.getStats().getValue(stat));

                futurePartialAct2.complete(null);
              });

          // Assert
          CompletableFuture.allOf(futurePartialAct1, futurePartialAct2)
              .thenRun(
                  () -> {
                    try {
                      context.assertValueEqual(
                          player.getMainHandItem().getItem(),
                          ModItems.REINFORCED_SHULKER_BOX_MAP
                              .get(shulkerBoxBlock.getMaterial())
                              .get((DyeColor) null),
                          Component.literal("main hand item"));
                      context.assertValueEqual(
                          context.getBlockState(blockPos).getValue(LayeredCauldronBlock.LEVEL),
                          2,
                          Component.literal("fluid level"));
                      context.assertValueEqual(
                          statMap.get(statMapKeyAfterCleaning)
                              - statMap.get(statMapKeyBeforeCleaning),
                          1,
                          Component.literal(String.format("diff %s value", stat.getName())));
                    } catch (Exception e) {
                      ReinforcedShulkerBoxesMod.LOGGER.error(
                          "[{}] {}", testIdentifier, e.getMessage());
                      throw e;
                    } finally {
                      MockServerPlayerHelper.destroy(context, player);
                    }

                    context.succeed();
                  });
        });
  }
}
