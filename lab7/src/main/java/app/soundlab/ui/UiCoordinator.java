package app.soundlab.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

class UiCoordinator implements UiBus {
    private final JButton chooseFileButton;
    private final JButton convertButton;
    private final JButton clearLogButton;
    private final JLabel selectedFileLabel;
    private final JTextArea eventLog;
    private final JProgressBar progressBar;

    private File selectedFile;

    UiCoordinator(JButton chooseFileButton,
                  JButton convertButton,
                  JButton clearLogButton,
                  JLabel selectedFileLabel,
                  JTextArea eventLog,
                  JProgressBar progressBar) {
        this.chooseFileButton = chooseFileButton;
        this.convertButton = convertButton;
        this.clearLogButton = clearLogButton;
        this.selectedFileLabel = selectedFileLabel;
        this.eventLog = eventLog;
        this.progressBar = progressBar;
        wireEvents();
    }

    private void wireEvents() {
        chooseFileButton.addActionListener(e -> notify(chooseFileButton, "chooseFile"));
        convertButton.addActionListener(e -> notify(convertButton, "convert"));
        clearLogButton.addActionListener(e -> notify(clearLogButton, "clearLog"));
    }

    @Override
    public void notify(Component sender, String event) {
        switch (event) {
            case "chooseFile" -> handleChooseFile();
            case "convert" -> handleConvert();
            case "clearLog" -> handleClearLog();
            default -> throw new IllegalArgumentException("Unknown event: " + event);
        }
    }

    private void handleChooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            selectedFileLabel.setText(selectedFile.getName());
            appendLog("Selected file: " + selectedFile.getAbsolutePath());
            convertButton.setEnabled(true);
        } else {
            appendLog("File selection cancelled.");
        }
    }

    private void handleConvert() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(null, "Please choose a file first.");
            return;
        }
        appendLog("Preparing fake conversion for " + selectedFile.getName());
        convertButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("Converting...");
        progressBar.setStringPainted(true);

        // Keep demo self-contained: simulate background work
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws InterruptedException {
                Thread.sleep(1500); // fake work
                return null;
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                progressBar.setString("Done");
                appendLog("Finished conversion for " + selectedFile.getName());
                convertButton.setEnabled(true);
            }
        }.execute();
    }

    private void handleClearLog() {
        eventLog.setText("");
        progressBar.setValue(0);
        progressBar.setString("");
        appendLog("Log cleared.");
    }

    private void appendLog(String message) {
        eventLog.append(message + System.lineSeparator());
    }
}

