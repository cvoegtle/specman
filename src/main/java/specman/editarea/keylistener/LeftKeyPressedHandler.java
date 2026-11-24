package specman.editarea.keylistener;

import specman.Specman;
import specman.editarea.TextEditArea;
import specman.util.Shortcuts;
import static specman.Specman.editor;

import java.awt.event.KeyEvent;

public class LeftKeyPressedHandler extends AbstractRemovalKeyPressedHandler {
  LeftKeyPressedHandler(TextEditArea textArea, KeyEvent keyEvent) {
    super(textArea, keyEvent);
  }

  void handle() {
    if (Shortcuts.isHistoryNavigationModifierDown(event)) {
      editor().scrollBackwardInEditHistory();
      event.consume();
    }
    else if (skipToStepnumberLinkStart()) {
      event.consume();
    }

  }

}
