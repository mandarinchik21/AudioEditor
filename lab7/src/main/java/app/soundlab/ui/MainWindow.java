package app.soundlab.ui;

import javax.swing.*;
import java.awt.*;

public class MainWindow {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::launch);
    }

    private static void launch() {
        JFrame frame = new JFrame("Mediator demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(480, 360);

        JButton chooseFileButton = new JButton("Choose audio...");
        JButton convertButton = new JButton("Convert to MP3");
        JButton clearLogButton = new JButton("Clear log");
        convertButton.setEnabled(false);

        JLabel selectedFileLabel = new JLabel("No file selected");
        selectedFileLabel.setBorder(BorderFactory.createTitledBorder("Selected file"));

        JTextArea eventLog = new JTextArea();
        eventLog.setEditable(false);
        eventLog.setLineWrap(true);
        JScrollPane logScroll = new JScrollPane(eventLog);
        logScroll.setBorder(BorderFactory.createTitledBorder("Mediator events"));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        new UiCoordinator(
                chooseFileButton,
                convertButton,
                clearLogButton,
                selectedFileLabel,
                eventLog,
                progressBar
        );

        JPanel controlPanel = new JPanel(new GridLayout(1, 3, 8, 8));
        controlPanel.add(chooseFileButton);
        controlPanel.add(convertButton);
        controlPanel.add(clearLogButton);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(controlPanel, BorderLayout.NORTH);
        content.add(selectedFileLabel, BorderLayout.CENTER);
        content.add(progressBar, BorderLayout.SOUTH);

        frame.setLayout(new BorderLayout(8, 8));
        frame.add(content, BorderLayout.NORTH);
        frame.add(logScroll, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}