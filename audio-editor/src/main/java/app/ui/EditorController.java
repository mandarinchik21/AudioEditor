package app.ui;

import app.domain.AudioAsset;
import app.domain.Project;
import app.service.EditorCoreService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.nio.file.Path;

public class EditorController {
  @FXML private Label lblProject;
  @FXML private ListView<String> listAssets;

  private final EditorCoreService service = new EditorCoreService();
  private Project project;

  public void init(Project p) {
    this.project = p;
    lblProject.setText(p.name() + "  •  " + p.sampleRate() + " Hz");
    refresh();
  }

  private void refresh() {
    var items = FXCollections.<String>observableArrayList();
    for (AudioAsset a : service.listAssets(project.id())) {
      items.add(Path.of(a.path()).getFileName().toString() + "  [" + a.format() + "]");
    }
    listAssets.setItems(items);
  }

  @FXML
  private void onImport() {
    FileChooser fc = new FileChooser();
    fc.getExtensionFilters().addAll(
      new FileChooser.ExtensionFilter("Audio", "*.wav", "*.mp3", "*.flac", "*.ogg"),
      new FileChooser.ExtensionFilter("All files", "*.*"));
    var file = fc.showOpenDialog(listAssets.getScene().getWindow());
    if (file == null) return;

    service.importFile(project.id(), file.toPath());
    refresh();
  }

  @FXML
  private void onExport() {
    new Alert(Alert.AlertType.INFORMATION, "Експорт поки як заглушка.").showAndWait();
  }
}

