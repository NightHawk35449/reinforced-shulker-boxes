package atonkish.reinfshulker.gametest.testcase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

import atonkish.reinfchest.ReinforcedChestsMod;
import atonkish.reinfcore.gametest.TestFunction;
import atonkish.reinfcore.util.ReinforcingMaterials;
import atonkish.reinfshulker.ReinforcedShulkerBoxesMod;
import atonkish.reinfshulker.gametest.util.MockServerPlayerHelper;
import atonkish.reinfshulker.gametest.util.TestIdentifier;
import atonkish.reinfshulker.item.ModItems;

// CONFIRMED (advancement/player plumbing) against decompiled
// reinforced-chests-4.0.9+26.1.2.jar's AdvancementTests: AdvancementEntry
// -> AdvancementHolder, getAdvancementLoader() -> getAdvancements(),
// getAdvancementTracker().getProgress(entry) ->
// getAdvancements().getOrStartProgress(entry), player.giveItemStack(...) ->
// player.getInventory().add(...), context.runAtTick -> runAtTickTime,
// context.complete() -> context.succeed(), Vec3d.of(BlockPos.ORIGIN) ->
// Vec3.atLowerCornerOf(BlockPos.ZERO) (non-centered conversion, matching
// the original's exact (0,0,0) spawn position rather than a centered
// variant), Identifier.of -> Identifier.fromNamespaceAndPath. item.getName()
// .getString() -> Component.translatable(item.getDescriptionId())
// .getString() (matches chests' confirmed usage exactly).
public class AdvancementTests {
  private static final Map<DyeColor, Item> SHULKER_BOX_MAP =
      new LinkedHashMap<>() {
        {
          put((DyeColor) null, Items.SHULKER_BOX);
          put(DyeColor.WHITE, Items.WHITE_SHULKER_BOX);
          put(DyeColor.ORANGE, Items.ORANGE_SHULKER_BOX);
          put(DyeColor.MAGENTA, Items.MAGENTA_SHULKER_BOX);
          put(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_SHULKER_BOX);
          put(DyeColor.YELLOW, Items.YELLOW_SHULKER_BOX);
          put(DyeColor.LIME, Items.LIME_SHULKER_BOX);
          put(DyeColor.PINK, Items.PINK_SHULKER_BOX);
          put(DyeColor.GRAY, Items.GRAY_SHULKER_BOX);
          put(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_SHULKER_BOX);
          put(DyeColor.CYAN, Items.CYAN_SHULKER_BOX);
          put(DyeColor.PURPLE, Items.PURPLE_SHULKER_BOX);
          put(DyeColor.BLUE, Items.BLUE_SHULKER_BOX);
          put(DyeColor.BROWN, Items.BROWN_SHULKER_BOX);
          put(DyeColor.GREEN, Items.GREEN_SHULKER_BOX);
          put(DyeColor.RED, Items.RED_SHULKER_BOX);
          put(DyeColor.BLACK, Items.BLACK_SHULKER_BOX);
        }
      };

  private static final String TEST_ENVIRONMENT_DEFAULT =
      String.format("%s:advancement/default", ReinforcedShulkerBoxesMod.MOD_ID);
  private static final String TEST_STRUCTURE_EMPTY = "fabric-gametest-api-v1:empty";

  // Tier chain: null == the base vanilla-shulker-box tier feeding "copper".
  private static final String[] TIER_MATERIAL_NAMES = {
    "copper", "iron", "gold", "diamond", "netherite"
  };
  private static final Item[] TIER_INGOTS = {
    Items.COPPER_INGOT, Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND, Items.NETHERITE_INGOT
  };
  private static final String[] TIER_DISPLAY_NAMES = {
    "Copper Shulker Box",
    "Iron Shulker Box",
    "Gold Shulker Box",
    "Diamond Shulker Box",
    "Netherite Shulker Box"
  };

