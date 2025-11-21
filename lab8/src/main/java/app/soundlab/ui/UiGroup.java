package app.soundlab.ui;

import java.util.ArrayList;
import java.util.List;

public class UiGroup implements UiNode {
    private final String name;
    private final List<UiNode> children = new ArrayList<>();

    public UiGroup(String name) {
        this.name = name;
    }

    public void add(UiNode node) {
        children.add(node);
    }

    public void remove(UiNode node) {
        children.remove(node);
    }

    @Override
    public void operate(int depth) {
        System.out.println("  ".repeat(depth) + "+ " + name);
        for (UiNode child : children) {
            child.operate(depth + 1);
        }
    }
}

