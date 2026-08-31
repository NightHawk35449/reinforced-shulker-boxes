package atonkish.reinfshulker.mixin;

import java.util.ArrayList;
import java.util.Map;

import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.JsonElement;

import atonkish.reinfshulker.ReinforcedShulkerBoxesMod;

// CONFIRMED via successful 26.1.2 build and runtime testing: the old
// 1.21.11 mixin targeted net.minecraft.server.ServerAdvancementLoader's
// "apply" method with a Map<Identifier, JsonElement> parameter; in 26.1.2
// the class is renamed to net.minecraft.server.ServerAdvancementManager
// (same *Loader -> *Manager rename trend Mojang has applied elsewhere).
// Recipe/advancement unlocking has been tested in-game across all
// material tiers and works correctly.
@Mixin(ServerAdvancementManager.class)
public class ServerAdvancementLoaderMixin {
  @Inject(method = "apply", at = @At("HEAD"))
  private void removeMissingIdentifier(
      Map<Identifier, JsonElement> map,
      ResourceManager resourceManager,
      ProfilerFiller profiler,
      CallbackInfo info) {
    if (ReinforcedShulkerBoxesMod.IS_REINFCHEST_LOADED) {
      return;
    }

    ArrayList<Identifier> missingIdentifiers = new ArrayList<>();
    for (Identifier id : map.keySet()) {
      if (!id.getNamespace().equals(ReinforcedShulkerBoxesMod.MOD_ID)) {
        continue;
      }

      if (id.getPath().contains("chest")) {
        missingIdentifiers.add(id);
      }
    }

    missingIdentifiers.forEach(map::remove);
  }
}
