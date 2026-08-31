package atonkish.reinfshulker.util;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import atonkish.reinfcore.api.ReinforcedCoreRegistry;
import atonkish.reinfcore.util.ReinforcingMaterial;

// CROSS-REFERENCED against EkagraTheBeast/reinforced-shulker-boxes@26.2
// (not independently confirmed for 26.1.2). This is the one class in the
// whole mod with no reinforced-chests/reinforced-barrels analogue at all
// (chests/barrels are opaque solid blocks; shulker boxes are the only
// non-opaque, dynamic-bounding-box block in this mod family), so unlike
// the rest of this port, none of it is CONFIRMED against your uploaded
// sibling jars. Treat every rename below as INFERRED and verify with IDE
// Ctrl+Click on BlockBehaviour.Properties / ShulkerBoxBlockEntity /
// PushReaction before shipping.

// NOTE: DyeColor is kept as net.minecraft.world.item.DyeColor here (matching the
// 1.21.11 original and every sibling mod's decompiled usage) rather than
// net.minecraft.world.item.DyeColor as the fork has it -- the fork's
// import may be specific to a 26.2 package move that hasn't happened yet
// in 26.1.2, or may simply be a mistake on their end. If compilation fails
// on this import, try net.minecraft.world.item.DyeColor next.
public enum ReinforcingMaterialSettings {
  COPPER(
      ReinforcedCoreRegistry.registerReinforcingMaterial("copper", 45, Items.COPPER_INGOT),
      BlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.COPPER),
      new Item.Properties()),
  IRON(
      ReinforcedCoreRegistry.registerReinforcingMaterial("iron", 54, Items.IRON_INGOT),
      BlockBehaviour.Properties.of()
          .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
          .strength(2.0F, 6.0F)
          .sound(SoundType.METAL),
      new Item.Properties()),
  GOLD(
      ReinforcedCoreRegistry.registerReinforcingMaterial("gold", 81, Items.GOLD_INGOT),
      BlockBehaviour.Properties.of()
          .instrument(NoteBlockInstrument.BELL)
          .strength(2.0F, 6.0F)
          .sound(SoundType.METAL),
      new Item.Properties()),
  DIAMOND(
      ReinforcedCoreRegistry.registerReinforcingMaterial("diamond", 108, Items.DIAMOND),
      BlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.METAL),
      new Item.Properties()),
  NETHERITE(
      ReinforcedCoreRegistry.registerReinforcingMaterial("netherite", 108, Items.NETHERITE_INGOT),
      BlockBehaviour.Properties.of().strength(2.0F, 1200.0F).sound(SoundType.NETHERITE_BLOCK),
      new Item.Properties().fireResistant());

  private final ReinforcingMaterial material;
  private final BlockBehaviour.Properties blockSettings;
  private final Item.Properties itemSettings;

  private ReinforcingMaterialSettings(
      ReinforcingMaterial material,
      BlockBehaviour.Properties blockSettings,
      Item.Properties itemSettings) {
    // INFERRED: BlockBehaviour.ContextPredicate (1.21.11 Yarn) ->
    // BlockBehaviour.StatePredicate. Same 3-arg (state, world, pos) shape.
    BlockBehaviour.StatePredicate contextPredicate =
        (state, world, pos) -> {
          BlockEntity blockEntity = world.getBlockEntity(pos);
          if (!(blockEntity instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity)) {
            return true;
          }
          // INFERRED: 1.21.11 checked animationStage == AnimationStage.CLOSED
          // via a package-private accessor; the fork uses a public
          // isClosed() method on ShulkerBoxBlockEntity. VERIFY this method
          // exists in 26.1.2 before relying on it.
          return shulkerBoxBlockEntity.isClosed();
        };

    this.material = material;
    this.blockSettings =
        blockSettings
            // INFERRED (UNVERIFIED, medium-low confidence): the fork calls
            // .forceSolidOn() here, which has no obvious 1.21.11 analogue
            // (1.21.11 called .solidBlock(Blocks::never) instead). This may
            // be a genuinely new method, or the fork may have simply
            // dropped the old solidBlock() call. VERIFY against the actual
            // 26.1.2 BlockBehaviour.Properties source before trusting this
            // line -- if it doesn't compile, try removing it or replacing
            // with .solidBlock(Blocks::never) (renamed if needed).
            .forceSolidOn()
            // INFERRED: dynamicBounds() -> dynamicShape()
            .dynamicShape()
            .noOcclusion()
            // INFERRED: suffocates(predicate) -> isSuffocating(predicate)
            .isSuffocating(contextPredicate)
            // INFERRED: blockVision(predicate) -> isViewBlocking(predicate)
            .isViewBlocking(contextPredicate)
            .pushReaction(PushReaction.DESTROY)
            // CAUTION (UNVERIFIED): the fork passes Blocks::always here.
            // The 1.21.11 original used .solidBlock(Blocks::never) style
            // logic (never a redstone conductor), so Blocks::always looks
            // suspicious/possibly inverted or possibly a genuine fork bug.
            // VERIFY the intended vanilla ShulkerBoxBlock settings and the
            // polarity of isRedstoneConductor(...) directly against 26.1.2
            // source before shipping -- this one is flagged SPECULATIVE.
            .isRedstoneConductor(Blocks::never);
    this.itemSettings = itemSettings.stacksTo(1);
  }

  public ReinforcingMaterial getMaterial() {
    return this.material;
  }

  public BlockBehaviour.Properties getBlockSettings() {
    return this.blockSettings.mapColor(MapColor.COLOR_PURPLE);
  }

  public BlockBehaviour.Properties getColorBlockSettings(DyeColor color) {
    MapColor mapColor =
        switch (color) {
          case PURPLE -> MapColor.TERRACOTTA_PURPLE;
          default -> color.getMapColor();
        };

    return this.blockSettings.mapColor(mapColor);
  }

  public Item.Properties getItemSettings() {
    return this.itemSettings;
  }
}
