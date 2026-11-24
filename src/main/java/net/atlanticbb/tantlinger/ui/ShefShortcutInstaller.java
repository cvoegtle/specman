package net.atlanticbb.tantlinger.ui;

import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import specman.util.Shortcuts;

import javax.swing.Action;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

/**
 * Adapts the hardcoded Ctrl/Alt accelerators of SHEF actions to the current platform (Cmd on macOS).
 * Works analogously to {@link ShefIconInstaller}: mutating {@link Action#ACCELERATOR_KEY} on the
 * actions updates every Swing menu item bound to them without needing to rebuild the SHEF jar.
 */
public final class ShefShortcutInstaller {

  private ShefShortcutInstaller() {}

  public static void install(Iterable<?> actions) {
    for (Object candidate : actions) {
      if (candidate instanceof Action action) {
        installOn(action);
      }
    }
  }

  private static void installOn(Action action) {
    if (action == CompoundUndoManager.UNDO) {
      action.putValue(Action.ACCELERATOR_KEY, Shortcuts.menuShortcut(KeyEvent.VK_Z));
      return;
    }
    if (action == CompoundUndoManager.REDO) {
      action.putValue(Action.ACCELERATOR_KEY, Shortcuts.redoKeyStroke());
      return;
    }
    Object value = action.getValue(Action.ACCELERATOR_KEY);
    if (value instanceof KeyStroke keyStroke) {
      KeyStroke adapted = Shortcuts.adaptForPlatform(keyStroke);
      if (adapted != keyStroke) {
        action.putValue(Action.ACCELERATOR_KEY, adapted);
      }
    }
  }
}
