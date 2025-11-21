package app.soundlab.ui;

import app.soundlab.audit.AuditLog;
import app.soundlab.db.DatabaseBootstrap;
import app.soundlab.service.RecentItemsService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainWindow {
    public static void main(String[] args) {
        DatabaseBootstrap.initializeDatabase();

        JFrame frame = new JFrame("Audio Editor");
        frame.setSize(800, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));
        frame.getContentPane().setBackground(new Color(245, 246, 248));

        JButton loadFileButton = new JButton("Open Audio...");

        JLabel fileLabel = new JLabel("No file selected");

        JButton convertToMp3Button = new JButton("To MP3");

        JButton convertToOggButton = new JButton("To OGG");

        JButton convertToFlacButton = new JButton("To FLAC");

        JPanel waveformPanel = new JPanel();
        waveformPanel.setVisible(false);
        waveformPanel.setPreferredSize(new Dimension(600, 160));
        waveformPanel.setBackground(Color.WHITE);
        JButton toggleWaveformButton = new JButton("Show Waveform");
        toggleWaveformButton.addActionListener(e -> {
            boolean isVisible = waveformPanel.isVisible();
            waveformPanel.setVisible(!isVisible);
            toggleWaveformButton.setText(isVisible ? "Show Waveform" : "Hide Waveform");
        });
        waveformPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JButton cutButton = new JButton("Cut");

        JButton copyButton = new JButton("Copy");

        JButton pasteButton = new JButton("Paste");

        JButton deformButton = new JButton("Morph");

        JTextField startField = new JTextField();
        startField.setToolTipText("Start time (s)");

        JTextField endField = new JTextField();
        endField.setToolTipText("End time (s)");

        JTextField segmentLabelField = new JTextField();
        segmentLabelField.setToolTipText("Optional label (e.g., Chorus)");

        JLabel startLabel = new JLabel("Start (s):");

        JLabel endLabel = new JLabel("End (s):");

        JLabel segmentLabel = new JLabel("Label:");

        JButton applyButton = new JButton("Apply Bounds");

        JButton saveButton = new JButton("Save As...");

        AuditLog logger = new AuditLog();
        JPanel lastActionsPanel = new JPanel();
        lastActionsPanel.setLayout(new BoxLayout(lastActionsPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(lastActionsPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Recent Items"));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        RecentItemsService audioService = new RecentItemsService();
        List<String> lastAudios = audioService.getLastAudios(3);
        List<String> lastSegments = audioService.getLastSegments(3);
        List<String> lastWorkspaces = audioService.getLastWorkspaces(3);

        lastActionsPanel.add(new JLabel("Last audios:"));
        for (String audio : lastAudios) {
            lastActionsPanel.add(new JLabel(audio));
        }

        lastActionsPanel.add(Box.createVerticalStrut(10));

        lastActionsPanel.add(new JLabel("Edited segments:"));
        for (String segment : lastSegments) {
            lastActionsPanel.add(new JLabel(segment));
        }

        lastActionsPanel.add(Box.createVerticalStrut(10));

        lastActionsPanel.add(new JLabel("Recent workspaces:"));
        for (String workspace : lastWorkspaces) {
            lastActionsPanel.add(new JLabel(workspace));
        }

        lastActionsPanel.revalidate();
        lastActionsPanel.repaint();

        // Top: File + Convert toolbar
        JToolBar topBar = new JToolBar();
        topBar.setFloatable(false);
        topBar.setBackground(new Color(232, 240, 254));
        topBar.add(loadFileButton);
        topBar.addSeparator();
        topBar.add(new JLabel("Convert:"));
        topBar.add(Box.createHorizontalStrut(6));
        topBar.add(convertToMp3Button);
        topBar.add(convertToOggButton);
        topBar.add(convertToFlacButton);
        topBar.add(Box.createHorizontalGlue());
        topBar.add(toggleWaveformButton);

        // Left: Edit actions
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(6, 1, 8, 8));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Edit"));
        leftPanel.add(cutButton);
        leftPanel.add(copyButton);
        leftPanel.add(pasteButton);
        leftPanel.add(deformButton);
        leftPanel.add(saveButton);

        // Center: Waveform + bounds
        JPanel boundsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        boundsPanel.add(new JLabel("File:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        boundsPanel.add(fileLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        boundsPanel.add(startLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        boundsPanel.add(startField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        boundsPanel.add(endLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        boundsPanel.add(endField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        boundsPanel.add(segmentLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        boundsPanel.add(segmentLabelField, gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        boundsPanel.add(applyButton, gbc);

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.add(boundsPanel, BorderLayout.NORTH);
        centerPanel.add(waveformPanel, BorderLayout.CENTER);

        // Main content area: center panel (70%) and recent items (30%)
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setTopComponent(centerPanel);
        mainSplitPane.setBottomComponent(scrollPane);
        mainSplitPane.setResizeWeight(0.7); // 70% for top, 30% for bottom
        mainSplitPane.setDividerLocation(490); // Approximate 70/30 split for 700px height
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder());

        new UiCoordinator(
                loadFileButton, convertToMp3Button, convertToOggButton, convertToFlacButton,
                fileLabel, cutButton, copyButton, pasteButton, deformButton, startField, endField, segmentLabelField, applyButton,
                waveformPanel, saveButton, logger, lastActionsPanel, scrollPane);

        UiGroup mainContainer = new UiGroup();
        UiGroup buttonPanel = new UiGroup();
        buttonPanel.add(new UiLeaf(loadFileButton));
        buttonPanel.add(new UiLeaf(convertToMp3Button));
        buttonPanel.add(new UiLeaf(convertToOggButton));
        buttonPanel.add(new UiLeaf(convertToFlacButton));
        buttonPanel.add(new UiLeaf(cutButton));
        buttonPanel.add(new UiLeaf(copyButton));
        buttonPanel.add(new UiLeaf(pasteButton));
        buttonPanel.add(new UiLeaf(deformButton));
        buttonPanel.add(new UiLeaf(saveButton));
        mainContainer.add(buttonPanel);
        mainContainer.add(new UiLeaf(fileLabel));
        mainContainer.add(new UiLeaf(startField));
        mainContainer.add(new UiLeaf(endField));
        mainContainer.add(new UiLeaf(segmentLabelField));
        mainContainer.add(new UiLeaf(startLabel));
        mainContainer.add(new UiLeaf(endLabel));
        mainContainer.add(new UiLeaf(segmentLabel));
        mainContainer.add(new UiLeaf(waveformPanel));
        mainContainer.add(new UiLeaf(lastActionsPanel));
        mainContainer.operate();

        frame.add(topBar, BorderLayout.NORTH);
        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(mainSplitPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}