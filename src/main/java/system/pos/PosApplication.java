package system.pos;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import system.pos.javafx.JavaFXApplication;

@SpringBootApplication
public class PosApplication {

	public static void main(String[] args) {
		Application.launch(JavaFXApplication.class, args);
	}
}
