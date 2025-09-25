package app.ui;

import app.domain.Project;
import app.service.EditorCoreService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.UUID;

public class ProjectsController {
  @FXML private TableView<Project> table;
  @FXML private TableColumn<Project, String> colName;
  @FXML private TableColumn<Project, Number> colRate;
  @FXML private TableColumn<Project, String> colCreated;

  private final EditorCoreService service = new EditorCoreService();

  @FXML
  public void initialize() {
    colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().name()));
    colRate.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().sampleRate()));
    colCreated.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
      c.getValue().createdAt() == null ? "" : c.getValue().createdAt().toString()));
    refresh();
  }

  private void refresh() {
    table.getItems().setAll(service.listProjects());
  }

  @FXML
  private void onCreate() {
    TextInputDialog nameDlg = new TextInputDialog("New Project " + UUID.randomUUID().toString().substring(0, 8));
    nameDlg.setHeaderText("Вкажіть назву проєкту");
    var name = nameDlg.showAndWait().orElse(null);
    if (name == null || name.isBlank()) return;

    TextInputDialog rateDlg = new TextInputDialog("44100");
    rateDlg.setHeaderText("Sample Rate (Гц)");
    int sr = Integer.parseInt(rateDlg.showAndWait().orElse("44100"));

    service.createProject(name, sr);
    refresh();
  }

  @FXML
  private void onDelete() {
    var sel = table.getSelectionModel().getSelectedItem();
    if (sel == null) return;
    service.deleteProject(sel.id());
    refresh();
  }

  @FXML
  private void onOpen() {
    var sel = table.getSelectionModel().getSelectedItem();
    if (sel == null) return;
    try {
      FXMLLoader fxml = new FXMLLoader(getClass().getResource("/ui/editor.fxml"));
      Parent root = fxml.load();
      EditorController controller = fxml.getController();
      controller.init(sel);
      Stage st = new Stage();
      st.setTitle("Редактор — " + sel.name());
      st.setScene(new Scene(root, 800, 600));
      st.show();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}