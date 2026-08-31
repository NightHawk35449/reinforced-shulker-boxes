package atonkish.reinfshulker.mixin.datafixer.fix;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Dynamic;

import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import net.minecraft.world.item.DyeColor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfcore.util.ReinforcingMaterials;
import atonkish.reinfshulker.ReinforcedShulkerBoxesMod;

// CONFIRMED against decompiled reinforced-chests-4.0.9+26.1.2.jar's
// ItemStackComponentizationFixMixin (corrected from an earlier stale-Yarn
// draft in this port): package is net.minecraft.util.datafix.fixes.
// ItemStackComponentizationFix (not net.minecraft.datafixer.fix.*), the
// nested data type is ItemStackComponentizationFix.ItemStackData (not
// .StackData), the injected method is "fixBlockEntityTag" (not
// "fixBlockEntityData"), and the matcher call is data.is(itemIds) (not
// data.itemMatches(itemIds)). This matches exactly what the access widener
// entry in reinfshulker.accesswidener already targeted.
@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {
  @Inject(at = @At("RETURN"), method = "fixBlockEntityTag", cancellable = true)
  private static <T> void fixBlockEntityTag(
      ItemStackComponentizationFix.ItemStackData data,
      Dynamic<T> dynamic,
      String blockEntityId,
      CallbackInfoReturnable<Dynamic<T>> cir) {
    Set<String> itemIds = new HashSet<>();
    for (ReinforcingMaterial material : ReinforcingMaterials.MAP.values()) {
      itemIds.add(
          String.format("%s:%s_shulker_box", ReinforcedShulkerBoxesMod.MOD_ID, material.getName()));
      for (DyeColor color : DyeColor.values()) {
        itemIds.add(
            String.format(
                "%s:%s_%s_shulker_box",
                ReinforcedShulkerBoxesMod.MOD_ID, color.getName(), material.getName()));
      }
    }

    if (data.is(itemIds)) {
      List<Dynamic<T>> list =
          dynamic
              .get("Items")
              .asList(
                  itemsDynamic ->
                      itemsDynamic
                          .emptyMap()
                          .set(
                              "slot",
                              itemsDynamic.createInt(
                                  itemsDynamic.get("Slot").asByte((byte) 0) & 255))
                          .set("item", itemsDynamic.remove("Slot")));
      if (!list.isEmpty()) {
        data.setComponent("minecraft:container", dynamic.createList(list.stream()));
      }
      cir.setReturnValue(dynamic.remove("Items"));
    }
  }
}
