package app.soundlab.audiotrack;

import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class FlacAsset extends SegmentEntity {
    private final AudioAttributes audioAttributes;
    private final File fileLink;

    public FlacAsset(String filePath) {
        super(filePath);
        checkFileFormat(filePath);
        this.fileLink = new File(filePath);
        this.audioAttributes = new AudioAttributes();
        audioAttributes.setCodec("flac");
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
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat("wav");
        attrs.setAudioAttributes(audio);
        File tempWav = File.createTempFile("temp_audio", ".wav");
        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(audioFile), tempWav, attrs);
        loadPcmData(tempWav);
        if (!tempWav.delete()) {
            tempWav.deleteOnExit();
        }
    }

    private void loadPcmData(File wavFile) throws Exception {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile)) {
            AudioFormat format = audioInputStream.getFormat();
            long frameLength = audioInputStream.getFrameLength();
            
            // Pre-allocate ArrayList to avoid reallocations
            if (frameLength > 0) {
                int estimatedSamples = (int) (frameLength * format.getChannels());
                ((java.util.ArrayList<Float>) audioData).ensureCapacity(estimatedSamples);
            }
            
            // Use larger buffer (64KB) for better I/O performance
            byte[] buffer = new byte[65536];
            int bytesRead;
            
            // Batch process: collect samples in temp array, then addAll() for better performance
            java.util.ArrayList<Float> tempSamples = new java.util.ArrayList<>(32768);
            
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i += 2) {
                    int sample = (buffer[i + 1] << 8) | (buffer[i] & 0xFF);
                    tempSamples.add((float) sample);
                    
                    // Batch add every 32K samples to reduce ArrayList overhead
                    if (tempSamples.size() >= 32768) {
                        audioData.addAll(tempSamples);
                        tempSamples.clear();
                    }
                }
            }
            
            // Add remaining samples
            if (!tempSamples.isEmpty()) {
                audioData.addAll(tempSamples);
            }
        }
    }

    private void checkFileFormat(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File does not exist or is not a valid file.");
        }
        String extension = getFileExtension(file);
        if (!"flac".equalsIgnoreCase(extension)) {
            throw new IllegalArgumentException("Wrong audio format. Expected FLAC, but got: " + extension);
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        return (lastIndex > 0) ? name.substring(lastIndex + 1) : "";
    }

    @Override
    public String getFormat() {
        return "FLAC";
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
            File output = new File(outputFilePath.replace(".wav", ".flac"));
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("flac");
            attrs.setAudioAttributes(audioAttributes);
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(tempWav), output, attrs);
            if (!tempWav.delete()) {
                System.err.println("Failed to delete temporary WAV file.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error encoding FLAC file: " + e.getMessage());
        }
    }
}
