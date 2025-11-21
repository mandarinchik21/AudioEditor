package app.soundlab.ui;

public class UiLeaf implements UiNode {
    private final String name;

    public UiLeaf(String name) {
        this.name = name;
    }

    @Override
    public void operate(int depth) {
        System.out.println("  ".repeat(depth) + "- " + name);
    }
}

