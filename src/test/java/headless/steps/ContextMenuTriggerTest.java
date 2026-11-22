package headless.steps;

import headless.HeadlessExtension;
import headless.SpecmanFT;
import net.atlanticbb.tantlinger.ui.text.actions.PlantUmlAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import specman.CatchLinkPanel;
import specman.editarea.EditContainer;
import specman.editarea.image.plantuml.PlantUmlContextMenu;
import specman.editarea.image.plantuml.PlantUmlImageEditArea;
import specman.metatag.generic.GenericMetaTag;
import specman.metatag.generic.MetaTagContextMenu;
import specman.metatag.generic.config.MetaTagConfig;
import specman.metatag.stepnumberlabel.StepnumberContextMenu;
import specman.metatag.stepnumberlabel.StepnumberLabel;
import specman.ops.buttons.CreateWhileStepOpButton;
import specman.view.BreakSchrittView;
import specman.view.EinfacherSchrittView;
import specman.view.WhileSchrittView;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.List;

import static headless.HeadlessExtension.specman;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static specman.i18n.Messages.i18n;

@ExtendWith(HeadlessExtension.class)
class ContextMenuTriggerTest {
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
  void testMacCtrlClickOnStepNumberOpensContextMenu() {
    System.setProperty("os.name", "Mac OS X");
    EinfacherSchrittView step1 = specman.clickNewSimpleStep();
    specman.focus(step1);
    EinfacherSchrittView step2 = specman.clickNewSimpleStep();
    step1.getFirstEditArea().asTextArea().setText("first");
    step2.getFirstEditArea().asTextArea().setText("second");
    specman.layoutComponents();

    // Initialize menu on step1 first, then Ctrl+click step2 to prove the menu re-targets to step2
    SpecmanFT.rightClick(specman.findStepnumberLabel(step1));
    SpecmanFT.ctrlLeftClick(specman.findStepnumberLabel(step2));

    clickItem(StepnumberContextMenu.popupMenu(), StepnumberContextMenu.DELETE_ITEM);
    specman.assertStepNumbers("1");
    assertEquals("first", specman.findStepByStepID("1").getFirstEditArea().asTextArea().getPlainText());
  }

  @Test
  void testWindowsCtrlClickOnStepNumberDoesNotOpenContextMenu() {
    System.setProperty("os.name", "Windows 11");
    EinfacherSchrittView step1 = specman.clickNewSimpleStep();
    specman.focus(step1);
    EinfacherSchrittView step2 = specman.clickNewSimpleStep();
    step1.getFirstEditArea().asTextArea().setText("first");
    step2.getFirstEditArea().asTextArea().setText("second");
    specman.layoutComponents();

    // Open menu on step1 via right-click, then Ctrl+click step2: menu must still point to step1
    SpecmanFT.rightClick(specman.findStepnumberLabel(step1));
    SpecmanFT.ctrlLeftClick(specman.findStepnumberLabel(step2));

    clickItem(StepnumberContextMenu.popupMenu(), StepnumberContextMenu.DELETE_ITEM);
    specman.assertStepNumbers("1");
    assertEquals("second", specman.findStepByStepID("1").getFirstEditArea().asTextArea().getPlainText());
  }

  @Test
  void testMacCtrlClickOnMetaTagOpensContextMenu() {
    System.setProperty("os.name", "Mac OS X");
    EinfacherSchrittView step = specman.clickNewSimpleStep();
    MetaTagConfig config = new MetaTagConfig("tag1", "T1", null, Color.YELLOW, null, true);
    GenericMetaTag tag = new GenericMetaTag(config, "some note");
    step.addTag(tag);
    specman.layoutComponents();

    SpecmanFT.ctrlLeftClick(tag);

    clickItem(MetaTagContextMenu.popupMenu(), MetaTagContextMenu.DELETE_ITEM);
    assertFalse(step.hasTag(config), "Delete from MetaTagContextMenu must remove the tag");
  }

