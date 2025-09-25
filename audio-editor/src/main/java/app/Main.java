package app;

import app.config.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    Database.init();
    Parent root = FXMLLoader.load(getClass().getResource("/ui/projects.fxml"));
    stage.setTitle("Аудіоредактор — Проєкти");
    stage.setScene(new Scene(root, 800, 600));
    stage.show();
  }
  public static void main(String[] args) { launch(args); }
}