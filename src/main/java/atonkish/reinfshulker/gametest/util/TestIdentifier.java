package atonkish.reinfshulker.gametest.util;

import java.util.Locale;

import net.minecraft.resources.Identifier;

// CONFIRMED: Identifier.of(namespace, path) -> Identifier.fromNamespaceAndPath(namespace, path),
// net.minecraft.util.Identifier -> net.minecraft.resources.Identifier;
// verified byte-for-byte identical logic via decompiled
// reinforced-chests-4.0.9+26.1.2.jar's TestIdentifier.
public class TestIdentifier {
  public static Identifier of(String namespace, Class<?> testClass, String name) {
    return Identifier.fromNamespaceAndPath(
        namespace,
        camelToSnake(String.format("%s/%s", testClass.getSimpleName(), name).replace(" ", "_")));
  }

  private static String camelToSnake(String input) {
    return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
  }
}
