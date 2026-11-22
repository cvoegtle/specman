package specman.util;

import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.util.Locale;

/**
 * Platform-aware classification of mouse gestures.
 * <p>
 * On macOS, Ctrl+left-click is the standard equivalent of right-click (for one-button mice and
 * trackpads) and popup menus open on {@code mousePressed}; on Windows and Linux right-click opens
 * them on {@code mouseReleased}. Because Ctrl+click on a step number opens the context menu on
 * macOS, jumping between Break and Catch steps uses Cmd+click there instead of Ctrl+click.
 */
public final class MouseEvents {
  private MouseEvents() {}

  public static boolean isMac() {
    return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
  }

  /** Whether the given mouse event should open a context menu right now. Must be checked in both
   * {@code mousePressed} and {@code mouseReleased}. */
  public static boolean isPopupTrigger(MouseEvent e) {
    if (e.isPopupTrigger()) {
      return true;
    }
    if (isMac()) {
      return e.getID() == MouseEvent.MOUSE_PRESSED && isContextMenuGesture(e);
    }
    return e.getID() == MouseEvent.MOUSE_RELEASED && SwingUtilities.isRightMouseButton(e);
  }

  /** Whether the event uses a context-menu button/modifier combination (right button anywhere, or
   * Ctrl+left button on macOS), regardless of event phase (pressed/released/clicked). */
  public static boolean isContextMenuGesture(MouseEvent e) {
    return e.isPopupTrigger()
      || SwingUtilities.isRightMouseButton(e)
      || (isMac() && SwingUtilities.isLeftMouseButton(e) && e.isControlDown());
  }

  /** Whether the event is a primary (left) button interaction that is not a context-menu gesture
   * (e.g. excludes Ctrl+left-click on macOS). */
  public static boolean isPrimaryButton(MouseEvent e) {
    return SwingUtilities.isLeftMouseButton(e) && !isContextMenuGesture(e);
  }

  /** Whether the modifier key for jumping between a Break step and its Catch heading is held:
   * Cmd on macOS, Ctrl on other platforms. */
  public static boolean isStepLinkModifierDown(MouseEvent e) {
    return isMac() ? e.isMetaDown() : e.isControlDown();
  }
}
