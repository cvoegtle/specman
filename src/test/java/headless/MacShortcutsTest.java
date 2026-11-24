package headless;

import net.atlanticbb.tantlinger.ui.text.actions.PlantUmlAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import specman.editarea.EditContainer;
import specman.editarea.TableEditArea;
import specman.editarea.TextEditArea;
import specman.editarea.image.plantuml.PlantUmlImageEditArea;
import specman.editarea.image.plantuml.PlantUmlSourceEditorPopup;
import specman.util.NoteEditorPopup;
import specman.view.EinfacherSchrittView;

import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static headless.HeadlessExtension.specman;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static specman.i18n.Messages.i18n;

@ExtendWith(HeadlessExtension.class)
class MacShortcutsTest {
  private final String originalOsName = System.getProperty("os.name");

  @AfterEach
  void restoreOsName() {
    if (originalOsName == null) {
      System.clearProperty("os.name");
    } else {
      System.setProperty("os.name", originalOsName);
    }
  }

  @Test
  void testHyperlinkClickAndTooltipUseCmdOnMacAndCtrlOnWindows() throws Exception {
    EinfacherSchrittView step = specman.clickNewSimpleStep();
    TextEditArea text = step.getFirstEditArea().asTextArea();
    text.setText("<a href=\"https://example.org\">link</a>");
    specman.layoutComponents();

    Rectangle2D firstChar = text.modelToView2D(1);
    int x = (int) firstChar.getX() + 2;
    int y = (int) firstChar.getCenterY();

    System.setProperty("os.name", "Mac OS X");
    text.dispatchEvent(new MouseEvent(text, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, false));
    assertEquals(i18n("tooltip.stepnumber.scroll.mac"), text.getToolTipText());

    // Ctrl+click on macOS must not follow the link
    text.dispatchEvent(new MouseEvent(text, MouseEvent.MOUSE_PRESSED, 0L,
      MouseEvent.CTRL_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK, x, y, 1, false, MouseEvent.BUTTON1));
    assertEquals(List.of(), specman.addressesOpenedInBrowser());

    // Cmd+click on macOS follows the link
    text.dispatchEvent(new MouseEvent(text, MouseEvent.MOUSE_PRESSED, 0L,
      MouseEvent.META_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK, x, y, 1, false, MouseEvent.BUTTON1));
    assertEquals(List.of("https://example.org"), specman.addressesOpenedInBrowser());

    System.setProperty("os.name", "Windows 11");
    assertEquals(i18n("tooltip.stepnumber.scroll"), text.getToolTipText());
  }

  @Test
  void testPlantUmlRefreshesOnCmdRForMac(@TempDir Path directory) throws IOException {
    System.setProperty("os.name", "Mac OS X");
    Path included = directory.resolve("included.puml");
    Files.writeString(included, "class Included\n");
    EinfacherSchrittView step = specman.clickNewSimpleStep();
    specman.focus(step);
    EditContainer container = step.getFirstEditArea().asTextArea().getParent();
    specman.doEdit(PlantUmlAction.class);
    specman.layoutComponents();
    PlantUmlImageEditArea diagram = (PlantUmlImageEditArea) container.getEditAreaAt(1);
    diagram.commitSource("@startuml\n!include " + included.toString().replace('\\', '/') + "\nclass Own\n@enduml\n");
    String sizeBefore = diagram.getFullSizeImage().getWidth() + "x" + diagram.getFullSizeImage().getHeight();
    Files.writeString(included, "class Included\nclass Second\nclass Third\n");
    specman.focus(diagram);

    specman.press(KeyEvent.VK_R, InputEvent.META_DOWN_MASK);

    String sizeAfter = diagram.getFullSizeImage().getWidth() + "x" + diagram.getFullSizeImage().getHeight();
    assertNotEquals(sizeBefore, sizeAfter);
  }

