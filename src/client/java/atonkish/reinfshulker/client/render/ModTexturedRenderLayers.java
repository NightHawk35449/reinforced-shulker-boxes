package atonkish.reinfshulker.client.render;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.ImmutableList;

import atonkish.reinfcore.util.ReinforcingMaterial;

// CROSS-REFERENCED against EkagraTheBeast/reinforced-shulker-boxes@26.2 for
// SpriteId (confirmed separately via decompiled reinforced-chests 26.1.2
// renderer) and Sheets.SHULKER_SHEET (INFERRED, consistent with the
// confirmed Sheets.CHEST_MAPPER constant in reinforced-chests, and with
// TexturedRenderLayers historically merging into Sheets in Mojang mappings
// even before 26.x).

// IMPORTANT CORRECTION vs. the 26.2 fork: the fork hardcodes "minecraft" as
// the sprite namespace, but this mod's actual texture files (unchanged
// below in src/main/resources) live under
// assets/reinfshulker/textures/entity/shulker/..., i.e. the "reinfshulker"
// namespace -- matching the original 1.21.11 source's use of the passed
// `namespace` parameter. Using "minecraft" here would be a real bug
// (textures failing to resolve), so this uses `namespace` like the
// original.
@Environment(EnvType.CLIENT)
public class ModTexturedRenderLayers {
  public static final Map<ReinforcingMaterial, SpriteId> REINFORCED_SHULKER_TEXTURE_ID_MAP =
      new LinkedHashMap<>();
  public static final Map<ReinforcingMaterial, List<SpriteId>>
      COLORED_REINFORCED_SHULKER_BOXES_TEXTURES_MAP = new LinkedHashMap<>();

  public static SpriteId registerMaterialDefaultSprite(
      String namespace, ReinforcingMaterial material) {
    if (!REINFORCED_SHULKER_TEXTURE_ID_MAP.containsKey(material)) {
      SpriteId spriteId =
          new SpriteId(
              Sheets.SHULKER_SHEET,
              Identifier.fromNamespaceAndPath(
                  namespace, String.format("entity/shulker/%s/shulker", material.getName())));
      REINFORCED_SHULKER_TEXTURE_ID_MAP.put(material, spriteId);
    }

    return REINFORCED_SHULKER_TEXTURE_ID_MAP.get(material);
  }

  public static List<SpriteId> registerMaterialColoringSprites(
      String namespace, ReinforcingMaterial material) {
    if (!COLORED_REINFORCED_SHULKER_BOXES_TEXTURES_MAP.containsKey(material)) {
      List<SpriteId> spriteIds =
          Stream.of(DyeColor.values())
              .map(
                  color ->
                      new SpriteId(
                          Sheets.SHULKER_SHEET,
                          Identifier.fromNamespaceAndPath(
                              namespace,
                              String.format(
                                  "entity/shulker/%s/shulker_%s",
                                  material.getName(), color.getName()))))
              .collect(ImmutableList.toImmutableList());
      COLORED_REINFORCED_SHULKER_BOXES_TEXTURES_MAP.put(material, spriteIds);
    }

    return COLORED_REINFORCED_SHULKER_BOXES_TEXTURES_MAP.get(material);
  }
}
