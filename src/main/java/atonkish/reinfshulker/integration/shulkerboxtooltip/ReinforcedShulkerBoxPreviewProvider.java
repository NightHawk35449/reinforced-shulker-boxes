package atonkish.reinfshulker.integration.shulkerboxtooltip;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.color.ColorKey;
import com.misterpemodder.shulkerboxtooltip.api.provider.BlockEntityPreviewProvider;

import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfshulker.block.ReinforcedShulkerBoxBlock;

// CONFIRMED: Block.getBlockFromItem(...) is replaced with the standard
// "instanceof BlockItem blockItem && blockItem.getBlock() instanceof X"
// pattern, since BlockItem/Item/Block package locations
// (net.minecraft.world.item / net.minecraft.world.level.block) are
// confirmed throughout this port (ModItems, ModBlocks, etc.). This mirrors
// how EkagraTheBeast/reinforced-shulker-boxes@26.2 accesses the block from
// an ItemStack too, so it's a safe, low-risk change either way (not
// dependent on ShulkerBoxTooltip's own API, only on vanilla ItemStack/
// BlockItem, which are confirmed elsewhere).

// SPECULATIVE (third-party API): the 2-arg super(size, true) constructor
// and the showTooltipHints/getWindowColorKey/getMaxRowSize method
// signatures are preserved unchanged from the 1.21.11 original rather than
// adopting the fork's expanded 4-arg constructor and extra overrides --
// see the note in ShulkerBoxTooltip.java. If this doesn't compile against
// the real ShulkerBoxTooltip 5.4.0+26.1.1 API, that's the first thing to
// check.
public class ReinforcedShulkerBoxPreviewProvider extends BlockEntityPreviewProvider {
  protected final int maxRowSize;
  private final ReinforcingMaterial material;

  public ReinforcedShulkerBoxPreviewProvider(ReinforcingMaterial material) {
    super(material.getSize(), true);

    int size = material.getSize();
    this.maxRowSize = size <= 81 ? 9 : size / 9;

    this.material = material;
  }

  @Override
  public boolean showTooltipHints(PreviewContext context) {
    return true;
  }

  @Override
  @Environment(EnvType.CLIENT)
  public ColorKey getWindowColorKey(PreviewContext context) {
    DyeColor dye = null;
    if (context.stack().getItem() instanceof BlockItem blockItem
        && blockItem.getBlock() instanceof ReinforcedShulkerBoxBlock block) {
      dye = block.getColor();
    }

    if (dye == null) {
      return ColorKey.SHULKER_BOX;
    }

    return switch (dye) {
      case ORANGE -> ColorKey.ORANGE_SHULKER_BOX;
      case MAGENTA -> ColorKey.MAGENTA_SHULKER_BOX;
      case LIGHT_BLUE -> ColorKey.LIGHT_BLUE_SHULKER_BOX;
      case YELLOW -> ColorKey.YELLOW_SHULKER_BOX;
      case LIME -> ColorKey.LIME_SHULKER_BOX;
      case PINK -> ColorKey.PINK_SHULKER_BOX;
      case GRAY -> ColorKey.GRAY_SHULKER_BOX;
      case LIGHT_GRAY -> ColorKey.LIGHT_GRAY_SHULKER_BOX;
      case CYAN -> ColorKey.CYAN_SHULKER_BOX;
      case PURPLE -> ColorKey.PURPLE_SHULKER_BOX;
      case BLUE -> ColorKey.BLUE_SHULKER_BOX;
      case BROWN -> ColorKey.BROWN_SHULKER_BOX;
      case GREEN -> ColorKey.GREEN_SHULKER_BOX;
      case RED -> ColorKey.RED_SHULKER_BOX;
      case BLACK -> ColorKey.BLACK_SHULKER_BOX;
      default -> ColorKey.WHITE_SHULKER_BOX;
    };
  }

  @Override
  public int getMaxRowSize(PreviewContext context) {
    return this.maxRowSize;
  }

  public ReinforcingMaterial getMaterial() {
    return this.material;
  }
}