  @Test
  void testMacCtrlClickOnPlantUmlDiagramOpensContextMenu() {
    System.setProperty("os.name", "Mac OS X");
    EinfacherSchrittView step = specman.clickNewSimpleStep();
    specman.focus(step);
    EditContainer container = step.getFirstEditArea().asTextArea().getParent();
    specman.doEdit(PlantUmlAction.class);
    specman.layoutComponents();
    PlantUmlImageEditArea diagram = (PlantUmlImageEditArea) container.getEditAreaAt(1);
    diagram.commitSource("@startuml\nclass A\n@enduml\n");
    specman.layoutComponents();

    SpecmanFT.ctrlLeftClick(diagram);

    clickItem(PlantUmlContextMenu.popupMenu(), PlantUmlContextMenu.DELETE_ITEM);
    assertFalse(hasSecondArea(container), "Delete from PlantUmlContextMenu must remove the diagram");
  }

  private static boolean hasSecondArea(EditContainer container) {
    try {
      return container.getEditAreaAt(1) != null;
    }
    catch (IndexOutOfBoundsException noSecondArea) {
      return false;
    }
  }

  @Test
  void testBreakCatchHoverTooltipUsesCmdOnMacAndCtrlOnWindows() {
    specman.click(CreateWhileStepOpButton.class);
    WhileSchrittView whileStep = specman.findStepByStepID("1", WhileSchrittView.class);
    specman.focus(specman.findSubstep(whileStep, "1.1", EinfacherSchrittView.class));
    BreakSchrittView breakStep = specman.clickNewBreakStep();
    specman.focus(whileStep);
    CatchLinkPanel panel = specman.clickCreateCatchButton();
    specman.choose(panel, breakStep);
    StepnumberLabel breakLabel = specman.findStepnumberLabel(breakStep);

    System.setProperty("os.name", "Mac OS X");
    breakLabel.dispatchEvent(new MouseEvent(breakLabel, MouseEvent.MOUSE_ENTERED, 0L, MouseEvent.CTRL_DOWN_MASK, 1, 1, 0, false));
    assertNull(breakLabel.getToolTipText(), "Ctrl hover on macOS must not show link-follow tooltip");

    breakLabel.dispatchEvent(new MouseEvent(breakLabel, MouseEvent.MOUSE_ENTERED, 0L, MouseEvent.META_DOWN_MASK, 1, 1, 0, false));
    assertEquals(i18n("tooltip.stepnumber.scroll.mac"), breakLabel.getToolTipText());
    breakLabel.dispatchEvent(new MouseEvent(breakLabel, MouseEvent.MOUSE_EXITED, 0L, 0, 1, 1, 0, false));

    System.setProperty("os.name", "Windows 11");
    breakLabel.dispatchEvent(new MouseEvent(breakLabel, MouseEvent.MOUSE_ENTERED, 0L, MouseEvent.META_DOWN_MASK, 1, 1, 0, false));
    assertNull(breakLabel.getToolTipText(), "Meta hover on Windows must not show link-follow tooltip");

    breakLabel.dispatchEvent(new MouseEvent(breakLabel, MouseEvent.MOUSE_ENTERED, 0L, MouseEvent.CTRL_DOWN_MASK, 1, 1, 0, false));
    assertEquals(i18n("tooltip.stepnumber.scroll"), breakLabel.getToolTipText());
  }

  private static void clickItem(JPopupMenu popup, String messageKey) {
    String expected = i18n(messageKey);
    for (Component component : popup.getComponents()) {
      if (component instanceof JMenuItem item && expected.equals(item.getText())) {
        assertTrue(item.isVisible() && item.isEnabled(), "menu item '" + expected + "' must be available");
        item.doClick(0);
        return;
      }
    }
    throw new IllegalStateException("No context menu item '" + expected + "' in " + List.of(popup.getComponents()));
  }
}
