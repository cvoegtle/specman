package specman.util;

import headless.HeadlessExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.swing.JLabel;
import java.awt.Component;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(HeadlessExtension.class)
class MouseEventsTest {
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
  void testMacCtrlLeftClickIsPopupTriggerOnPressAndNotPrimary() {
    System.setProperty("os.name", "Mac OS X");

    MouseEvent pressed = event(MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK, MouseEvent.BUTTON1, true);
    MouseEvent pressedWithoutFlag = event(MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK, MouseEvent.BUTTON1, false);
    MouseEvent released = event(MouseEvent.MOUSE_RELEASED, MouseEvent.CTRL_DOWN_MASK, MouseEvent.BUTTON1, false);
    MouseEvent clicked = event(MouseEvent.MOUSE_CLICKED, MouseEvent.CTRL_DOWN_MASK, MouseEvent.BUTTON1, false);

    assertTrue(MouseEvents.isPopupTrigger(pressed));
    assertTrue(MouseEvents.isPopupTrigger(pressedWithoutFlag));
    assertFalse(MouseEvents.isPopupTrigger(released));
    assertFalse(MouseEvents.isPopupTrigger(clicked));

    assertFalse(MouseEvents.isPrimaryButton(pressed));
    assertFalse(MouseEvents.isPrimaryButton(released));
    assertFalse(MouseEvents.isPrimaryButton(clicked));
  }

  @Test
  void testMacRightClickIsPopupTriggerOnPressOnly() {
    System.setProperty("os.name", "Mac OS X");

    MouseEvent pressed = event(MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON3_DOWN_MASK, MouseEvent.BUTTON3, true);
    MouseEvent released = event(MouseEvent.MOUSE_RELEASED, 0, MouseEvent.BUTTON3, false);

    assertTrue(MouseEvents.isPopupTrigger(pressed));
    assertFalse(MouseEvents.isPopupTrigger(released));
    assertFalse(MouseEvents.isPrimaryButton(pressed));
  }

  @Test
  void testWindowsRightClickIsPopupTriggerOnReleaseOnly() {
    System.setProperty("os.name", "Windows 11");

    MouseEvent pressed = event(MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON3_DOWN_MASK, MouseEvent.BUTTON3, false);
    MouseEvent released = event(MouseEvent.MOUSE_RELEASED, 0, MouseEvent.BUTTON3, true);
    MouseEvent releasedWithoutFlag = event(MouseEvent.MOUSE_RELEASED, 0, MouseEvent.BUTTON3, false);

    assertFalse(MouseEvents.isPopupTrigger(pressed));
    assertTrue(MouseEvents.isPopupTrigger(released));
    assertTrue(MouseEvents.isPopupTrigger(releasedWithoutFlag));
    assertFalse(MouseEvents.isPrimaryButton(pressed));
  }

  @Test
  void testWindowsCtrlLeftClickIsNotPopupTriggerAndStaysPrimary() {
    System.setProperty("os.name", "Windows 11");

    MouseEvent pressed = event(MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK, MouseEvent.BUTTON1, false);
    MouseEvent released = event(MouseEvent.MOUSE_RELEASED, MouseEvent.CTRL_DOWN_MASK, MouseEvent.BUTTON1, false);
    MouseEvent clicked = event(MouseEvent.MOUSE_CLICKED, MouseEvent.CTRL_DOWN_MASK, MouseEvent.BUTTON1, false);

    assertFalse(MouseEvents.isPopupTrigger(pressed));
    assertFalse(MouseEvents.isPopupTrigger(released));
    assertFalse(MouseEvents.isPopupTrigger(clicked));
    assertTrue(MouseEvents.isPrimaryButton(pressed));
    assertTrue(MouseEvents.isPrimaryButton(clicked));
  }

  @Test
  void testStepLinkModifierIsCmdOnMacAndCtrlElsewhere() {
    MouseEvent withCtrl = event(MouseEvent.MOUSE_CLICKED, MouseEvent.CTRL_DOWN_MASK, MouseEvent.BUTTON1, false);
    MouseEvent withMeta = event(MouseEvent.MOUSE_CLICKED, MouseEvent.META_DOWN_MASK, MouseEvent.BUTTON1, false);

    System.setProperty("os.name", "Mac OS X");
    assertTrue(MouseEvents.isStepLinkModifierDown(withMeta));
    assertFalse(MouseEvents.isStepLinkModifierDown(withCtrl));
    assertTrue(MouseEvents.isPrimaryButton(withMeta));

    System.setProperty("os.name", "Windows 11");
    assertTrue(MouseEvents.isStepLinkModifierDown(withCtrl));
    assertFalse(MouseEvents.isStepLinkModifierDown(withMeta));
  }

  private MouseEvent event(int id, int modifiersEx, int button, boolean popupTrigger) {
    return new MouseEvent(target, id, 0L, modifiersEx, 1, 1, 1, popupTrigger, button);
  }
}
