package system.pos.spring.utility;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

@Component
public class MessagePrinter {
    public static void printMessage(Label messageLabel, String message, boolean color) {
        Platform.runLater(() -> {
            messageLabel.setText(message);
            messageLabel.setTextFill(color ? Color.web("#27ae60") : Color.web("#f62b2b"));

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
                messageLabel.setText(""); // Clear the message text after 5 seconds
            }));

            timeline.setCycleCount(1);
            timeline.play();
        });
    }
}