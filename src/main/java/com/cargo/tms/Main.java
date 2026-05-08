package com.cargo.tms;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TmsFrame().setVisible(true));
    }
}
