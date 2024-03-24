package system.pos.javafx.stage;

import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StageReadyEvent extends ApplicationEvent {
    private final String fxmlPath;
    public StageReadyEvent(Stage source, String fxmlPath) {
        super(source);
        this.fxmlPath = fxmlPath;
    }

    public Stage getStage() {
        return (Stage) getSource();
    }

}
