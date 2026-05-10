package com.gulfcars.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

/** GulfCars design tokens — desert dusk palette with teal accent. */
public class Theme {
    public static final Color BG          = new Color(0x0F1720);
    public static final Color SURFACE     = new Color(0x18222E);
    public static final Color SURFACE_ALT = new Color(0x223040);
    public static final Color BORDER      = new Color(0x2C3B4E);
    public static final Color TEXT        = new Color(0xE8EEF5);
    public static final Color MUTED       = new Color(0x8FA1B5);
    public static final Color PRIMARY     = new Color(0xE9B665); // sand gold
    public static final Color PRIMARY_DK  = new Color(0xC9933E);
    public static final Color ACCENT      = new Color(0x4FD1C5); // teal
    public static final Color DANGER      = new Color(0xE06C75);

    /** Pick the first font name that's actually installed on the user's system.
     *  Roboto first; if missing, fall back to Roboto-like system fonts so we keep
     *  the same Material/Android feel. SansSerif is the absolute last-resort fallback. */
    private static String pickFontFamily() {
        String[] preferred = {
            "Roboto",          // best — install free from fonts.google.com if missing
            "Noto Sans",       // very Roboto-like, ships with many Linux/Android systems
            "Segoe UI",        // Windows default, neutral fallback
            "Helvetica Neue",  // macOS-ish fallback
            "DejaVu Sans",     // common Linux fallback
            "SansSerif"        // logical Java fallback (always present)
        };
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                .getAvailableFontFamilyNames();
        java.util.Set<String> set = new java.util.HashSet<>();
        for (String f : available) set.add(f);
        for (String name : preferred) {
            if (set.contains(name)) return name;
        }
        return "SansSerif";
    }

    public static final String FONT_FAMILY = pickFontFamily();

    public static final Font  DISPLAY = new Font(FONT_FAMILY, Font.BOLD,  25);
    public static final Font  H2      = new Font(FONT_FAMILY, Font.BOLD,  18);
    public static final Font  BODY    = new Font(FONT_FAMILY, Font.PLAIN, 16);
    public static final Font  MONO    = new Font("Monospaced", Font.PLAIN, 14);

    public static void apply() {
        // Apply chosen font family globally so EVERY Swing widget (dialogs, buttons,
        // menus, tooltips, etc.) picks it up by default.
        FontUIResource defaultFont = new FontUIResource(FONT_FAMILY, Font.PLAIN, 16);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, defaultFont);
            }
        }

        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", BODY);
        UIManager.put("TextField.background", SURFACE_ALT);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", PRIMARY);
        UIManager.put("TextField.font", BODY);
        UIManager.put("TextField.border", new EmptyBorder(8,10,8,10));
        UIManager.put("TextArea.background", SURFACE_ALT);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("TextArea.font", BODY);
        UIManager.put("ComboBox.background", SURFACE_ALT);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.font", BODY);
        UIManager.put("Spinner.background", SURFACE_ALT);
        UIManager.put("Spinner.foreground", TEXT);
        UIManager.put("Spinner.font", BODY);
        UIManager.put("Button.font", BODY);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Table.font", BODY);
        UIManager.put("Table.selectionBackground", PRIMARY);
        UIManager.put("Table.selectionForeground", BG);
        UIManager.put("TableHeader.background", SURFACE_ALT);
        UIManager.put("TableHeader.foreground", PRIMARY);
        UIManager.put("TableHeader.font", H2);
        UIManager.put("ScrollPane.background", BG);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("Viewport.background", SURFACE);
        UIManager.put("SplitPane.background", BG);
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PRIMARY);
        b.setForeground(BG);
        b.setFont(H2);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10,18,10,18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(SURFACE_ALT);
        b.setForeground(TEXT);
        b.setFont(BODY);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8,14,8,14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
