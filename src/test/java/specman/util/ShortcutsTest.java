package specman.util;

import headless.HeadlessExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.swing.JLabel;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(HeadlessExtension.class)
class ShortcutsTest {
  private final String originalOsName = System.getProperty("os.name");
  private final Component target = new JLabel();

  @AfterEach
  void restoreOsName() {
    if (originalOsName == null) {
      System.clearProperty("os.name");
    } else {
      System.setProperty("os.name", originalOsName);
    }
  }

  @Test
  void testMenuShortcutsUseCmdOnMacAndCtrlElsewhere() {
    System.setProperty("os.name", "Mac OS X");
    assertEquals(InputEvent.META_DOWN_MASK, Shortcuts.menuShortcutMask());
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK), Shortcuts.menuShortcut(KeyEvent.VK_S));
    assertEquals(
      KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
      Shortcuts.menuShortcut(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK));
    assertEquals(
      KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
      Shortcuts.redoKeyStroke());
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.META_DOWN_MASK), Shortcuts.exitKeyStroke());
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.META_DOWN_MASK), Shortcuts.settingsKeyStroke());
    assertEquals(KeyEvent.VK_META, Shortcuts.linkModifierKeyCode());

    System.setProperty("os.name", "Windows 11");
    assertEquals(InputEvent.CTRL_DOWN_MASK, Shortcuts.menuShortcutMask());
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), Shortcuts.menuShortcut(KeyEvent.VK_S));
    assertEquals(
      KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
      Shortcuts.menuShortcut(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK));
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), Shortcuts.redoKeyStroke());
    assertEquals(
      KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
      Shortcuts.exitKeyStroke());
    assertNull(Shortcuts.settingsKeyStroke());
    assertEquals(KeyEvent.VK_CONTROL, Shortcuts.linkModifierKeyCode());
  }

  @Test
  void testKeyEventShortcutChecks() {
    KeyEvent cmdC = keyEvent(KeyEvent.VK_C, InputEvent.META_DOWN_MASK);
    KeyEvent ctrlC = keyEvent(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
    KeyEvent cmdShiftC = keyEvent(KeyEvent.VK_C, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
    KeyEvent cmdOptLeft = keyEvent(KeyEvent.VK_LEFT, InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
    KeyEvent ctrlAltLeft = keyEvent(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
    KeyEvent cmdR = keyEvent(KeyEvent.VK_R, InputEvent.META_DOWN_MASK);
    KeyEvent ctrlR = keyEvent(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);
    KeyEvent f5 = keyEvent(KeyEvent.VK_F5, 0);

    System.setProperty("os.name", "Mac OS X");
    assertTrue(Shortcuts.isShortcut(cmdC, KeyEvent.VK_C));
    assertFalse(Shortcuts.isShortcut(ctrlC, KeyEvent.VK_C));
    assertFalse(Shortcuts.isShortcut(cmdShiftC, KeyEvent.VK_C));
    assertTrue(Shortcuts.isHistoryNavigationModifierDown(cmdOptLeft));
    assertFalse(Shortcuts.isHistoryNavigationModifierDown(ctrlAltLeft));
    assertTrue(Shortcuts.isPlantUmlRefresh(f5));
    assertTrue(Shortcuts.isPlantUmlRefresh(cmdR));
    assertFalse(Shortcuts.isPlantUmlRefresh(ctrlR));

    System.setProperty("os.name", "Windows 11");
    assertTrue(Shortcuts.isShortcut(ctrlC, KeyEvent.VK_C));
    assertFalse(Shortcuts.isShortcut(cmdC, KeyEvent.VK_C));
    assertTrue(Shortcuts.isHistoryNavigationModifierDown(ctrlAltLeft));
    assertFalse(Shortcuts.isHistoryNavigationModifierDown(cmdOptLeft));
    assertTrue(Shortcuts.isPlantUmlRefresh(f5));
    assertFalse(Shortcuts.isPlantUmlRefresh(cmdR));
    assertFalse(Shortcuts.isPlantUmlRefresh(ctrlR));
  }

  @Test
  void testDeleteKeyAcceptsDeleteAndBackspace() {
    assertTrue(Shortcuts.isDeleteKey(keyEvent(KeyEvent.VK_DELETE, 0)));
    assertTrue(Shortcuts.isDeleteKey(keyEvent(KeyEvent.VK_BACK_SPACE, 0)));
    assertFalse(Shortcuts.isDeleteKey(keyEvent(KeyEvent.VK_X, 0)));
  }

  @Test
  void testAdaptForPlatformOnMacAndWindows() {
    KeyStroke ctrlB = KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK);
    KeyStroke shiftCtrlY = KeyStroke.getKeyStroke("shift ctrl Y");
    KeyStroke alt5 = KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.ALT_MASK);
    KeyStroke altD = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_MASK);
    KeyStroke altQ = KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.ALT_MASK);
    KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK);

    System.setProperty("os.name", "Mac OS X");
    assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.META_DOWN_MASK), Shortcuts.adaptForPlatform(ctrlB));
    assertEquals(
      KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
      Shortcuts.adaptForPlatform(shiftCtrlY));
    assertEquals(
      KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
      Shortcuts.adaptForPlatform(alt5));
    assertEquals(
      KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
      Shortcuts.adaptForPlatform(altD));
    assertNull(Shortcuts.adaptForPlatform(altQ));
    assertEquals(shiftEnter, Shortcuts.adaptForPlatform(shiftEnter));

    System.setProperty("os.name", "Windows 11");
    assertEquals(ctrlB, Shortcuts.adaptForPlatform(ctrlB));
    assertEquals(shiftCtrlY, Shortcuts.adaptForPlatform(shiftCtrlY));
    assertEquals(alt5, Shortcuts.adaptForPlatform(alt5));
    assertEquals(altD, Shortcuts.adaptForPlatform(altD));
    assertEquals(altQ, Shortcuts.adaptForPlatform(altQ));
  }

  private KeyEvent keyEvent(int keyCode, int modifiersEx) {
    return new KeyEvent(target, KeyEvent.KEY_PRESSED, 0L, modifiersEx, keyCode, KeyEvent.CHAR_UNDEFINED);
  }
}