  @Test
  void testNoteAndPlantUmlPopupKeyBindingsAdaptToPlatform() {
    System.setProperty("os.name", "Mac OS X");
    NoteEditorPopup macNote = new NoteEditorPopup(new Font("Dialog", Font.PLAIN, 12), "", s -> {}, () -> {});
    JTextArea macNoteArea = findFirst(macNote, JTextArea.class);
    assertEquals("commit", macNoteArea.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.META_DOWN_MASK)));
    assertNull(macNoteArea.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK)));

    PlantUmlSourceEditorPopup macPlantUml = new PlantUmlSourceEditorPopup("@startuml\nA -> B\n@enduml", s -> {}, () -> {}, s -> {});
    JTextArea macPlantUmlArea = findFirst(macPlantUml, JTextArea.class);
    assertEquals("preview", macPlantUmlArea.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.META_DOWN_MASK)));
    assertEquals("preview", macPlantUmlArea.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)));

    System.setProperty("os.name", "Windows 11");
    NoteEditorPopup winNote = new NoteEditorPopup(new Font("Dialog", Font.PLAIN, 12), "", s -> {}, () -> {});
    JTextArea winNoteArea = findFirst(winNote, JTextArea.class);
    assertEquals("commit", winNoteArea.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK)));
    assertNull(winNoteArea.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.META_DOWN_MASK)));
  }

  @Test
  void testEditHistoryNavigationUsesCmdOptionOnMacAndCtrlAltOnWindows() {
    EinfacherSchrittView step1 = specman.clickNewSimpleStep();
    EinfacherSchrittView step2 = specman.clickNewSimpleStep();
    specman.focus(step1);
    specman.focus(step2);

    System.setProperty("os.name", "Mac OS X");
    specman.press(KeyEvent.VK_LEFT, InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
    assertSame(step1.getFirstEditArea(), specman.focusedArea());
    specman.press(KeyEvent.VK_RIGHT, InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
    assertSame(step2.getFirstEditArea(), specman.focusedArea());

    System.setProperty("os.name", "Windows 11");
    specman.press(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
    assertSame(step1.getFirstEditArea(), specman.focusedArea());
    specman.press(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
    assertSame(step2.getFirstEditArea(), specman.focusedArea());
  }

  @Test
  void testTableHoverRemovalWorksWithBackspaceOnMac() {
    EinfacherSchrittView step = specman.clickNewSimpleStep();
    specman.focus(step);
    specman.addTable(2, 2);
    EditContainer container = step.getFirstEditArea().asTextArea().getParent();
    TableEditArea table = (TableEditArea) container.getEditAreaAt(1);
    specman.layoutComponents();

    // Hover top-left corner of the table to activate RemoveTable selection
    table.dispatchEvent(new MouseEvent(table, MouseEvent.MOUSE_MOVED, 0L, 0, 1, 1, 0, false));
    KeyEvent backspace = new KeyEvent(table, KeyEvent.KEY_PRESSED, 0L, 0, KeyEvent.VK_BACK_SPACE, KeyEvent.CHAR_UNDEFINED);
    KeyboardFocusManager.getCurrentKeyboardFocusManager().dispatchEvent(backspace);

    assertFalse(hasTable(container), "Backspace while hovering table corner must remove the table");
    assertNotNull(step.getFirstEditArea());
  }

  private static boolean hasTable(EditContainer container) {
    for (int i = 0; ; i++) {
      try {
        if (container.getEditAreaAt(i) instanceof TableEditArea) {
          return true;
        }
      } catch (IndexOutOfBoundsException end) {
        return false;
      }
    }
  }

  private static <T extends Component> T findFirst(Container root, Class<T> type) {
    T found = findFirstOrNull(root, type);
    if (found == null) {
      throw new AssertionError("No component of type " + type.getSimpleName() + " found");
    }
    return found;
  }

  private static <T extends Component> T findFirstOrNull(Container root, Class<T> type) {
    for (Component c : root.getComponents()) {
      if (type.isInstance(c)) {
        return type.cast(c);
      }
      if (c instanceof Container child) {
        T found = findFirstOrNull(child, type);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
