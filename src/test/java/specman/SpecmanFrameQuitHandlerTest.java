package specman;

import headless.HeadlessExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.awt.desktop.QuitResponse;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(HeadlessExtension.class)
class SpecmanFrameQuitHandlerTest {

  @Test
  void confirmedQuitDisposesAndPerformsQuit() {
    List<String> calls = new ArrayList<>();
    QuitResponse response = recordingResponse(calls);

    SpecmanFrame.handleQuitRequest(
      () -> {
        calls.add("confirmClose");
        return true;
      },
      () -> calls.add("dispose"),
      response
    );

    assertEquals(List.of("confirmClose", "dispose", "performQuit"), calls);
  }

  @Test
  void cancelledQuitDoesNotDisposeAndCancelsQuit() {
    List<String> calls = new ArrayList<>();
    QuitResponse response = recordingResponse(calls);

    SpecmanFrame.handleQuitRequest(
      () -> {
        calls.add("confirmClose");
        return false;
      },
      () -> calls.add("dispose"),
      response
    );

    assertEquals(List.of("confirmClose", "cancelQuit"), calls);
  }

  private static QuitResponse recordingResponse(List<String> calls) {
    return new QuitResponse() {
      @Override
      public void performQuit() {
        calls.add("performQuit");
      }

      @Override
      public void cancelQuit() {
        calls.add("cancelQuit");
      }
    };
  }
}
