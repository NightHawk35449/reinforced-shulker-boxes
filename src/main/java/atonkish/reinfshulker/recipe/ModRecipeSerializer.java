package atonkish.reinfshulker.recipe;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import atonkish.reinfshulker.ReinforcedShulkerBoxesMod;

// CONFIRMED registration mechanics (Registry.register into
// BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath) via
// every other Mod*.java file in this port. The RecipeSerializer<T>
// constructor call itself (taking a MapCodec + StreamCodec pair directly,
// no separate anonymous class) is CROSS-REFERENCED against
// EkagraTheBeast/reinforced-shulker-boxes@26.2 -- UNVERIFIED for 26.1.2.
public class ModRecipeSerializer {
  public static final RecipeSerializer<ReinforcedShulkerBoxCraftingRecipe> REINFORCED_SHULKER_BOX;

  public static void init() {}

  private static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(
      String id, S serializer) {
    Identifier identifier = Identifier.fromNamespaceAndPath(ReinforcedShulkerBoxesMod.MOD_ID, id);
    return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, identifier, serializer);
  }

  static {
    REINFORCED_SHULKER_BOX =
        register(
            "crafting_special_reinforcedshulkerbox",
            new RecipeSerializer<>(
                ReinforcedShulkerBoxCraftingRecipe.Serializer.CODEC,
                ReinforcedShulkerBoxCraftingRecipe.Serializer.PACKET_CODEC));
  }
}
