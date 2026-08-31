package atonkish.reinfshulker.gametest.testcase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

import atonkish.reinfcore.gametest.TestFunction;
import atonkish.reinfcore.util.ReinforcingMaterials;
import atonkish.reinfshulker.ReinforcedShulkerBoxesMod;
import atonkish.reinfshulker.gametest.util.TestIdentifier;
import atonkish.reinfshulker.item.ModItems;

// CONFIRMED (recipe manager plumbing) against decompiled
// reinforced-chests-4.0.9+26.1.2.jar's RecipeTests: ServerWorld ->
// ServerLevel, DynamicRegistryManager -> RegistryAccess (declared but
// unused in both the confirmed chests version and here -- dropped),
// ServerRecipeManager -> RecipeManager, CraftingRecipeInput.create(w,h,l)
// -> CraftingInput.of(w,h,l), recipeManager.getFirstMatch(...) ->
// recipeManager.getRecipeFor(...), recipe.craft(input, registryManager) ->
// recipe.assemble(input) (single-arg, matching the codec restructure also
// used in this port's ReinforcedShulkerBoxCraftingRecipe#assemble),
// ItemStack.areEqual(...) -> ItemStack.matches(...), context.getWorld() ->
// context.getLevel().

// CONFIRMED via real 26.1.2 compiler output: DataComponents.CONTAINER is
// at net.minecraft.core.component.DataComponents (an earlier draft guessed
// "DataComponentTypes" as the class name in that same package -- wrong,
// fixed after a real build error), and
// ItemContainerContents.fromItems(List) at
// net.minecraft.world.item.component.ItemContainerContents compiles and
// works correctly.
// DyeItem.byColor(DyeColor) does NOT exist in 26.1.2 (CONFIRMED via real
// compiler output: DyeItem itself resolves, the byColor factory method
// does not) -- replaced with the explicit DYE_ITEM_MAP below instead of
// guessing at DyeItem's real API surface.

// STRUCTURE NOTE: the 1.21.11 original repeats this same test-generation
// logic five times, once per material tier (copper/iron/gold/diamond/
// netherite), with only the tier names and "from Copper Chests" special
// case differing. This port collapses that into a loop producing the exact
// same set of TestFunctions -- purely mechanical deduplication, not a
// behavior change.
public class RecipeTests {
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

  // CONFIRMED (compiler evidence from a real 26.1.2 build): DyeItem itself
  // resolves fine at net.minecraft.world.item.DyeItem, but it has no
  // byColor(DyeColor) static factory in 26.1.2 -- the compiler reported
  // "cannot find symbol: method byColor(DyeColor)" specifically, not a
  // missing-class error. Rather than guess again at DyeItem's real API,
  // this sidesteps it entirely with an explicit Items.*_DYE map, the same
  // safe approach already used for SHULKER_BOX_MAP above.
  private static final Map<DyeColor, Item> DYE_ITEM_MAP =
      new LinkedHashMap<>() {
        {
          put(DyeColor.WHITE, Items.WHITE_DYE);
          put(DyeColor.ORANGE, Items.ORANGE_DYE);
          put(DyeColor.MAGENTA, Items.MAGENTA_DYE);
          put(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_DYE);
          put(DyeColor.YELLOW, Items.YELLOW_DYE);
          put(DyeColor.LIME, Items.LIME_DYE);
          put(DyeColor.PINK, Items.PINK_DYE);
          put(DyeColor.GRAY, Items.GRAY_DYE);
          put(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_DYE);
          put(DyeColor.CYAN, Items.CYAN_DYE);
          put(DyeColor.PURPLE, Items.PURPLE_DYE);
          put(DyeColor.BLUE, Items.BLUE_DYE);
          put(DyeColor.BROWN, Items.BROWN_DYE);
          put(DyeColor.GREEN, Items.GREEN_DYE);
          put(DyeColor.RED, Items.RED_DYE);
          put(DyeColor.BLACK, Items.BLACK_DYE);
        }
      };

  private static final List<Item> COPPER_CHEST_FAMILY =
      List.of(
          Items.COPPER_CHEST,
          Items.EXPOSED_COPPER_CHEST,
          Items.WEATHERED_COPPER_CHEST,
          Items.OXIDIZED_COPPER_CHEST,
          Items.WAXED_COPPER_CHEST,
          Items.WAXED_EXPOSED_COPPER_CHEST,
          Items.WAXED_WEATHERED_COPPER_CHEST,
          Items.WAXED_OXIDIZED_COPPER_CHEST);

