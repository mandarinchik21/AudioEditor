package app.soundlab.ui;

import app.soundlab.audit.AuditLog;
import app.soundlab.audiotrack.WavePreviewPanel;
import app.soundlab.client.ServerResponse;
import app.soundlab.client.TcpClient;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused"})
class UiCoordinator implements UiBus {
    private final JButton loadFileButton;
    private final JButton convertToMp3Button;
    private final JButton convertToOggButton;
    private final JButton convertToFlacButton;
    private final JButton cutButton;
    private final JButton copyButton;
    private final JButton pasteButton;
    private final JButton deformButton;
    private final JTextField startField;
    private final JTextField endField;
    private final JTextField segmentLabelField;
    private final JButton applyButton;
    private final JLabel fileLabel;
    private final JPanel waveformPanel;
    private final AuditLog logger;
    private final JButton saveButton;
    private final JPanel lastActionsPanel;
    private final JScrollPane scrollPane;
    private final TcpClient tcpClient;

    private File selectedFile;

    public UiCoordinator(JButton loadFileButton, JButton convertToMp3Button, JButton convertToOggButton, JButton convertToFlacButton,
                         JLabel fileLabel, JButton cutButton, JButton copyButton, JButton pasteButton, JButton deformButton,
                         JTextField startField, JTextField endField, JTextField segmentLabelField, JButton applyButton,
                         JPanel waveformPanel, JButton saveButton, AuditLog logger,
                         JPanel lastActionsPanel, JScrollPane scrollPane) {
        this.loadFileButton = loadFileButton;
        this.convertToMp3Button = convertToMp3Button;
        this.convertToOggButton = convertToOggButton;
        this.convertToFlacButton = convertToFlacButton;
        this.fileLabel = fileLabel;
        this.cutButton = cutButton;
        this.copyButton = copyButton;
        this.pasteButton = pasteButton;
        this.deformButton = deformButton;
        this.startField = startField;
        this.endField = endField;
        this.segmentLabelField = segmentLabelField;
        this.applyButton = applyButton;
        this.waveformPanel = waveformPanel;
        this.saveButton = saveButton;
        this.logger = logger;
        this.lastActionsPanel = lastActionsPanel;
        this.scrollPane = scrollPane;
        this.tcpClient = new TcpClient();
        wireEvents();
    }

    private void wireEvents() {
        loadFileButton.addActionListener(e -> notify(loadFileButton, "loadFile"));
        convertToMp3Button.addActionListener(e -> notify(convertToMp3Button, "convertToMp3"));
        convertToOggButton.addActionListener(e -> notify(convertToOggButton, "convertToOgg"));
        convertToFlacButton.addActionListener(e -> notify(convertToFlacButton, "convertToFlac"));
        cutButton.addActionListener(e -> notify(cutButton, "cut"));
        copyButton.addActionListener(e -> notify(copyButton, "copy"));
        pasteButton.addActionListener(e -> notify(pasteButton, "paste"));
        deformButton.addActionListener(e -> notify(deformButton, "deform"));
        applyButton.addActionListener(e -> notify(applyButton, "apply"));
        saveButton.addActionListener(e -> notify(saveButton, "save"));
    }

    @Override
    public void notify(Component sender, String event) {
        switch (event) {
            case "loadFile" -> handleLoadFile();
            case "convertToMp3" -> handleConvert("mp3");
            case "convertToOgg" -> handleConvert("ogg");
            case "convertToFlac" -> handleConvert("flac");
            case "cut" -> handleCut();
            case "copy" -> handleCopy();
            case "paste" -> handlePaste();
            case "deform" -> handleMorph();
            case "apply" -> handleApplyBounds();
            case "save" -> handleSave();
            default -> throw new IllegalArgumentException("Unknown event: " + event);
        }
    }

    private void handleLoadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                long startTime = System.currentTimeMillis();
                System.out.println("[UI] Starting file load via TCP...");
                
                selectedFile = fileChooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();
                System.out.println("[UI] Selected file: " + filePath);
                
