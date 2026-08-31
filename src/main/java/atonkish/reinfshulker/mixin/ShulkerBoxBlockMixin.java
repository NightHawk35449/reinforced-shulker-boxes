package atonkish.reinfshulker.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import atonkish.reinfshulker.block.ReinforcedShulkerBoxBlock;
import atonkish.reinfshulker.block.entity.ReinforcedShulkerBoxBlockEntity;

// CROSS-REFERENCED against EkagraTheBeast/reinforced-shulker-boxes@26.2
// (not independently confirmed for 26.1.2). Injects into
// ShulkerBoxBlock#playerWillDestroy right after it builds the drop
// ItemStack via ItemStack.applyComponents(...), swapping the vanilla
// shulker box item for our material-specific item so drops preserve the
// reinforcing material and contents. Method name "playerWillDestroy" and
// the applyComponents(DataComponentMap) target signature are UNVERIFIED
// against the actual 26.1.2 jar -- if the @At INVOKE target string doesn't
// resolve at build time, check the real method with IDE Ctrl+Click on
// ShulkerBoxBlock and update the target string and local variable order to
// match.
@Mixin(ShulkerBoxBlock.class)
public class ShulkerBoxBlockMixin {
  @Inject(
      method = "playerWillDestroy",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/ItemStack;applyComponents(Lnet/minecraft/core/component/DataComponentMap;)V"),
      locals = LocalCapture.CAPTURE_FAILHARD)
  public void onBreak(
      Level world,
      BlockPos pos,
      BlockState state,
      Player player,
      CallbackInfoReturnable<BlockState> cir,
      BlockEntity blockEntity,
      ShulkerBoxBlockEntity shulkerBoxBlockEntity,
      ItemStack itemStack) {
    if (blockEntity instanceof ReinforcedShulkerBoxBlockEntity entity) {
      ((ItemStackAccessor) (Object) itemStack)
          .setItem(
              ReinforcedShulkerBoxBlock.get(entity.getMaterial(), entity.getColor())
                  .asItem()
                  .builtInRegistryHolder());
    }
  }
}
