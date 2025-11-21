package app.soundlab.server;

import app.soundlab.audiotrack.*;
import app.soundlab.audioencoder.FlacEncoder;
import app.soundlab.audioencoder.Mp3Encoder;
import app.soundlab.audioencoder.OggEncoder;
import app.soundlab.client.ClientRequest;
import app.soundlab.client.ServerResponse;
import app.soundlab.dao.AudioDao;
import app.soundlab.dao.SegmentDao;
import app.soundlab.dao.WorkspaceDao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class AudioEditorServer {
    private static final int DEFAULT_PORT = 8888;
    private static final Gson gson = new GsonBuilder().create();

    private final int port;
    private ServerSocket serverSocket;
    private boolean running = false;

    public AudioEditorServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        System.out.println("Audio Editor Server started on port " + port);

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
                new ClientHandler(clientSocket).start();
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        System.out.println("Server stopped");
    }

    private class ClientHandler extends Thread {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        
        private SegmentEntity currentTrack;
        private String selectedFilePath;
        private final AudioDao audioDao = new AudioDao();
        private final SegmentDao segmentDao = new SegmentDao();

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                while (true) {
                    try {
                        String requestLengthStr = in.readLine();
                        if (requestLengthStr == null || requestLengthStr.trim().isEmpty()) {
                            System.out.println("Client disconnected or sent empty request");
                            break;
                        }
                        int requestLength = Integer.parseInt(requestLengthStr.trim());

                        char[] buffer = new char[requestLength];
                        int totalRead = 0;
                        while (totalRead < requestLength) {
                            int read = in.read(buffer, totalRead, requestLength - totalRead);
                            if (read == -1) {
                                System.out.println("Unexpected end of stream while reading request");
                                return;
                            }
                            totalRead += read;
                        }
                        
                        in.readLine();

                        String requestJson = new String(buffer, 0, totalRead);
                        ClientRequest request = gson.fromJson(requestJson, ClientRequest.class);
                        System.out.println("Received request: " + request.getCommand());

                        ServerResponse response = processRequest(request);
                        System.out.println("Sending response: success=" + response.isSuccess());

                        String responseJson = gson.toJson(response);
                        out.println(responseJson.length());
                        out.print(responseJson);
                        out.flush();
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing request length: " + e.getMessage());
                        ServerResponse errorResponse = new ServerResponse(false, "Invalid request format", null);
                        String errorJson = gson.toJson(errorResponse);
                        out.println(errorJson.length());
                        out.print(errorJson);
                        out.flush();
                    } catch (Exception e) {
                        System.err.println("Error processing request: " + e.getMessage());
                        e.printStackTrace();
                        ServerResponse errorResponse = new ServerResponse(false, "Server error: " + e.getMessage(), null);
                        String errorJson = gson.toJson(errorResponse);
                        try {
                            out.println(errorJson.length());
                            out.print(errorJson);
                            out.flush();
                        } catch (Exception ex) {
                            System.err.println("Error sending error response: " + ex.getMessage());
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (clientSocket != null) clientSocket.close();
                    System.out.println("Client connection closed");
                } catch (IOException e) {
                    System.err.println("Error closing client connection: " + e.getMessage());
                }
            }
        }

        private ServerResponse processRequest(ClientRequest request) {
            try {
                String command = request.getCommand();
                Map<String, Object> params = request.getParameters() != null ? request.getParameters() : new HashMap<>();

                return switch (command) {
                    case "loadFile" -> handleLoadFile((String) params.get("filePath"));
                    case "convertToMp3" -> handleConvert("mp3");
                    case "convertToOgg" -> handleConvert("ogg");
                    case "convertToFlac" -> handleConvert("flac");
                    case "cut" -> handleCut(
                            ((Double) params.get("start")).intValue(),
                            ((Double) params.get("end")).intValue()
                    );
                    case "copy" -> handleCopy(
                            ((Double) params.get("start")).intValue(),
                            ((Double) params.get("end")).intValue()
                    );
                    case "paste" -> handlePaste(
                            ((Double) params.get("position")).intValue()
                    );
                    case "deform" -> handleMorph(
                            ((Double) params.get("start")).intValue(),
                            ((Double) params.get("end")).intValue(),
                            ((Double) params.get("factor")).doubleValue()
                    );
                    case "apply" -> handleApplyBounds(
                            ((Double) params.get("start")).intValue(),
                            ((Double) params.get("end")).intValue(),
                            (String) params.get("label")
                    );
                    case "save" -> handleSave();
                    case "getDuration" -> handleGetDuration();
                    case "getSamplesBySeconds" -> handleGetSamplesBySeconds(
                            ((Double) params.get("seconds")).intValue()
                    );
                    default -> new ServerResponse(false, "Unknown command: " + command, null);
                };
            } catch (Exception e) {
                return new ServerResponse(false, "Error processing request: " + e.getMessage(), null);
            }
        }

        private ServerResponse handleLoadFile(String filePath) {
            try {
                if (filePath == null) {
                    return new ServerResponse(false, "File path is required", null);
                }

                java.io.File file = new java.io.File(filePath);
                if (!file.exists()) {
                    return new ServerResponse(false, "File does not exist: " + filePath, null);
                }

                String fileName = file.getName().toLowerCase();
                if (!fileName.endsWith(".mp3") && !fileName.endsWith(".ogg") && !fileName.endsWith(".flac")) {
                    return new ServerResponse(false, "Unsupported file format. Please select MP3, OGG, or FLAC file.", null);
                }

                if (!audioDao.exists(filePath)) {
                    String format = filePath.substring(filePath.lastIndexOf('.') + 1);
                    String title = file.getName();
                    audioDao.create(title, format, filePath);
                }

                selectedFilePath = filePath;
                currentTrack = resolveFor(file);

                Map<String, Object> data = new HashMap<>();
                data.put("fileName", file.getName());
                data.put("duration", currentTrack.getDurationSeconds());

                return new ServerResponse(true, "File loaded successfully", data);
            } catch (Exception e) {
                return new ServerResponse(false, "Failed to load file: " + e.getMessage(), null);
            }
        }

        private ServerResponse handleConvert(String format) {
            try {
                if (currentTrack == null) {
                    return new ServerResponse(false, "Please load a file first.", null);
                }

                java.io.File convertedFile;
                switch (format) {
                    case "mp3" -> convertedFile = Mp3Encoder.get().encode(currentTrack);
                    case "ogg" -> convertedFile = OggEncoder.get().encode(currentTrack);
                    case "flac" -> convertedFile = FlacEncoder.get().encode(currentTrack);
                    default -> throw new IllegalArgumentException("Unsupported format: " + format);
                }

                Map<String, Object> data = new HashMap<>();
                data.put("convertedFilePath", convertedFile.getAbsolutePath());
                data.put("fileName", convertedFile.getName());

                return new ServerResponse(true, "File converted to " + format.toUpperCase() + " successfully!", data);
            } catch (Exception e) {
                return new ServerResponse(false, "Error during conversion: " + e.getMessage(), null);
            }
        }

        private ServerResponse handleCut(int start, int end) {
            try {
                if (currentTrack == null) {
                    return new ServerResponse(false, "Please load a file first.", null);
                }

                int trackDuration = currentTrack.getDurationSeconds();
                if (!isSegmentValid(start, end, trackDuration)) {
                    return new ServerResponse(false, "Invalid segment bounds. Ensure 0 <= Start < End <= " + trackDuration, null);
                }

                int startSamples = currentTrack.getSamplesBySeconds(start);
                int endSamples = currentTrack.getSamplesBySeconds(end);
                currentTrack.select(startSamples, endSamples);
                currentTrack.cut();

                return new ServerResponse(true, "Segment cut successfully.", null);
            } catch (Exception e) {
                return new ServerResponse(false, "Error cutting segment: " + e.getMessage(), null);
            }
        }

        private ServerResponse handleCopy(int start, int end) {
            try {
                if (currentTrack == null) {
                    return new ServerResponse(false, "Please load a file first.", null);
                }

                int trackDuration = currentTrack.getDurationSeconds();
                if (!isSegmentValid(start, end, trackDuration)) {
                    return new ServerResponse(false, "Invalid segment bounds. Ensure 0 <= Start < End <= " + trackDuration + ".", null);
                }

                int startSamples = currentTrack.getSamplesBySeconds(start);
                int endSamples = currentTrack.getSamplesBySeconds(end);
                currentTrack.select(startSamples, endSamples);
                currentTrack.copy();

                return new ServerResponse(true, "Segment copied successfully.", null);
            } catch (Exception e) {
                return new ServerResponse(false, "Error copying segment: " + e.getMessage(), null);
            }
        }

        private ServerResponse handlePaste(int position) {
            try {
                if (currentTrack == null) {
                    return new ServerResponse(false, "Please load a file first.", null);
                }

                int samplesPerSecond = currentTrack.getSamplesBySeconds(1);
                currentTrack.paste(position, samplesPerSecond);

                return new ServerResponse(true, "Segment pasted successfully.", null);
            } catch (Exception e) {
                return new ServerResponse(false, "Error pasting segment: " + e.getMessage(), null);
            }
        }

        private ServerResponse handleMorph(int start, int end, double factor) {
            try {
                if (currentTrack == null) {
                    return new ServerResponse(false, "Please load a file first.", null);
                }

                int startSamples = currentTrack.getSamplesBySeconds(start);
                int endSamples = currentTrack.getSamplesBySeconds(end);
                currentTrack.select(startSamples, endSamples);
                currentTrack.morph(factor);

                return new ServerResponse(true, "Segment deformed successfully.", null);
            } catch (Exception e) {
                return new ServerResponse(false, "Error deforming segment: " + e.getMessage(), null);
            }
        }

        private ServerResponse handleApplyBounds(int start, int end, String label) {
            try {
                if (currentTrack == null || selectedFilePath == null) {
                    return new ServerResponse(false, "Please load a file first.", null);
                }

                int trackDuration = currentTrack.getDurationSeconds();
                if (trackDuration == 0) {
                    return new ServerResponse(false, "Audio data is empty. Please load a valid file.", null);
                }

                if (!isSegmentValid(start, end, trackDuration)) {
                    return new ServerResponse(false,
                            String.format("Invalid segment bounds. Ensure 0 <= Start < End <= %d.", trackDuration), null);
                }

                int startSamples = currentTrack.getSamplesBySeconds(start);
                int endSamples = currentTrack.getSamplesBySeconds(end);
                currentTrack.select(startSamples, endSamples);

                int audioId = segmentDao.getAudioIdByPath(selectedFilePath);
                if (audioId == -1) {
                    return new ServerResponse(false, "Failed to find audio ID for the current track.", null);
                }

                String segmentLabel = (label == null || label.isBlank())
                        ? String.format("Segment %ds-%ds", start, end)
                        : label.trim();

                int startMs = Math.toIntExact((long) start * 1000);
                int endMs = Math.toIntExact((long) end * 1000);
                segmentDao.create(audioId, startMs, endMs, segmentLabel);

                return new ServerResponse(true, "Segment bounds applied and saved successfully.", null);
            } catch (Exception e) {
                return new ServerResponse(false, "An unexpected error occurred: " + e.getMessage(), null);
            }
        }

        private ServerResponse handleSave() {
            try {
                if (currentTrack == null || selectedFilePath == null) {
                    return new ServerResponse(false, "Please load a file first.", null);
                }

                byte[] wavBytes = generateWavBytes(currentTrack);
                
                String base64Audio = java.util.Base64.getEncoder().encodeToString(wavBytes);

                int audioId = segmentDao.getAudioIdByPath(selectedFilePath);
                if (audioId == -1) {
                    return new ServerResponse(false, "Failed to find audio ID for the current track.", null);
                }

                Map<String, Object> data = new HashMap<>();
                data.put("audioData", base64Audio);
                data.put("audioId", audioId);

                return new ServerResponse(true, "Audio data ready for saving", data);
            } catch (Exception e) {
                return new ServerResponse(false, "Error preparing file for save: " + e.getMessage(), null);
            }
        }
        
        private byte[] generateWavBytes(SegmentEntity track) throws Exception {
            javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(
                44100, 16, 2, true, false);
            
            int audioDataSize = track.getSize();
            byte[] byteArray = new byte[audioDataSize * 2];
            
            java.lang.reflect.Field audioDataField = SegmentEntity.class.getDeclaredField("audioData");
            audioDataField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<Float> audioData = (java.util.List<Float>) audioDataField.get(track);
            
            for (int i = 0; i < audioData.size(); i++) {
                int sample = Math.round(audioData.get(i));
                byteArray[2 * i] = (byte) (sample & 0xFF);
                byteArray[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
            }
            
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(byteArray);
            javax.sound.sampled.AudioInputStream audioStream = 
                new javax.sound.sampled.AudioInputStream(bais, format, audioDataSize);
            javax.sound.sampled.AudioSystem.write(audioStream, 
                javax.sound.sampled.AudioFileFormat.Type.WAVE, baos);
            audioStream.close();
            
            return baos.toByteArray();
        }

        private ServerResponse handleGetDuration() {
            try {
                if (currentTrack == null) {
                    return new ServerResponse(false, "Please load a file first.", null);
                }

                Map<String, Object> data = new HashMap<>();
                data.put("duration", currentTrack.getDurationSeconds());

                return new ServerResponse(true, "Duration retrieved", data);
            } catch (Exception e) {
                return new ServerResponse(false, "Error getting duration: " + e.getMessage(), null);
            }
        }

        private ServerResponse handleGetSamplesBySeconds(int seconds) {
            try {
                if (currentTrack == null) {
                    return new ServerResponse(false, "Please load a file first.", null);
                }

                Map<String, Object> data = new HashMap<>();
                data.put("samples", currentTrack.getSamplesBySeconds(seconds));

                return new ServerResponse(true, "Samples calculated", data);
            } catch (Exception e) {
                return new ServerResponse(false, "Error calculating samples: " + e.getMessage(), null);
            }
        }

        private SegmentEntity resolveFor(java.io.File file) {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".mp3")) {
                return new Mp3Asset(file.getAbsolutePath());
            } else if (fileName.endsWith(".ogg")) {
                return new OggAsset(file.getAbsolutePath());
            } else if (fileName.endsWith(".flac")) {
                return new FlacAsset(file.getAbsolutePath());
            } else {
                throw new IllegalArgumentException("Unsupported file format: " + fileName);
            }
        }

        private boolean isSegmentValid(int start, int end, int max) {
            return start >= 0 && end <= max && start < end;
        }
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        AudioEditorServer server = new AudioEditorServer(port);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

