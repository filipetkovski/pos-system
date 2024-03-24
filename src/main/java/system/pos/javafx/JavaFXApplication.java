package system.pos.javafx;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import system.pos.PosApplication;
import system.pos.javafx.stage.StageReadyEvent;

public class JavaFXApplication extends Application {
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        ApplicationContextInitializer<GenericApplicationContext> initializer =
                ac -> {
                    ac.registerBean(Application.class, () -> JavaFXApplication.this);
                    ac.registerBean(Parameters.class, this::getParameters);
                    ac.registerBean(HostServices.class, this::getHostServices);
                };

        this.applicationContext = new SpringApplicationBuilder()
                .sources(PosApplication.class)
                .initializers(initializer)
                .run(getParameters()
                        .getRaw()
                        .toArray(new String[0]));
    }

    @Override
    public void stop() {
        this.applicationContext.close();
        Platform.exit();
    }

    @Override
    public void start(Stage stage) {
        String fxmlPath = "/fxml/layout.fxml";
        this.applicationContext.publishEvent(new StageReadyEvent(stage,fxmlPath));
    }
}