                String fileName = selectedFile.getName().toLowerCase();
                if (!fileName.endsWith(".mp3") && !fileName.endsWith(".ogg") && !fileName.endsWith(".flac")) {
                    JOptionPane.showMessageDialog(null, 
                        "Unsupported file format. Please select MP3, OGG, or FLAC file.",
                        "Unsupported Format", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Map<String, Object> params = new HashMap<>();
                params.put("filePath", filePath);
                ServerResponse response = tcpClient.sendRequest("loadFile", params);

                if (!response.isSuccess()) {
                    JOptionPane.showMessageDialog(null, 
                        "Failed to load file: " + response.getMessage(),
                        "Error Loading File", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String title = selectedFile.getName();
                fileLabel.setText(title.length() > 20 ? title.substring(0, 20) + "..." : title);
                
                long beforeWaveform = System.currentTimeMillis();
                System.out.println("[UI] Rendering waveform (TarsosDSP)...");
                renderWaveform(selectedFile);
                long afterWaveform = System.currentTimeMillis();
                System.out.println("[UI] Waveform rendered in " + (afterWaveform - beforeWaveform) + "ms");
                
                logger.fileOpen();
                
                long totalTime = System.currentTimeMillis() - startTime;
                System.out.println("[UI] Total file load time: " + totalTime + "ms");
            } catch (Exception e) {
                logError("Load file", e);
                JOptionPane.showMessageDialog(null, 
                    "Failed to load audio file:\n" + e.getMessage() + 
                    "\n\nMake sure the server is running and ffmpeg is installed.",
                    "Error Loading File", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleConvert(String format) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(null, "Please load a file first.");
            return;
        }
        try {
            Map<String, Object> params = new HashMap<>();
            ServerResponse response = tcpClient.sendRequest("convertTo" + format.substring(0, 1).toUpperCase() + format.substring(1), params);
            
            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(null, "Error during conversion: " + response.getMessage());
                return;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getData();
            String fileName = data != null && data.containsKey("fileName") ? (String) data.get("fileName") : "converted file";
            JOptionPane.showMessageDialog(null, "File converted to " + format.toUpperCase() + " successfully!\n" + fileName);
        } catch (Exception e) {
            logError("Conversion to " + format, e);
            JOptionPane.showMessageDialog(null, "Error during conversion: " + e.getMessage());
        }
    }

    private void handleCut() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(null, "Please load a file first.");
            return;
        }
        try {
            int start = Integer.parseInt(startField.getText());
            int end = Integer.parseInt(endField.getText());
            
            Map<String, Object> params = new HashMap<>();
            params.put("start", (double) start);
            params.put("end", (double) end);
            
            ServerResponse response = tcpClient.sendRequest("cut", params);
            
            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(null, response.getMessage());
                return;
            }
            
            JOptionPane.showMessageDialog(null, "Segment cut successfully.");
        } catch (NumberFormatException e) {
            logError("Cut segment - invalid number", e);
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter numeric values for Start and End.");
        } catch (Exception e) {
            logError("Cut segment", e);
            JOptionPane.showMessageDialog(null, "Error cutting segment: " + e.getMessage());
        }
    }

