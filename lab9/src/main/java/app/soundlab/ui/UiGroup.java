package app.soundlab.ui;

import java.util.ArrayList;
import java.util.List;

public class UiGroup implements UiNode {
    private final List<UiNode> children = new ArrayList<>();

    public void add(UiNode node) {
        children.add(node);
    }

    public void remove(UiNode node) {
        children.remove(node);
    }

    @Override
    public void operate() {
        for (UiNode child : children) {
            child.operate();
        }
    }
}

