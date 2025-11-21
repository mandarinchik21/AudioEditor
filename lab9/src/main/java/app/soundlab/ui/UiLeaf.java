package app.soundlab.ui;

import javax.swing.*;

public class UiLeaf implements UiNode {
    private final JComponent component;

    public UiLeaf(JComponent component) {
        this.component = component;
    }

    @Override
    public void operate() {
        component.repaint();
    }
}

