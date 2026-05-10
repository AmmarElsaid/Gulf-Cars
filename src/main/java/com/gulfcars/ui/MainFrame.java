package com.gulfcars.ui;

import com.gulfcars.ui.panels.TablePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    private final JPanel content = new JPanel(new BorderLayout());

    private static final String[][] NAV = {
            {"Dashboard","__dashboard__"},
            {"Reports","__reports__"},
            {"Customers","Customer"},
            {"Vehicles","Vehicle"},
            {"Bookings","Booking"},
            {"Rental Contracts","RentalContract"},
            {"Payments","Payment"},
            {"Branches","Branch"},
            {"Vehicle Branch Assignments","VehicleBranchAssignment"},
            {"Employees","Employee"},
            {"Suppliers","Supplier"},
            {"Promotions","Promotion"},
            {"Applied Promotions","CustomerPromotion"},
            {"Reviews","Review"},
            {"Maintenance","VehicleMaintenance"},
            {"Fuel Logs","FuelLog"},
            {"Insurance","InsurancePolicy"},
            {"Accidents","AccidentReport"},
            {"Fines","Fine"},
            {"Licenses","License"},
    };

    public MainFrame() {
        setTitle("GulfCars — Vehicle Rental Management");
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);

        content.setBackground(Theme.BG);
        content.setBorder(new EmptyBorder(20,20,20,20));
        add(content, BorderLayout.CENTER);

        showDashboard();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,Theme.BORDER),
                new EmptyBorder(14,22,14,22)));

        JLabel logo = new JLabel("⌬  GulfCars");
        logo.setForeground(Theme.PRIMARY);
        logo.setFont(new Font("SansSerif", Font.BOLD, 24));

        JLabel tag = new JLabel("Premium vehicle rentals across the Gulf");
        tag.setForeground(Theme.MUTED);
        tag.setFont(Theme.BODY);
        tag.setBorder(new EmptyBorder(0,16,0,0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(logo); left.add(tag);

        JLabel right = new JLabel("● Connected to MySQL");
        right.setForeground(Theme.ACCENT);
        right.setFont(Theme.BODY);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildSidebar() {
        JPanel side = new JPanel();

        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.SURFACE);
        side.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,0,1,Theme.BORDER),
            new EmptyBorder(18,12,18,12)));
        side.setPreferredSize(new Dimension(250, 800));

        for (String[] item : NAV) {
            side.add(navButton(item[0], item[1]));
            side.add(Box.createVerticalStrut(4));
        }
        side.add(Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(side);
        sp.getViewport().setBackground(Theme.SURFACE);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        // sp.setBorder(BorderFactory.createCompoundBorder(
        //     BorderFactory.createMatteBorder(0,0,0,1,Theme.BORDER),
        //     new EmptyBorder(18,12,18,12)));

        return sp; /*side */
    }

    private JButton navButton(String label, String table) {
        JButton b = new JButton(label);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(220, 38));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBackground(Theme.SURFACE);
        b.setForeground(Theme.TEXT);
        b.setBorder(new EmptyBorder(8,14,8,14));
        b.setFocusPainted(false);
        b.setFont(Theme.BODY);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e){ b.setBackground(Theme.SURFACE_ALT); b.setForeground(Theme.PRIMARY); }
            public void mouseExited (java.awt.event.MouseEvent e){ b.setBackground(Theme.SURFACE);     b.setForeground(Theme.TEXT); }
        });
        b.addActionListener(e -> {
            if ("__dashboard__".equals(table)) showDashboard();
            else if ("__reports__".equals(table)) show(new TablePanel(label, "__reports__"));
            else show(new TablePanel(label, table));
        });
        return b;
    }

    private void show(JComponent c) {
        content.removeAll();
        content.add(c, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private void showDashboard() {
        show(new DashboardPanel());
    }
}
