package app.soundlab;

import app.soundlab.audioencoder.AudioEncoder;
import app.soundlab.audioencoder.FlacEncoder;
import app.soundlab.audioencoder.Mp3Encoder;
import app.soundlab.audioencoder.OggEncoder;

public final class App {
    public static void main(String[] args) {
        System.out.println("Singleton encoder demo:");

        demonstrate("lofi-beat.wav", Mp3Encoder.get());
        demonstrate("podcast-episode.wav", FlacEncoder.get());
        demonstrate("game-soundtrack.wav", OggEncoder.get());

        AudioEncoder mp3First = Mp3Encoder.get();
        AudioEncoder mp3Second = Mp3Encoder.get();

        System.out.println();
        System.out.println("Are both MP3 encoder references identical?: " + (mp3First == mp3Second));
        System.out.println("(Same object, because only one encoder instance exists in the JVM.)");
    }

    private static void demonstrate(String resourceName, AudioEncoder encoder) {
        System.out.println();
        System.out.printf("Requesting %s for '%s'%n", encoder.getClass().getSimpleName(), resourceName);
        System.out.println("Result: " + encoder.encode(resourceName));
    }
}

