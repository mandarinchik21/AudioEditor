package app.soundlab.ui;

import java.awt.*;

interface UiBus {
    void notify(Component sender, String event);
}

