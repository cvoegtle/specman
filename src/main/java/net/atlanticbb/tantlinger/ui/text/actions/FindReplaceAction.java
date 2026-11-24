package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import net.atlanticbb.tantlinger.ui.text.dialogs.TextFinderDialog;
import specman.util.Shortcuts;

public class FindReplaceAction extends BasicEditAction {
  private static final long serialVersionUID = 1L;
  private boolean isReplaceTab;
  private TextFinderDialog dialog;

  public FindReplaceAction(boolean isReplace) {
    super((String)null);
    if (isReplace) {
      this.putValue("Name", i18n.str("replace_"));
      this.putValue("MnemonicKey", Integer.valueOf(i18n.mnem("replace_")));
    } else {
      this.putValue("Name", i18n.str("find_"));
      this.putValue("MnemonicKey", Integer.valueOf(i18n.mnem("find_")));
      this.putValue(Action.ACCELERATOR_KEY, Shortcuts.menuShortcut(KeyEvent.VK_F));
    }

    this.isReplaceTab = isReplace;
  }

  protected void doEdit(ActionEvent e, JEditorPane textComponent) {
    Component c = SwingUtilities.getWindowAncestor(textComponent);
    if (this.dialog == null) {
      if (c instanceof Frame) {
        if (this.isReplaceTab) {
          this.dialog = new TextFinderDialog((Frame)c, 1);
        } else {
          this.dialog = new TextFinderDialog((Frame)c, 0);
        }
      } else {
        if (!(c instanceof Dialog)) {
          return;
        }

        if (this.isReplaceTab) {
          this.dialog = new TextFinderDialog((Dialog)c, 1);
        } else {
          this.dialog = new TextFinderDialog((Dialog)c, 0);
        }
      }
    }
    this.dialog.initSearchCycle(textComponent);

    if (!this.dialog.isVisible()) {
      this.dialog.show(this.isReplaceTab ? 1 : 0);
    }

  }

  protected void updateContextState(JEditorPane editor) {
    if (this.dialog != null) {
      this.dialog.updateContextState(editor);
    }
  }
}
