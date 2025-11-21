package app.soundlab.audiotrack;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class WavePreviewPanel extends JPanel {
    private float[] audioData;

    public WavePreviewPanel(File audioFile) {
        loadAudioFile(audioFile);
    }

    private void loadAudioFile(File audioFile) {
        try {
            var dispatcher = AudioDispatcherFactory.fromPipe(
                    audioFile.getAbsolutePath(), 44100, 1024, 512);

            audioData = new float[0];

            dispatcher.addAudioProcessor(new AudioProcessor() {
                private int bufferPosition = 0;

                @Override
                public boolean process(AudioEvent audioEvent) {
                    float[] buffer = audioEvent.getFloatBuffer();
                    if (audioData.length < bufferPosition + buffer.length) {
                        float[] newAudioData = new float[bufferPosition + buffer.length];
                        System.arraycopy(audioData, 0, newAudioData, 0, audioData.length);
                        audioData = newAudioData;
                    }
                    System.arraycopy(buffer, 0, audioData, bufferPosition, buffer.length);
                    bufferPosition += buffer.length;
                    return true;
                }

                @Override
                public void processingFinished() {
                    // no action needed
                }
            });

            dispatcher.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (audioData == null || audioData.length == 0) {
            g.drawString("No audio loaded", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        int middleY = getHeight() / 2;
        int step = Math.max(audioData.length / getWidth(), 1);
        g.setColor(Color.BLUE);

        for (int i = 0; i < getWidth(); i++) {
            int start = i * step;
            int end = Math.min(start + step, audioData.length);
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;

            for (int j = start; j < end; j++) {
                min = Math.min(min, audioData[j]);
                max = Math.max(max, audioData[j]);
            }

            int yMin = middleY - (int) (min * (middleY - 10));
            int yMax = middleY - (int) (max * (middleY - 10));
            g.drawLine(i, yMin, i, yMax);
        }
    }
}