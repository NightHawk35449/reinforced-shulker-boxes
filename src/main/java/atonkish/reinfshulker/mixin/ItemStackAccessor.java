package atonkish.reinfshulker.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

// CONFIRMED via successful 26.1.2 build: this field's type is
// Holder<Item> (not Item), consistent with Minecraft's ongoing shift to
// Holder-wrapped registry references in ItemStack internals.
@Mixin(ItemStack.class)
public interface ItemStackAccessor {
  @Mutable
  @Accessor("item")
  public void setItem(Holder<Item> item);
}