  // Tier order: base item map (null == vanilla shulker box tier), then
  // material ingot, matching the original's copper->iron->gold->diamond
  // crafting chain and diamond->netherite smithing step.
  private static final String[] TIER_MATERIAL_NAMES = {
    "copper", "iron", "gold", "diamond", "netherite"
  };

  private static final String TEST_ENVIRONMENT_DEFAULT =
      String.format("%s:recipe/default", ReinforcedShulkerBoxesMod.MOD_ID);
  private static final String TEST_STRUCTURE_EMPTY = "fabric-gametest-api-v1:empty";

  public static final Collection<TestFunction> TEST_FUNCTIONS =
      new ArrayList<>() {
        {
          for (int tierIndex = 0; tierIndex < TIER_MATERIAL_NAMES.length; tierIndex++) {
            String tierName = TIER_MATERIAL_NAMES[tierIndex];
            String previousTierName = tierIndex == 0 ? null : TIER_MATERIAL_NAMES[tierIndex - 1];
            boolean isFirstTier = tierIndex == 0;
            boolean isSmithingTier = tierName.equals("netherite");

            Map<DyeColor, Item> baseTierItems =
                isFirstTier
                    ? SHULKER_BOX_MAP
                    : ModItems.REINFORCED_SHULKER_BOX_MAP.get(
                        ReinforcingMaterials.MAP.get(previousTierName));
            Map<DyeColor, Item> thisTierItems =
                ModItems.REINFORCED_SHULKER_BOX_MAP.get(ReinforcingMaterials.MAP.get(tierName));

            Item craftingMaterial =
                switch (tierName) {
                  case "copper" -> Items.COPPER_INGOT;
                  case "iron" -> Items.IRON_INGOT;
                  case "gold" -> Items.GOLD_INGOT;
                  case "diamond" -> Items.DIAMOND;
                  case "netherite" -> Items.NETHERITE_INGOT;
                  default -> throw new IllegalStateException("Unknown tier: " + tierName);
                };

            // Craft/smith from the previous tier + material, per color.
            for (DyeColor color : baseTierItems.keySet()) {
              ItemStack baseShulkerBox = withDirt(new ItemStack(baseTierItems.get(color)));
              ItemStack material = new ItemStack(craftingMaterial);
              ItemStack shulkerBox = withDirt(new ItemStack(thisTierItems.get(color)));

              if (isSmithingTier) {
                ItemStack template = new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
                add(
                    RecipeTests.createTest(
                        String.format(
                            "Smithing %s",
                            Component.translatable(baseShulkerBox.getItem().getDescriptionId())
                                .getString()),
                        RecipeType.SMITHING,
                        new SmithingRecipeInput(template, baseShulkerBox, material),
                        shulkerBox));
              } else {
                add(
                    RecipeTests.createTest(
                        String.format(
                            "Craft %s",
                            Component.translatable(baseShulkerBox.getItem().getDescriptionId())
                                .getString()),
                        RecipeType.CRAFTING,
                        CraftingInput.of(
                            3,
                            3,
                            List.of(
                                material,
                                material,
                                material,
                                material,
                                baseShulkerBox,
                                material,
                                material,
                                material,
                                material)),
                        shulkerBox));
              }
            }

            // Dye-recoloring within this tier, for every (base, target)
            // color pair.
            for (DyeColor baseColor : thisTierItems.keySet()) {
              for (DyeColor dyeColor : DyeColor.values()) {
                if (dyeColor.equals(baseColor)) {
                  continue;
                }

                ItemStack baseShulkerBox = withDirt(new ItemStack(thisTierItems.get(baseColor)));
                ItemStack dye = new ItemStack(DYE_ITEM_MAP.get(dyeColor));
                ItemStack dyedShulkerBox = withDirt(new ItemStack(thisTierItems.get(dyeColor)));

                add(
                    RecipeTests.createTest(
                        String.format(
                            "Coloring from %s to %s",
                            Component.translatable(baseShulkerBox.getItem().getDescriptionId())
                                .getString(),
                            Component.translatable(dyedShulkerBox.getItem().getDescriptionId())
                                .getString()),
                        RecipeType.CRAFTING,
                        CraftingInput.of(
                            2, 2, List.of(baseShulkerBox, dye, ItemStack.EMPTY, ItemStack.EMPTY)),
                        dyedShulkerBox));
              }
            }

            // Craft from the equivalent reinforced-chests tier (backward
            // compatible upgrade path), plus the copper-chest-family
            // special case on the copper tier only.
            ItemStack chest =
                new ItemStack(
                    atonkish.reinfchest.item.ModItems.REINFORCED_CHEST_MAP.get(
                        ReinforcingMaterials.MAP.get(tierName)));
            ItemStack shell = new ItemStack(Items.SHULKER_SHELL);
            ItemStack shulkerBoxFromChest = new ItemStack(thisTierItems.get((DyeColor) null));

            add(
                RecipeTests.createTest(
                    isFirstTier
                        ? "Craft Copper Shulker Box from Reinforced Copper Chest"
                        : String.format(
                            "Craft %s from %s",
                            Component.translatable(shulkerBoxFromChest.getItem().getDescriptionId())
                                .getString(),
                            Component.translatable(chest.getItem().getDescriptionId()).getString()),
                    RecipeType.CRAFTING,
                    CraftingInput.of(
                        3,
                        3,
                        List.of(
                            shell,
                            ItemStack.EMPTY,
                            ItemStack.EMPTY,
                            chest,
                            ItemStack.EMPTY,
                            ItemStack.EMPTY,
                            shell,
                            ItemStack.EMPTY,
                            ItemStack.EMPTY)),
                    shulkerBoxFromChest));

            if (isFirstTier) {
              for (Item item : COPPER_CHEST_FAMILY) {
                ItemStack familyChest = new ItemStack(item);
                ItemStack familyShulkerBox = new ItemStack(thisTierItems.get((DyeColor) null));

                add(
                    RecipeTests.createTest(
                        String.format(
                            "Craft Copper Shulker Box from %s",
                            Component.translatable(familyChest.getItem().getDescriptionId())
                                .getString()),
                        RecipeType.CRAFTING,
                        CraftingInput.of(
                            3,
                            3,
                            List.of(
                                shell,
                                ItemStack.EMPTY,
                                ItemStack.EMPTY,
                                familyChest,
                                ItemStack.EMPTY,
                                ItemStack.EMPTY,
                                shell,
                                ItemStack.EMPTY,
                                ItemStack.EMPTY)),
                        familyShulkerBox));
              }
            }
          }
        }
      };

