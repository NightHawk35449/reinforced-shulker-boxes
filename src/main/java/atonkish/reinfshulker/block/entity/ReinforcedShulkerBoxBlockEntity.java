package atonkish.reinfshulker.block.entity;

import java.util.stream.IntStream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import atonkish.reinfcore.screen.ReinforcedStorageScreenHandler;
import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfshulker.mixin.BlockEntityAccessor;

// CONFIRMED: createMenu(...) delegating to
// ReinforcedStorageScreenHandler.createShulkerBoxScreen(material, syncId,
// playerInventory, this) is confirmed against the 4-arg overload present
// in decompiled reinforced-core-4.0.9+26.1.2.jar's
// ReinforcedStorageScreenHandler. getDefaultName()/setItems(...)/
// BlockEntityAccessor.setType(...) pattern is confirmed against decompiled
// reinforced-barrels-2.7.6+26.1.2.jar's ReinforcedBarrelBlockEntity (same
// shape).

// CROSS-REFERENCED (UNVERIFIED) against
// EkagraTheBeast/reinforced-shulker-boxes@26.2 for getSlotsForFace(...)
// (renamed from getAvailableSlots(...)) since neither barrel nor chest
// overrides this Container-side-access method.
public class ReinforcedShulkerBoxBlockEntity extends ShulkerBoxBlockEntity {
  private final ReinforcingMaterial cachedMaterial;

  public ReinforcedShulkerBoxBlockEntity(
      ReinforcingMaterial material, @Nullable DyeColor color, BlockPos pos, BlockState state) {
    super(color, pos, state);
    ((BlockEntityAccessor) this)
        .setType(ModBlockEntityType.REINFORCED_SHULKER_BOX_MAP.get(material));
    this.setItems(NonNullList.withSize(material.getSize(), ItemStack.EMPTY));
    this.cachedMaterial = material;
  }

  public ReinforcedShulkerBoxBlockEntity(
      ReinforcingMaterial material, BlockPos pos, BlockState state) {
    super(pos, state);
    ((BlockEntityAccessor) this)
        .setType(ModBlockEntityType.REINFORCED_SHULKER_BOX_MAP.get(material));
    this.setItems(NonNullList.withSize(material.getSize(), ItemStack.EMPTY));
    this.cachedMaterial = material;
  }

  @Override
  protected Component getDefaultName() {
    String namespace = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()).getNamespace();
    return Component.translatable(
        "container." + namespace + "." + this.cachedMaterial.getName() + "ShulkerBox");
  }

  @Override
  public int[] getSlotsForFace(Direction side) {
    return IntStream.range(0, this.getContainerSize()).toArray();
  }

  public ReinforcingMaterial getMaterial() {
    return this.cachedMaterial;
  }

  @Override
  protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
    return ReinforcedStorageScreenHandler.createShulkerBoxScreen(
        this.cachedMaterial, syncId, playerInventory, this);
  }
}
