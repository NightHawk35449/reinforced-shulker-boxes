package atonkish.reinfshulker.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfshulker.block.entity.ModBlockEntityType;
import atonkish.reinfshulker.block.entity.ReinforcedShulkerBoxBlockEntity;
import atonkish.reinfshulker.stat.ModStats;

// CONFIRMED: useWithoutItem(state, world, pos, player, hit) signature,
// getTicker(...) override shape, player.openMenu(...)/player.awardStat(...)
// calls, and Level/BlockPos/ServerLevel/Player/InteractionResult package
// locations are all confirmed via decompiled reinforced-barrels-2.7.6+
// 26.1.2.jar's ReinforcedBarrelBlock (same interaction pattern, different
// block entity type).

// CROSS-REFERENCED (not independently confirmed) against
// EkagraTheBeast/reinforced-shulker-boxes@26.2 for the shulker-specific
// pieces with no barrel/chest analogue: getTicker's createTickerHelper(...)
// name (this one also matches long-standing pre-26.x official Mojang
// naming from my own background knowledge, so treated as higher
// confidence than most other cross-referenced items here); the
// canOpen(...) AABB collision check using Shulker.getProgressDeltaAabb(...)
// /Vec3.atBottomCenterOf(...)/AABB.deflate(...)/Level.noCollision(...),
// which are all long-stable Yarn->Mojang renames from my own background
// knowledge (Box->AABB, contract->deflate, isSpaceEmpty->noCollision,
// toBottomCenterPos->atBottomCenterOf); and
// ShulkerBoxBlockEntity.AnimationStatus.CLOSED / getAnimationStatus(),
// which is UNVERIFIED and should be checked against the real 26.1.2 jar.
public class ReinforcedShulkerBoxBlock extends ShulkerBoxBlock {
  private final ReinforcingMaterial material;

  public ReinforcedShulkerBoxBlock(
      ReinforcingMaterial material, @Nullable DyeColor color, BlockBehaviour.Properties settings) {
    super(color, settings);
    this.material = material;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new ReinforcedShulkerBoxBlockEntity(this.material, this.getColor(), pos, state);
  }

  @Override
  @Nullable public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level world, BlockState state, BlockEntityType<T> type) {
    return ReinforcedShulkerBoxBlock.createTickerHelper(
        type,
        ModBlockEntityType.REINFORCED_SHULKER_BOX_MAP.get(this.material),
        ReinforcedShulkerBoxBlockEntity::tick);
  }

  @Override
  public InteractionResult useWithoutItem(
      BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
    if (world instanceof ServerLevel serverWorld) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
        if (canOpen(state, world, pos, shulkerBoxBlockEntity)) {
          player.openMenu(shulkerBoxBlockEntity);
          player.awardStat(ModStats.OPEN_REINFORCED_SHULKER_BOX_MAP.get(this.material));
          PiglinAi.angerNearbyPiglins(serverWorld, player, true);
        }
      }
    }

    return InteractionResult.SUCCESS;
  }

  private static boolean canOpen(
      BlockState state, Level world, BlockPos pos, ShulkerBoxBlockEntity entity) {
    if (entity.getAnimationStatus() != ShulkerBoxBlockEntity.AnimationStatus.CLOSED) {
      return true;
    } else {
      AABB box =
          Shulker.getProgressDeltaAabb(
                  1.0F, state.getValue(FACING), 0.0F, 0.5F, Vec3.atBottomCenterOf(pos))
              .deflate(1.0E-6D);
      return world.noCollision(box);
    }
  }

  public static Block get(ReinforcingMaterial material, @Nullable DyeColor color) {
    return ModBlocks.REINFORCED_SHULKER_BOX_MAP.get(material).get(color);
  }

  public ReinforcingMaterial getMaterial() {
    return this.material;
  }

  public static ItemStack getItemStack(ReinforcingMaterial material, @Nullable DyeColor color) {
    return new ItemStack(get(material, color));
  }
}
