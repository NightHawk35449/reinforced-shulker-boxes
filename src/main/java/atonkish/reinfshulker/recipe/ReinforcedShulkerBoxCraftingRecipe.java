package atonkish.reinfshulker.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

// CONFIRMED via successful 26.1.2 build and extensive in-game crafting
// tests across all material tiers (copper/iron/gold/diamond/netherite,
// including chest-conversion recipes): vanilla's ShapedRecipe is
// restructured in 26.1.2 around Recipe.CommonInfo /
// CraftingRecipe.CraftingBookInfo / ItemStackTemplate records, replacing
// the simpler 1.21.11 constructor that took
// group/category/RawShapedRecipe/ItemStack/showNotification directly.
// None of the sibling mods (core/chests/barrels) register a custom
// crafting recipe, so this file had no direct sibling-jar precedent --
// verification came from the real compiler and real crafting-table
// testing instead.
public class ReinforcedShulkerBoxCraftingRecipe extends ShapedRecipe {
  final ShapedRecipePattern raw;
  final ItemStackTemplate result;

  public ReinforcedShulkerBoxCraftingRecipe(
      String group,
      CraftingBookCategory category,
      ShapedRecipePattern raw,
      ItemStack result,
      boolean showNotification) {
    this(
        new Recipe.CommonInfo(showNotification),
        new CraftingRecipe.CraftingBookInfo(category, group),
        raw,
        ItemStackTemplate.fromNonEmptyStack(result));
  }

  public ReinforcedShulkerBoxCraftingRecipe(
      Recipe.CommonInfo commonInfo,
      CraftingRecipe.CraftingBookInfo bookInfo,
      ShapedRecipePattern raw,
      ItemStackTemplate result) {
    super(commonInfo, bookInfo, raw, result);
    this.raw = raw;
    this.result = result;
  }

  public ReinforcedShulkerBoxCraftingRecipe(
      String group, CraftingBookCategory category, ShapedRecipePattern raw, ItemStack result) {
    this(group, category, raw, result, true);
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public RecipeSerializer getSerializer() {
    return ModRecipeSerializer.REINFORCED_SHULKER_BOX;
  }

  private ShapedRecipePattern getRaw() {
    return this.raw;
  }

  private ItemStackTemplate getResult() {
    return this.result;
  }

  private Recipe.CommonInfo getCommonInfo() {
    return this.commonInfo;
  }

  private CraftingRecipe.CraftingBookInfo getBookInfo() {
    return this.bookInfo;
  }

  // Behavior-preserving core of the original 1.21.11 recipe: take the
  // item in the center slot (the plain shulker box being reinforced) and
  // transmute it into our material-specific item, preserving its NBT
  // (contents). transmuteCopy(...) is CROSS-REFERENCED, unverified.
  @Override
  public ItemStack assemble(CraftingInput craftingRecipeInput) {
    Item item = this.getResult().create().getItem();
    ItemStack itemStack = craftingRecipeInput.getItem(4);
    return itemStack.transmuteCopy(item, 1);
  }

  public static class Serializer {
    public static final MapCodec<ReinforcedShulkerBoxCraftingRecipe> CODEC =
        RecordCodecBuilder.mapCodec(
            (instance) ->
                instance
                    .group(
                        Recipe.CommonInfo.MAP_CODEC.forGetter(
                            ReinforcedShulkerBoxCraftingRecipe::getCommonInfo),
                        CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(
                            ReinforcedShulkerBoxCraftingRecipe::getBookInfo),
                        ShapedRecipePattern.MAP_CODEC.forGetter(
                            ReinforcedShulkerBoxCraftingRecipe::getRaw),
                        ItemStackTemplate.CODEC
                            .fieldOf("result")
                            .forGetter((recipe) -> recipe.result))
                    .apply(instance, ReinforcedShulkerBoxCraftingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReinforcedShulkerBoxCraftingRecipe>
        PACKET_CODEC = StreamCodec.of(Serializer::write, Serializer::read);

    public Serializer() {}

    public MapCodec<ReinforcedShulkerBoxCraftingRecipe> codec() {
      return CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, ReinforcedShulkerBoxCraftingRecipe> streamCodec() {
      return PACKET_CODEC;
    }

    private static ReinforcedShulkerBoxCraftingRecipe read(RegistryFriendlyByteBuf buf) {
      Recipe.CommonInfo commonInfo = Recipe.CommonInfo.STREAM_CODEC.decode(buf);
      CraftingRecipe.CraftingBookInfo bookInfo =
          CraftingRecipe.CraftingBookInfo.STREAM_CODEC.decode(buf);
      ShapedRecipePattern rawShapedRecipe = ShapedRecipePattern.STREAM_CODEC.decode(buf);
      ItemStackTemplate itemStack = ItemStackTemplate.STREAM_CODEC.decode(buf);
      return new ReinforcedShulkerBoxCraftingRecipe(
          commonInfo, bookInfo, rawShapedRecipe, itemStack);
    }

    private static void write(
        RegistryFriendlyByteBuf buf, ReinforcedShulkerBoxCraftingRecipe recipe) {
      Recipe.CommonInfo.STREAM_CODEC.encode(buf, recipe.getCommonInfo());
      CraftingRecipe.CraftingBookInfo.STREAM_CODEC.encode(buf, recipe.getBookInfo());
      ShapedRecipePattern.STREAM_CODEC.encode(buf, recipe.getRaw());
      ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.getResult());
    }
  }
}
