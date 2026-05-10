package com.gulfcars.app;

import com.gulfcars.ui.MainFrame;
import com.gulfcars.ui.Theme;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Theme.apply();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
