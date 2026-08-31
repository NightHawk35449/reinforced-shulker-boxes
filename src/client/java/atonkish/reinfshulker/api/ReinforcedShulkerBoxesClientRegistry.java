package atonkish.reinfshulker.api;

import java.util.List;

import net.minecraft.client.resources.model.sprite.SpriteId;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfshulker.client.render.ModTexturedRenderLayers;

// CONFIRMED: SpriteId (renamed from SpriteIdentifier) and package location
// net.minecraft.client.resources.model.sprite.SpriteId, plus
// Identifier -> net.minecraft.resources.Identifier, both confirmed via
// decompiled reinforced-chests-4.0.9+26.1.2.jar's client renderer classes.
// The two @Deprecated legacy accessors (registerMaterialAtlasTexture /
// registerMaterialRenderLayer) from 1.21.11 pointed at
// TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE / getShulkerBoxes(),
// which no longer exist in this form now that texture-sheet handling moved
// into Sheets (see ModTexturedRenderLayers). Since these two methods were
// already @Deprecated and unused internally in the original source, and
// their old return types (RenderLayer-based atlas access) don't have a
// clean 26.1.2 equivalent that preserves their exact old signature, they
// are dropped here rather than guessed at. If any downstream mod actually
// called them, that's a genuine binary-compat break inherent to the vanilla
// rendering rewrite, not something a source-level rename can paper over.
@Environment(EnvType.CLIENT)
public class ReinforcedShulkerBoxesClientRegistry {
  public static SpriteId registerMaterialDefaultSprite(
      String namespace, ReinforcingMaterial material) {
    return ModTexturedRenderLayers.registerMaterialDefaultSprite(namespace, material);
  }

  public static List<SpriteId> registerMaterialColoringSprites(
      String namespace, ReinforcingMaterial material) {
    return ModTexturedRenderLayers.registerMaterialColoringSprites(namespace, material);
  }
}