    private void handleApplyBounds() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(null, "Please load a file first.");
            return;
        }
        try {
            int start = Integer.parseInt(startField.getText());
            int end = Integer.parseInt(endField.getText());
            
            Map<String, Object> params = new HashMap<>();
            params.put("start", (double) start);
            params.put("end", (double) end);
            params.put("label", segmentLabelField.getText());
            
            ServerResponse response = tcpClient.sendRequest("apply", params);
            
            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(null, response.getMessage());
                return;
            }
            
            segmentLabelField.setText("");
            JOptionPane.showMessageDialog(null, "Segment bounds applied and saved successfully.");
        } catch (NumberFormatException e) {
            logError("Apply bounds - invalid number", e);
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter numeric values for Start and End.");
        } catch (Exception e) {
            logError("Apply bounds", e);
            JOptionPane.showMessageDialog(null, "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void handleCopy() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(null, "Please load a file first.");
            return;
        }
        try {
            int start = Integer.parseInt(startField.getText());
            int end = Integer.parseInt(endField.getText());
            
            Map<String, Object> params = new HashMap<>();
            params.put("start", (double) start);
            params.put("end", (double) end);
            
            ServerResponse response = tcpClient.sendRequest("copy", params);
            
            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(null, response.getMessage());
                return;
            }
            
            JOptionPane.showMessageDialog(null, "Segment copied successfully.");
        } catch (NumberFormatException e) {
            logError("Copy segment - invalid number", e);
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter numeric values for Start and End.");
        } catch (Exception e) {
            logError("Copy segment", e);
            JOptionPane.showMessageDialog(null, "Error copying segment: " + e.getMessage());
        }
    }

    private void handlePaste() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(null, "Please load a file first.");
            return;
        }
        try {
            String input = JOptionPane.showInputDialog("Enter paste position (seconds):");
            if (input == null) {
                return;
            }
            int positionInSeconds = Integer.parseInt(input);
            
            Map<String, Object> params = new HashMap<>();
            params.put("position", (double) positionInSeconds);
            
            ServerResponse response = tcpClient.sendRequest("paste", params);
            
            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(null, response.getMessage());
                return;
            }
            
            JOptionPane.showMessageDialog(null, "Segment pasted successfully.");
        } catch (NumberFormatException e) {
            logError("Paste segment - invalid number", e);
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter a numeric value for the paste position.");
        } catch (Exception e) {
            logError("Paste segment", e);
            JOptionPane.showMessageDialog(null, "Error pasting segment: " + e.getMessage());
        }
    }

    private void handleMorph() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(null, "Please load a file first.");
            return;
        }
        try {
            String factorInput = JOptionPane.showInputDialog("Enter deformation factor (e.g., 0.5 or 2.0):");
            if (factorInput == null) {
                return;
            }
            double factor = Double.parseDouble(factorInput);
            int start = Integer.parseInt(startField.getText());
            int end = Integer.parseInt(endField.getText());
            
            Map<String, Object> params = new HashMap<>();
            params.put("start", (double) start);
            params.put("end", (double) end);
            params.put("factor", factor);
            
            ServerResponse response = tcpClient.sendRequest("deform", params);
            
            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(null, response.getMessage());
                return;
            }
            
            JOptionPane.showMessageDialog(null, "Segment deformed successfully.");
        } catch (NumberFormatException e) {
            logError("Morph segment - invalid number", e);
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter numeric values for Start and End.");
        } catch (Exception e) {
            logError("Morph segment", e);
            JOptionPane.showMessageDialog(null, "Error deforming segment: " + e.getMessage());
        }
    }

    private void renderWaveform(File audioFile) {
        waveformPanel.removeAll();
        waveformPanel.setLayout(new BorderLayout());
        waveformPanel.add(new WavePreviewPanel(audioFile), BorderLayout.CENTER);
        waveformPanel.setVisible(true);
        waveformPanel.revalidate();
        waveformPanel.repaint();
    }

    private void handleSave() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(null, "Please load a file first.");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            String filePath = saveFile.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".wav")) {
                filePath += ".wav";
            }
            try {
                Map<String, Object> params = new HashMap<>();
                ServerResponse response = tcpClient.sendRequest("save", params);
                
                if (!response.isSuccess()) {
                    JOptionPane.showMessageDialog(null, response.getMessage());
                    return;
                }
                
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.getData();
                if (data == null || !data.containsKey("audioData")) {
                    JOptionPane.showMessageDialog(null, "No audio data received from server.");
                    return;
                }
                
                String base64Audio = (String) data.get("audioData");
                byte[] audioBytes = java.util.Base64.getDecoder().decode(base64Audio);
                
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath)) {
                    fos.write(audioBytes);
                }
                
                int audioId = data.containsKey("audioId") ? ((Double) data.get("audioId")).intValue() : -1;
                if (audioId != -1) {
                    app.soundlab.dao.WorkspaceDao workspaceDao = new app.soundlab.dao.WorkspaceDao();
                    String workspaceTitle = saveFile.getName();
                    workspaceDao.addWorkspace(workspaceTitle);
                    int workspaceId = workspaceDao.getLastInsertedWorkspaceId();
                    app.soundlab.dao.SegmentDao segmentDao = new app.soundlab.dao.SegmentDao();
                    workspaceDao.addAudioToWorkspace(workspaceId, audioId);
                }
                
                JOptionPane.showMessageDialog(null, "File saved successfully to: " + filePath);
            } catch (Exception e) {
                logError("Save file", e);
                JOptionPane.showMessageDialog(null, "Error saving file: " + e.getMessage());
            }
        }
    }

    private void logError(String context, Exception e) {
        System.err.println("[UI] " + context + " failed: " + e.getMessage());
        e.printStackTrace();
    }
}

