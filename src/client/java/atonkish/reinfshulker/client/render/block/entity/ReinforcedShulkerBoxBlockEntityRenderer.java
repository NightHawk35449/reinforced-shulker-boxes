package atonkish.reinfshulker.client.render.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.blockentity.state.ShulkerBoxRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.Nullable;

import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfshulker.block.entity.ReinforcedShulkerBoxBlockEntity;
import atonkish.reinfshulker.client.render.ModTexturedRenderLayers;

// CONFIRMED via successful 26.1.2 build and in-game testing (in-world
// rendering and lid open/close animation both verified working): the
// BlockEntityRenderer interface shape is createRenderState /
// extractRenderState / submit(state, poseStack, submitNodeCollector,
// cameraRenderState), matching decompiled
// reinforced-chests-4.0.9+26.1.2.jar's ReinforcedChestBlockEntityRenderer,
// with the same Sheets/SpriteId/ModelFeatureRenderer.CrumblingOverlay/
// OverlayTexture.NO_OVERLAY/CameraRenderState/SubmitNodeCollector types.

// The key structural difference from the old 1.21.11 renderer: rather than
// reimplementing shulker box rendering from scratch, this delegates most
// of the work to vanilla's net.minecraft.client.renderer.blockentity.
// ShulkerBoxRenderer, only overriding the sprite selection to point at our
// material-specific textures. This mirrors how reinforced-chests' renderer
// reuses vanilla's ChestRenderer instead of reimplementing chest
// rendering.
@Environment(EnvType.CLIENT)
public class ReinforcedShulkerBoxBlockEntityRenderer
    implements BlockEntityRenderer<
        ReinforcedShulkerBoxBlockEntity,
        ReinforcedShulkerBoxBlockEntityRenderer.ReinforcedShulkerBoxRenderState> {
  private final ShulkerBoxRenderer vanillaRenderer;

  public ReinforcedShulkerBoxBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    this.vanillaRenderer = new ShulkerBoxRenderer(context);
  }

  @Override
  public ReinforcedShulkerBoxRenderState createRenderState() {
    return new ReinforcedShulkerBoxRenderState();
  }

  @Override
  public void extractRenderState(
      ReinforcedShulkerBoxBlockEntity blockEntity,
      ReinforcedShulkerBoxRenderState state,
      float partialTicks,
      Vec3 cameraPosition,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    this.vanillaRenderer.extractRenderState(
        blockEntity, state, partialTicks, cameraPosition, breakProgress);
    state.material = blockEntity.getMaterial();
  }

  @Override
  public void submit(
      ReinforcedShulkerBoxRenderState state,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      CameraRenderState cameraRenderState) {
    SpriteId sprite = getSprite(state.material, state.color);

    poseStack.pushPose();
    poseStack.mulPose(ShulkerBoxRenderer.modelTransform(state.direction));
    this.vanillaRenderer.submit(
        poseStack,
        submitNodeCollector,
        state.lightCoords,
        OverlayTexture.NO_OVERLAY,
        state.progress,
        state.breakProgress,
        sprite,
        0);
    poseStack.popPose();
  }

  private static SpriteId getSprite(ReinforcingMaterial material, @Nullable DyeColor color) {
    if (color == null) {
      return ModTexturedRenderLayers.REINFORCED_SHULKER_TEXTURE_ID_MAP.get(material);
    }

    return ModTexturedRenderLayers.COLORED_REINFORCED_SHULKER_BOXES_TEXTURES_MAP
        .get(material)
        .get(color.getId());
  }

  public static class ReinforcedShulkerBoxRenderState extends ShulkerBoxRenderState {
    public ReinforcingMaterial material;
  }
}
