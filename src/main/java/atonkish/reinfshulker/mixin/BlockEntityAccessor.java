package atonkish.reinfshulker.mixin;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

// CONFIRMED: identical class name, package, field type, and @Mixin/@Accessor
// signature as reinforced-barrels' shipped 26.1.2
// atonkish.reinfbarrel.mixin.BlockEntityAccessor (decompiled from
// reinforced-barrels-2.7.6+26.1.2.jar). Only the "type" field's setter is
// widened; net.minecraft.world.level.block.entity.BlockEntity /
// BlockEntityType are the confirmed 26.1.2 Mojang-mapped locations of the
// old net.minecraft.block.entity.BlockEntity / BlockEntityType.
@Mixin(BlockEntity.class)
public interface BlockEntityAccessor {
  @Mutable
  @Accessor("type")
  public void setType(BlockEntityType<?> type);
}
