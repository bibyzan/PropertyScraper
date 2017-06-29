import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Created by Ben Rasmussen on 6/28/2017.
 */
public class PropertyScraperWindow extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setScene(new Scene(null));
		primaryStage.show();
	}

	private class PropertyScraperGUI extends HBox {

	}
}