  public static final Collection<TestFunction> TEST_FUNCTIONS =
      new ArrayList<>() {
        {
          for (int tierIndex = 0; tierIndex < TIER_MATERIAL_NAMES.length; tierIndex++) {
            String tierName = TIER_MATERIAL_NAMES[tierIndex];
            String displayName = TIER_DISPLAY_NAMES[tierIndex];
            boolean isSmithingTier = tierName.equals("netherite");
            String advancementId =
                isSmithingTier
                    ? "recipes/decorations/netherite_shulker_box_smithing"
                    : String.format("recipes/decorations/%s_shulker_box", tierName);

            Map<DyeColor, Item> baseTierItems =
                tierIndex == 0
                    ? SHULKER_BOX_MAP
                    : ModItems.REINFORCED_SHULKER_BOX_MAP.get(
                        ReinforcingMaterials.MAP.get(TIER_MATERIAL_NAMES[tierIndex - 1]));

            // Obtain by having any color of the previous tier.
            for (Item item : baseTierItems.values()) {
              add(
                  AdvancementTests.createTest(
                      String.format(
                          "Obtain %s recipe advancement by having %s",
                          displayName, Component.translatable(item.getDescriptionId()).getString()),
                      item,
                      Identifier.fromNamespaceAndPath(
                          ReinforcedShulkerBoxesMod.MOD_ID, advancementId)));
            }

            // Obtain by having the raw crafting/smithing ingredient.
            add(
                AdvancementTests.createTest(
                    String.format(
                        "Obtain %s recipe advancement by having %s",
                        displayName,
                        Component.translatable(TIER_INGOTS[tierIndex].getDescriptionId())
                            .getString()),
                    TIER_INGOTS[tierIndex],
                    Identifier.fromNamespaceAndPath(
                        ReinforcedShulkerBoxesMod.MOD_ID, advancementId)));

            // Obtain by having the equivalent reinforced-chests tier
            // (backward compatible upgrade path).
            String fromChestAdvancementId =
                tierIndex == 0
                    ? "recipes/decorations/copper_shulker_box_from_reinforced_copper_chest"
                    : String.format(
                        "recipes/decorations/%s_shulker_box_from_%s_chest",
                        tierName, TIER_MATERIAL_NAMES[tierIndex - 1]);
            add(
                AdvancementTests.createTest(
                    String.format(
                        "Obtain %s recipe advancement by having Reinforced %s Chest",
                        displayName,
                        tierIndex == 0
                            ? "Copper"
                            : TIER_DISPLAY_NAMES[tierIndex - 1].split(" ")[0]),
                    atonkish.reinfchest.item.ModItems.REINFORCED_CHEST_MAP.get(
                        ReinforcingMaterials.MAP.get(
                            tierIndex == 0 ? "copper" : TIER_MATERIAL_NAMES[tierIndex - 1])),
                    Identifier.fromNamespaceAndPath(
                        ReinforcedShulkerBoxesMod.MOD_ID, fromChestAdvancementId)));

            // Copper tier only: also obtainable from the whole
            // copper-chest-family (weathering states).
            if (tierIndex == 0) {
              for (Item item :
                  List.of(
                      Items.COPPER_CHEST,
                      Items.EXPOSED_COPPER_CHEST,
                      Items.WEATHERED_COPPER_CHEST,
                      Items.OXIDIZED_COPPER_CHEST,
                      Items.WAXED_COPPER_CHEST,
                      Items.WAXED_EXPOSED_COPPER_CHEST,
                      Items.WAXED_WEATHERED_COPPER_CHEST,
                      Items.WAXED_OXIDIZED_COPPER_CHEST)) {
                add(
                    AdvancementTests.createTest(
                        String.format(
                            "Obtain Copper Shulker Box recipe advancement by having %s",
                            Component.translatable(item.getDescriptionId()).getString()),
                        item,
                        Identifier.fromNamespaceAndPath(
                            ReinforcedChestsMod.MOD_ID,
                            "recipes/decorations/iron_chest_from_copper_chests")));
              }
            }
          }
        }
      };

  private static TestFunction createTest(String name, Item item, Identifier advancementId) {
    Identifier testIdentifier =
        TestIdentifier.of(ReinforcedShulkerBoxesMod.MOD_ID, AdvancementTests.class, name);

    return new TestFunction(
        testIdentifier,
        AdvancementTests.TEST_ENVIRONMENT_DEFAULT,
        AdvancementTests.TEST_STRUCTURE_EMPTY,
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
          ServerPlayer player =
              MockServerPlayerHelper.spawn(
                  context, GameType.SURVIVAL, Vec3.atLowerCornerOf((Vec3i) BlockPos.ZERO));
          AdvancementHolder entry =
              context.getLevel().getServer().getAdvancements().get(advancementId);
          AdvancementProgress progress = player.getAdvancements().getOrStartProgress(entry);

          // Act
          CompletableFuture<Void> futurePartialAct1 = new CompletableFuture<>();
          CompletableFuture<Void> futurePartialAct2 = new CompletableFuture<>();

          Map<String, Boolean> progressMap = new HashMap<String, Boolean>();
          String progressMapKeyBeforeHavingItem = "beforeHavingItem";
          String progressMapKeyAfterHavingItem = "afterHavingItem";

          long tickOrigin = 0;
          context.runAtTickTime(
              tickOrigin,
              () -> {
                progressMap.put(progressMapKeyBeforeHavingItem, progress.isDone());

                player.getInventory().add(new ItemStack((ItemLike) item));

                futurePartialAct1.complete(null);
              });

          long tickObtained = 1;
          context.runAtTickTime(
              tickObtained,
              () -> {
                progressMap.put(progressMapKeyAfterHavingItem, progress.isDone());

                futurePartialAct2.complete(null);
              });

          // Assert
          CompletableFuture.allOf(futurePartialAct1, futurePartialAct2)
              .thenRun(
                  () -> {
                    try {
                      context.assertFalse(
                          progressMap.get(progressMapKeyBeforeHavingItem),
                          Component.literal(
                              String.format(
                                  "Expected that advancement %s has not been done yet, but it has been already done.",
                                  entry)));
                      context.assertTrue(
                          progressMap.get(progressMapKeyAfterHavingItem),
                          Component.literal(
                              String.format(
                                  "Expected that advancement %s has been done, but it has not been done yet.",
                                  entry)));
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
