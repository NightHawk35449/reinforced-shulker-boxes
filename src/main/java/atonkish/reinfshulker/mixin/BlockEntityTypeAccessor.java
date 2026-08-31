package atonkish.reinfshulker.mixin;

import java.util.Set;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// CONFIRMED: identical class name, package suffix, and @Accessor target
// as reinforced-chests' shipped 26.1.2 atonkish.reinfchest.mixin.BlockEntityTypeAccessor
// AND reinforced-barrels' atonkish.reinfbarrel.mixin.BlockEntityTypeAccessor
// (both decompiled from their shipped 26.1.2 jars). The only difference from
// the old 1.21.11 mixin is the field name: 1.21.11 named it "blocks", the
// 26.1.2 Mojang-mapped field is "validBlocks" (getValidBlocks()).
@Mixin(value = {BlockEntityType.class})
public interface BlockEntityTypeAccessor {
  @Accessor
  public Set<Block> getValidBlocks();
}
