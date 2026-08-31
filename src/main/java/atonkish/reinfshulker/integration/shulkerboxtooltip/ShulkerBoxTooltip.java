package atonkish.reinfshulker.integration.shulkerboxtooltip;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.misterpemodder.shulkerboxtooltip.api.ShulkerBoxTooltipApi;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProviderRegistry;

import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfcore.util.ReinforcingMaterials;
import atonkish.reinfshulker.block.entity.ModBlockEntityType;
import atonkish.reinfshulker.item.ModItems;

// KEPT IN src/main (not src/client): this preserves the 1.21.11 original's
// placement -- ShulkerBoxTooltip's own "shulkerboxtooltip" entrypoint is
// only invoked client-side by that mod even though the class is compiled
// into the common jar, so moving it to the client sourceset isn't required.
// EkagraTheBeast/reinforced-shulker-boxes@26.2 moved this file to
// src/client -- that's a valid alternative structure, not a required
// 26.1.2 change, so it's not adopted here per "no broad speculative
// rewrites."

// CONFIRMED: Identifier.of(...) -> Identifier.fromNamespaceAndPath(...),
// BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(...) replacing
// BlockEntityType.getId(...), and net.minecraft.core.registries.
// BuiltInRegistries are all confirmed elsewhere in this port (see
// ReinforcedShulkerBoxBlockEntity#getDefaultName, ModStats, ModBlocks).

// SPECULATIVE (third-party API, cannot be verified from the 26.1.2
// Minecraft jar or any sibling mod): ShulkerBoxTooltipApi /
// PreviewProviderRegistry / PreviewProvider are com.misterpemodder.
// shulkerboxtooltip's own API surface, not Mojang's. Their actual 26.1.x
// method signatures are unverified here -- if compilation fails on this
// file, check ShulkerBoxTooltip 5.4.0+26.1.1's real API (e.g. via its
// jar/sources if you add it as a compile-time dependency) rather than
// assuming the fork's expanded method set (getInventoryMaxSize,
// getActiveSlotCount, getPriority, 4-arg super(...)) is required --
// that fork may simply have added functionality beyond the 1.21.11
// original rather than reacting to a forced API change.
public class ShulkerBoxTooltip implements ShulkerBoxTooltipApi {
  private static void register(
      PreviewProviderRegistry registry,
      String namespace,
      String id,
      PreviewProvider provider,
      Item... items) {
    registry.register(Identifier.fromNamespaceAndPath(namespace, id), provider, items);
  }

  @Override
  public void registerProviders(PreviewProviderRegistry registry) {
    for (ReinforcingMaterial material : ReinforcingMaterials.MAP.values()) {
      BlockEntityType<?> blockEntityType =
          ModBlockEntityType.REINFORCED_SHULKER_BOX_MAP.get(material);
      String namespace = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType).getNamespace();
      String id = material.getName() + "_shulker_box";
      Item[] items =
          ModItems.REINFORCED_SHULKER_BOX_MAP.get(material).values().toArray(new Item[0]);
      register(registry, namespace, id, new ReinforcedShulkerBoxPreviewProvider(material), items);
    }
  }
}
