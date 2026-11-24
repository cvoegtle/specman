package specman.util;

import javax.swing.KeyStroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Platform-aware keyboard shortcuts and modifier checks.
 * <p>
 * Uses {@link MouseEvents#isMac()} so macOS gets native Command ({@code META_DOWN_MASK}) shortcuts
 * while Windows and Linux keep Control ({@code CTRL_DOWN_MASK}), and tests can exercise both
 * platforms by setting {@code os.name}.
 */
public final class Shortcuts {
  private Shortcuts() {}

  /** The primary menu shortcut modifier mask: Cmd on macOS, Ctrl elsewhere. */
  public static int menuShortcutMask() {
    return MouseEvents.isMac() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
  }

  /** A {@link KeyStroke} using the platform menu shortcut modifier (Cmd on macOS, Ctrl elsewhere). */
  public static KeyStroke menuShortcut(int keyCode) {
    return KeyStroke.getKeyStroke(keyCode, menuShortcutMask());
  }

  /** A {@link KeyStroke} combining the platform menu shortcut modifier with extra modifiers (e.g. {@link InputEvent#SHIFT_DOWN_MASK}). */
  public static KeyStroke menuShortcut(int keyCode, int extraModifiers) {
    return KeyStroke.getKeyStroke(keyCode, menuShortcutMask() | extraModifiers);
  }

  /** Redo shortcut: Cmd+Shift+Z on macOS, Ctrl+Y elsewhere. */
  public static KeyStroke redoKeyStroke() {
    return MouseEvents.isMac()
      ? KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)
      : KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK);
  }

  /** Exit shortcut: Cmd+Q on macOS, Ctrl+Shift+Q elsewhere. */
  public static KeyStroke exitKeyStroke() {
    return MouseEvents.isMac()
      ? KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.META_DOWN_MASK)
      : KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
  }

  /** Settings shortcut: Cmd+, on macOS, none elsewhere. */
  public static KeyStroke settingsKeyStroke() {
    return MouseEvents.isMac()
      ? KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.META_DOWN_MASK)
      : null;
  }

  /** Whether the platform's primary shortcut modifier is held: Cmd on macOS, Ctrl elsewhere. */
  public static boolean isMenuShortcutDown(InputEvent e) {
    return MouseEvents.isMac() ? e.isMetaDown() : e.isControlDown();
  }

  /** Whether the event is a plain menu-shortcut press for the given key (Cmd+key on macOS, Ctrl+key elsewhere,
   * without Shift or Alt). */
  public static boolean isShortcut(KeyEvent e, int keyCode) {
    return isMenuShortcutDown(e) && !e.isAltDown() && !e.isShiftDown() && e.getKeyCode() == keyCode;
  }

  /** Modifier combination for navigating backward/forward in the edit history:
   * Cmd+Option on macOS, Ctrl+Alt elsewhere. */
  public static boolean isHistoryNavigationModifierDown(KeyEvent e) {
    return isMenuShortcutDown(e) && e.isAltDown();
  }

  /** Whether the event presses either Delete or Backspace (important on MacBooks without a dedicated Delete key). */
  public static boolean isDeleteKey(KeyEvent e) {
    return e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE;
  }

  /** Whether the event refreshes a PlantUML diagram: F5 on all platforms, and additionally Cmd+R on macOS. */
  public static boolean isPlantUmlRefresh(KeyEvent e) {
    return e.getKeyCode() == KeyEvent.VK_F5 || (MouseEvents.isMac() && isShortcut(e, KeyEvent.VK_R));
  }

  /** Key code to check with {@link specman.EditorI#isKeyPressed(int)} when hovering followable links:
   * {@link KeyEvent#VK_META} on macOS, {@link KeyEvent#VK_CONTROL} elsewhere. */
  public static int linkModifierKeyCode() {
    return MouseEvents.isMac() ? KeyEvent.VK_META : KeyEvent.VK_CONTROL;
  }

  /**
   * Adapts a Windows/Linux-oriented {@link KeyStroke} (such as those hardcoded in the SHEF library)
   * for the current platform. On non-macOS systems the key stroke is returned unchanged.
   * <p>
   * On macOS:
   * <ul>
   *   <li>Ctrl-based shortcuts are mapped to Cmd (preserving Shift/Alt).</li>
   *   <li>Bare Option/Alt shortcuts (e.g. SHEF's paragraph styles {@code Alt+1..6}) would intercept
   *       special characters on macOS keyboards (such as {@code [} and {@code ]} on German layouts),
   *       so they are moved to {@code Cmd+Option+...}. {@code Alt+D} (Body Text) becomes
   *       {@code Cmd+Option+0} to avoid toggling the macOS Dock ({@code Cmd+Option+D}), and
   *       {@code Alt+Q} (Blockquote) has its accelerator removed to avoid macOS's system
   *       {@code Cmd+Option+Q} shortcut.</li>
   * </ul>
   */
  public static KeyStroke adaptForPlatform(KeyStroke keyStroke) {
    if (keyStroke == null || !MouseEvents.isMac()) {
      return keyStroke;
    }
    int modifiers = keyStroke.getModifiers();
    boolean hasCtrl = (modifiers & (InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK)) != 0;
    boolean hasAlt = (modifiers & (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) != 0;
    boolean hasMeta = (modifiers & (InputEvent.META_DOWN_MASK | InputEvent.META_MASK)) != 0;
    boolean hasShift = (modifiers & (InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK)) != 0;

    if (hasCtrl) {
      int adapted = InputEvent.META_DOWN_MASK
        | (hasShift ? InputEvent.SHIFT_DOWN_MASK : 0)
        | (hasAlt ? InputEvent.ALT_DOWN_MASK : 0);
      return KeyStroke.getKeyStroke(keyStroke.getKeyCode(), adapted, keyStroke.isOnKeyRelease());
    }
    if (hasAlt && !hasMeta) {
      int keyCode = keyStroke.getKeyCode();
      if (keyCode == KeyEvent.VK_Q) {
        return null;
      }
      int adaptedKeyCode = keyCode == KeyEvent.VK_D ? KeyEvent.VK_0 : keyCode;
      int adapted = InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK
        | (hasShift ? InputEvent.SHIFT_DOWN_MASK : 0);
      return KeyStroke.getKeyStroke(adaptedKeyCode, adapted, keyStroke.isOnKeyRelease());
    }
    return keyStroke;
  }
}
