package specman.metatag.generic;

import specman.graphics.IconReader;
import specman.graphics.SpecmanIcons;
import specman.i18n.IMenuItem;
import specman.metatag.TagMoveDirection;
import specman.undo.UndoableMetaTagRemoved;
import specman.util.MouseEvents;
import specman.view.AbstractSchrittView;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static specman.Specman.editor;

/** Context menu of a {@link GenericMetaTag} at a step, same pattern as the one of the step
 * number: one shared instance, registered as mouse listener of every tag. */
public class MetaTagContextMenu implements MouseListener {
  public static final String EDIT_ITEM = "menu.context.edit";
  public static final String MOVE_LEFT_ITEM = "menu.context.move.left";
  public static final String MOVE_RIGHT_ITEM = "menu.context.move.right";
  public static final String DELETE_ITEM = "menu.context.delete";

  static final MetaTagContextMenu instance = new MetaTagContextMenu();

  private final JPopupMenu popup = new JPopupMenu();
  private final JMenuItem edit = new IMenuItem(EDIT_ITEM);
  private final JMenuItem left = new IMenuItem(MOVE_LEFT_ITEM, icon(SpecmanIcons.MOVE_LEFT));
  private final JMenuItem right = new IMenuItem(MOVE_RIGHT_ITEM, icon(SpecmanIcons.MOVE_RIGHT));
  private GenericMetaTag currentTag;
  private AbstractSchrittView currentStep;

  private MetaTagContextMenu() {
    edit.addActionListener(e -> currentTag.editFreetext());
    left.addActionListener(e -> moveCurrentTag(TagMoveDirection.LEFT));
    right.addActionListener(e -> moveCurrentTag(TagMoveDirection.RIGHT));
    JMenuItem delete = new IMenuItem(DELETE_ITEM, icon(SpecmanIcons.DELETE));
    delete.addActionListener(e -> deleteCurrentTag());
    popup.add(edit);
    popup.add(left);
    popup.add(right);
    popup.add(delete);
  }

  private static Icon icon(String svgPath) {
    return IconReader.readSvgIcon(svgPath, SpecmanIcons.SIZE_SMALL);
  }

  private void moveCurrentTag(TagMoveDirection direction) {
    currentStep.moveTagUDBL(currentTag, direction);
  }

  private void deleteCurrentTag() {
    int index = currentStep.removeTag(currentTag);
    editor().addEdit(new UndoableMetaTagRemoved(currentStep, currentTag, index));
  }

  @Override
  public void mousePressed(MouseEvent e) {
    maybeShowPopup(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    maybeShowPopup(e);
  }

  private void maybeShowPopup(MouseEvent e) {
    if (MouseEvents.isPopupTrigger(e)) {
      GenericMetaTag tag = (GenericMetaTag) e.getComponent();
      AbstractSchrittView step = editor().findStep(tag);
      // A tag outside of a step, like the one in the preview of the configuration dialog, has no menu,
      // and neither has a tag at a deleted step, which can't be changed.
      if (step != null && !tag.isDeleted()) {
        currentTag = tag;
        currentStep = step;
        edit.setVisible(tag.getConfig().allowsFreetext());
        boolean leftAllowed = step.allowsTagMove(tag, TagMoveDirection.LEFT);
        boolean rightAllowed = step.allowsTagMove(tag, TagMoveDirection.RIGHT);
        left.setVisible(leftAllowed || rightAllowed);
        right.setVisible(leftAllowed || rightAllowed);
        left.setEnabled(leftAllowed);
        right.setEnabled(rightAllowed);
        // Not showing means headless in unit tests: The initialized menu stays operable via popupMenu()
        if (e.getComponent().isShowing()) {
          popup.show(e.getComponent(), e.getX(), e.getY());
        }
      }
    }
  }

  /** The menu as initialized for the tag last right-clicked. Allows tests to operate the menu without showing it. */
  public static JPopupMenu popupMenu() {
    return instance.popup;
  }

  @Override public void mouseClicked(MouseEvent e) {}
  @Override public void mouseEntered(MouseEvent e) {}
  @Override public void mouseExited(MouseEvent e) {}
}
