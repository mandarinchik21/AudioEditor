package app.soundlab.ui;

public class CompositeDemo {
    public static void main(String[] args) {
        UiGroup root = new UiGroup("Studio Dashboard");

        UiGroup toolbar = new UiGroup("Toolbar");
        toolbar.add(new UiLeaf("Open Button"));
        toolbar.add(new UiLeaf("Save Button"));
        toolbar.add(new UiLeaf("Export Button"));

        UiGroup trackPanel = new UiGroup("Track Panel");
        UiGroup trackOne = new UiGroup("Track #1");
        trackOne.add(new UiLeaf("Waveform View"));
        trackOne.add(new UiLeaf("Volume Slider"));
        UiGroup trackTwo = new UiGroup("Track #2");
        trackTwo.add(new UiLeaf("Waveform View"));
        trackTwo.add(new UiLeaf("Volume Slider"));
        UiGroup fxControls = new UiGroup("FX Controls");
        fxControls.add(new UiLeaf("Reverb Toggle"));
        fxControls.add(new UiLeaf("Delay Toggle"));
        trackPanel.add(trackOne);
        trackPanel.add(trackTwo);
        trackPanel.add(fxControls);

        UiGroup statusPanel = new UiGroup("Status Panel");
        statusPanel.add(new UiLeaf("CPU Meter"));
        statusPanel.add(new UiLeaf("Disk Meter"));
        statusPanel.add(new UiLeaf("Project Clock"));

        root.add(toolbar);
        root.add(trackPanel);
        root.add(statusPanel);

        root.operate();
    }
}