package app.soundlab.ui;

public interface UiNode {
    void operate(int depth);

    default void operate() {
        operate(0);
    }
}

