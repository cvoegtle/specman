package net.atlanticbb.tantlinger.ui;

import headless.HeadlessExtension;
import net.atlanticbb.tantlinger.shef.HTMLEditorPane;
import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.actions.ClearStylesAction;
import net.atlanticbb.tantlinger.ui.text.actions.CopyAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLBlockAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLInlineAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import specman.menubar.SpecmanMenuBar;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;
import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static headless.HeadlessExtension.specman;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(HeadlessExtension.class)
class ShefShortcutInstallerTest {
  private final String originalOsName = System.getProperty("os.name");

  @AfterEach
  void restoreOsNameAndStaticActions() {
    if (originalOsName == null) {
      System.clearProperty("os.name");
    } else {
      System.setProperty("os.name", originalOsName);
    }
    ShefShortcutInstaller.install(List.of(CompoundUndoManager.UNDO, CompoundUndoManager.REDO));
  }

  @Test
  void installAdaptsShefActionsForMacAndKeepsCtrlOnWindows() {
    System.setProperty("os.name", "Mac OS X");
    Action copy = new CopyAction();
    Action bold = new HTMLInlineAction(HTMLInlineAction.BOLD);
    Action clearStyles = new ClearStylesAction();
    Action heading5 = new HTMLBlockAction(HTMLBlockAction.H5);
    Action bodyText = new HTMLBlockAction(HTMLBlockAction.DIV);
    Action blockquote = new HTMLBlockAction(HTMLBlockAction.BLOCKQUOTE);

    ShefShortcutInstaller.install(List.of(
      CompoundUndoManager.UNDO, CompoundUndoManager.REDO,
      copy, bold, clearStyles, heading5, bodyText, blockquote));

    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK), CompoundUndoManager.UNDO.getValue(Action.ACCELERATOR_KEY));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), CompoundUndoManager.REDO.getValue(Action.ACCELERATOR_KEY));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_DOWN_MASK), copy.getValue(Action.ACCELERATOR_KEY));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.META_DOWN_MASK), bold.getValue(Action.ACCELERATOR_KEY));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), clearStyles.getValue(Action.ACCELERATOR_KEY));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK), heading5.getValue(Action.ACCELERATOR_KEY));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK), bodyText.getValue(Action.ACCELERATOR_KEY));
    assertNull(blockquote.getValue(Action.ACCELERATOR_KEY));

    System.setProperty("os.name", "Windows 11");
    Action winCopy = new CopyAction();
    Action winHeading5 = new HTMLBlockAction(HTMLBlockAction.H5);
    ShefShortcutInstaller.install(List.of(CompoundUndoManager.UNDO, CompoundUndoManager.REDO, winCopy, winHeading5));

    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), CompoundUndoManager.UNDO.getValue(Action.ACCELERATOR_KEY));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), CompoundUndoManager.REDO.getValue(Action.ACCELERATOR_KEY));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), winCopy.getValue(Action.ACCELERATOR_KEY));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.ALT_MASK), winHeading5.getValue(Action.ACCELERATOR_KEY));
  }

  @Test
  void menuBarOnMacHasNoCtrlOrBareAltAccelerators() {
    System.setProperty("os.name", "Mac OS X");
    HTMLEditorPane editorPane = new HTMLEditorPane(new UndoManager());
    SpecmanMenuBar menuBar = new SpecmanMenuBar(specman, editorPane);

    List<JMenuItem> items = new ArrayList<>();
    for (int i = 0; i < menuBar.getMenuCount(); i++) {
      collectMenuItems(menuBar.getMenu(i), items);
    }

    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.META_DOWN_MASK), acceleratorOf(items, SpecmanMenuBar.LOAD_ITEM));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK), acceleratorOf(items, SpecmanMenuBar.SAVE_ITEM));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), acceleratorOf(items, SpecmanMenuBar.SAVE_AS_ITEM));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.META_DOWN_MASK), acceleratorOf(items, SpecmanMenuBar.EXIT_ITEM));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.META_DOWN_MASK), acceleratorOf(items, SpecmanMenuBar.SETTINGS_ITEM));

    for (JMenuItem item : items) {
      KeyStroke ks = item.getAccelerator();
      if (ks == null) {
        continue;
      }
      int mods = ks.getModifiers();
      boolean hasCtrl = (mods & (InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK)) != 0;
      boolean hasAlt = (mods & (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) != 0;
      boolean hasMeta = (mods & (InputEvent.META_DOWN_MASK | InputEvent.META_MASK)) != 0;
      assertFalse(hasCtrl, "Menu item '" + item.getText() + "' must not use Ctrl on macOS: " + ks);
      assertFalse(hasAlt && !hasMeta, "Menu item '" + item.getText() + "' must not use bare Option/Alt on macOS: " + ks);
    }
  }

  private static KeyStroke acceleratorOf(List<JMenuItem> items, String name) {
    return items.stream()
      .filter(item -> name.equals(item.getName()))
      .findFirst()
      .orElseThrow(() -> new AssertionError("No menu item named " + name))
      .getAccelerator();
  }

  private static void collectMenuItems(JMenu menu, List<JMenuItem> target) {
    if (menu == null) {
      return;
    }
    for (Component c : menu.getMenuComponents()) {
      if (c instanceof JMenu subMenu) {
        collectMenuItems(subMenu, target);
      } else if (c instanceof JMenuItem item) {
        target.add(item);
      }
    }
  }
}
