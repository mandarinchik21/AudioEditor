package app.soundlab.audiotrack;

import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class Mp3Asset extends SegmentEntity {
    private final AudioAttributes audioAttributes;
    private final File fileLink;

    public Mp3Asset(String filePath) {
        super(filePath);
        checkFileFormat(filePath);
        this.fileLink = new File(filePath);
        this.audioAttributes = new AudioAttributes();
        audioAttributes.setCodec("libmp3lame");
        audioAttributes.setBitRate(128000);
        audioAttributes.setChannels(2);
        audioAttributes.setSamplingRate(44100);
        try {
            loadAudioDataWithJave(fileLink);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load audio data: " + e.getMessage());
        }
    }

    private void loadAudioDataWithJave(File audioFile) throws Exception {
        long startTime = System.currentTimeMillis();
        System.out.println("[Mp3Asset] Starting JAVE conversion: " + audioFile.getName());
        
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat("wav");
        attrs.setAudioAttributes(audio);
        File tempWav = File.createTempFile("temp_audio", ".wav");
        
        long beforeEncode = System.currentTimeMillis();
        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(audioFile), tempWav, attrs);
        long afterEncode = System.currentTimeMillis();
        System.out.println("[Mp3Asset] JAVE encode (MP3→WAV) took: " + (afterEncode - beforeEncode) + "ms");
        
        long beforePcm = System.currentTimeMillis();
        loadPcmData(tempWav);
        long afterPcm = System.currentTimeMillis();
        System.out.println("[Mp3Asset] PCM data load took: " + (afterPcm - beforePcm) + "ms");
        
        if (!tempWav.delete()) {
            tempWav.deleteOnExit();
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("[Mp3Asset] Total load time: " + totalTime + "ms");
    }
    
    private void loadPcmData(File wavFile) throws Exception {
        System.out.println("[Mp3Asset] loadPcmData: Starting, file size: " + wavFile.length() + " bytes");
        
        // Validate it's a WAV file
        try (java.io.FileInputStream validateFis = new java.io.FileInputStream(wavFile)) {
            byte[] riffHeader = new byte[12];
            if (validateFis.read(riffHeader) < 12) {
                throw new Exception("Invalid WAV file: too small");
            }
            if (riffHeader[0] != 'R' || riffHeader[1] != 'I' || 
                riffHeader[2] != 'F' || riffHeader[3] != 'F') {
                throw new Exception("Invalid WAV file: missing RIFF header");
            }
            if (riffHeader[8] != 'W' || riffHeader[9] != 'A' || 
                riffHeader[10] != 'V' || riffHeader[11] != 'E') {
                throw new Exception("Invalid WAV file: missing WAVE format");
            }
        }
        
        // Find data chunk offset
        int dataOffset = findDataChunk(wavFile);
        System.out.println("[Mp3Asset] loadPcmData: Data chunk found at offset " + dataOffset);
        
        // Read WAV file as raw bytes, skip WAV header
        try (java.io.FileInputStream fis = new java.io.FileInputStream(wavFile)) {
            // Skip to data chunk
            fis.skip(dataOffset);
            
            // Read data chunk header to get size
            byte[] dataHeader = new byte[8];
            if (fis.read(dataHeader) < 8) {
                throw new Exception("Could not read data chunk header");
            }
            
            // Get actual PCM data size from chunk header
            int dataSize = (dataHeader[4] & 0xFF) | 
                          ((dataHeader[5] & 0xFF) << 8) | 
                          ((dataHeader[6] & 0xFF) << 16) | 
                          ((dataHeader[7] & 0xFF) << 24);
            
            int estimatedSamples = dataSize / 2;
            System.out.println("[Mp3Asset] loadPcmData: Data chunk size: " + dataSize + " bytes, estimated samples: " + estimatedSamples);
            ((java.util.ArrayList<Float>) audioData).ensureCapacity(estimatedSamples);
            
            // Use larger buffer (64KB) for better I/O performance
            byte[] buffer = new byte[65536];
            int bytesRead;
            long totalBytesRead = 0;
            int batchCount = 0;
            
            // Batch process: collect samples in temp array, then addAll() for better performance
            java.util.ArrayList<Float> tempSamples = new java.util.ArrayList<>(32768);
            
            System.out.println("[Mp3Asset] loadPcmData: Starting read loop...");
            while ((bytesRead = fis.read(buffer)) != -1 && totalBytesRead < dataSize) {
                // Don't read beyond data chunk
                int bytesToProcess = Math.min(bytesRead, (int)(dataSize - totalBytesRead));
                totalBytesRead += bytesToProcess;
                
                // Process samples in pairs (16-bit = 2 bytes per sample, little-endian)
                for (int i = 0; i < bytesToProcess - 1; i += 2) {
                    int sample = (buffer[i + 1] << 8) | (buffer[i] & 0xFF);
                    // Convert signed 16-bit to float
                    if (sample > 32767) sample -= 65536;
                    tempSamples.add((float) sample);
                    
                    // Batch add every 32K samples to reduce ArrayList overhead
                    if (tempSamples.size() >= 32768) {
                        audioData.addAll(tempSamples);
                        tempSamples.clear();
                        batchCount++;
                        if (batchCount % 10 == 0) {
                            System.out.println("[Mp3Asset] loadPcmData: Processed " + (batchCount * 32768) + " samples, " + (totalBytesRead / 1024) + " KB read");
                        }
                    }
                }
            }
            
            System.out.println("[Mp3Asset] loadPcmData: Read loop finished, total bytes: " + totalBytesRead);
            
            // Add remaining samples
            if (!tempSamples.isEmpty()) {
                System.out.println("[Mp3Asset] loadPcmData: Adding final " + tempSamples.size() + " samples");
                audioData.addAll(tempSamples);
            }
            
            System.out.println("[Mp3Asset] loadPcmData: Complete! Total samples: " + audioData.size());
        } catch (Exception e) {
            System.err.println("[Mp3Asset] loadPcmData: ERROR - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private int findDataChunk(File wavFile) throws Exception {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(wavFile)) {
            byte[] buffer = new byte[8];
            
            // Skip RIFF header (12 bytes: "RIFF" + size + "WAVE")
            fis.skip(12);
            
            int offset = 12;
            int maxSearch = 1024 * 1024; // Search up to 1MB into file
            
            while (offset < maxSearch) {
                int bytesRead = fis.read(buffer, 0, 8);
                if (bytesRead < 8) {
                    break;
                }
                
                // Check if this is the "data" chunk
                if (buffer[0] == 'd' && buffer[1] == 'a' && 
                    buffer[2] == 't' && buffer[3] == 'a') {
                    System.out.println("[Mp3Asset] Found data chunk at offset: " + offset);
                    return offset;
                }
                
                // Read chunk size (little-endian, bytes 4-7)
                int chunkSize = (buffer[4] & 0xFF) | 
                               ((buffer[5] & 0xFF) << 8) | 
                               ((buffer[6] & 0xFF) << 16) | 
                               ((buffer[7] & 0xFF) << 24);
                
                System.out.println("[Mp3Asset] Skipping chunk at offset " + offset + ", size: " + chunkSize);
                
                // Skip this chunk's data
                long skipped = fis.skip(chunkSize);
                if (skipped != chunkSize) {
                    System.err.println("[Mp3Asset] Warning: Could not skip full chunk, expected " + chunkSize + ", got " + skipped);
                    break;
                }
                
                offset += 8 + chunkSize;
            }
        }
        
        throw new Exception("Could not find data chunk in WAV file");
    }

    private void checkFileFormat(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File does not exist or is not a valid file.");
        }
        String extension = getFileExtension(file);
        if (!"mp3".equalsIgnoreCase(extension)) {
            throw new IllegalArgumentException("Wrong audio format. Expected MP3, but got: " + extension);
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        return (lastIndex > 0) ? name.substring(lastIndex + 1) : "";
    }

    @Override
    public String getFormat() {
        return "MP3";
    }

    @Override
    public File getFileLink() {
        return fileLink;
    }

    @Override
    public void saveAs(String outputFilePath) {
        super.saveAs(outputFilePath);
        try {
            File tempWav = new File(outputFilePath);
            if (!tempWav.exists()) {
                throw new RuntimeException("Temporary WAV file not found.");
            }
            File output = new File(outputFilePath.replace(".wav", ".mp3"));
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("mp3");
            attrs.setAudioAttributes(audioAttributes);
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(tempWav), output, attrs);
            if (!tempWav.delete()) {
                System.err.println("Failed to delete temporary WAV file.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error encoding MP3 file: " + e.getMessage());
        }
    }
}