  private static ItemStack withDirt(ItemStack stack) {
    // DataComponents.CONTAINER / ItemContainerContents.fromItems(List) are
    // CONFIRMED -- see class-level note.
    stack.set(
        net.minecraft.core.component.DataComponents.CONTAINER,
        net.minecraft.world.item.component.ItemContainerContents.fromItems(
            List.of(new ItemStack((ItemLike) Items.DIRT))));
    return stack;
  }

  private static <I extends RecipeInput, T extends Recipe<I>> TestFunction createTest(
      String name, RecipeType<T> type, I input, ItemStack expected) {
    Identifier testIdentifier =
        TestIdentifier.of(ReinforcedShulkerBoxesMod.MOD_ID, RecipeTests.class, name);

    return new TestFunction(
        testIdentifier,
        RecipeTests.TEST_ENVIRONMENT_DEFAULT,
        RecipeTests.TEST_STRUCTURE_EMPTY,
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
          ServerLevel world = context.getLevel();
          RecipeManager recipeManager = world.getServer().getRecipeManager();
          T recipe =
              ((RecipeHolder<T>)
                      recipeManager.getRecipeFor(type, input, (Level) world).orElseThrow())
                  .value();

          // Act
          ItemStack actual = recipe.assemble(input);

          // Assert
          try {
            context.assertTrue(
                ItemStack.matches(actual, expected),
                Component.literal("Recipe result differs from expected."));
          } catch (Exception e) {
            ReinforcedShulkerBoxesMod.LOGGER.error("[{}] {}", testIdentifier, e.getMessage());
            throw e;
          }

          context.succeed();
        });
  }
}
