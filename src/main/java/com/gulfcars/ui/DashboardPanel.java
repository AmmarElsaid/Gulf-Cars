package com.gulfcars.ui;

import com.gulfcars.db.Database;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        add(hero(), BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 16));
        stats.setOpaque(false);
        stats.setBorder(new EmptyBorder(20,0,0,0));
        stats.add(statCard("Total Vehicles", count("Vehicle"), Theme.PRIMARY));
        stats.add(statCard("Active Customers", count("Customer"), Theme.ACCENT));
        stats.add(statCard("Active Contracts", count("RentalContract WHERE ContractStatus IN ('Active','Confirmed')"), new Color(0x82C7FF)));
        stats.add(statCard("Active Bookings", count("Booking WHERE BookingStatus IN ('Active','Confirmed')"), new Color(0x82C7FF)));
        stats.add(statCard("Branches", count("Branch"), new Color(0xE9A0E9)));

        JPanel south = new JPanel(new BorderLayout(16,16));
        south.setOpaque(false);
        south.add(stats, BorderLayout.NORTH);
        south.add(featureGrid(), BorderLayout.CENTER);

        add(south, BorderLayout.CENTER);
    }

    private JComponent hero() {
        JPanel hero = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0,new Color(0x1B2A3D),getWidth(),getHeight(),new Color(0x4A2E12));
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),24,24);
                g2.setColor(new Color(255,255,255,18));
                for (int i=0;i<6;i++) g2.fillOval(getWidth()-220+i*18, -40+i*10, 240, 240);
                g2.dispose();
            }
        };
        hero.setOpaque(false);
        hero.setBorder(new EmptyBorder(28,32,28,32));
        hero.setPreferredSize(new Dimension(100, 170));

        JLabel title = new JLabel("Welcome to GulfCars");
        title.setForeground(Theme.TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 30));

        JLabel sub = new JLabel("Manage your fleet, customers, contracts and revenue from a single dashboard.");
        sub.setForeground(new Color(0xC8D4E2));
        sub.setFont(Theme.BODY);
        sub.setBorder(new EmptyBorder(8,0,16,0));

        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        title.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        txt.add(title); txt.add(sub);

        hero.add(txt, BorderLayout.WEST);
        return hero;
    }

    private JComponent statCard(String label, String value, Color accent) {
        JPanel card = roundedCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18,20,18,20));

        JLabel l = new JLabel(label.toUpperCase());
        l.setForeground(Theme.MUTED);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel v = new JLabel(value);
        v.setForeground(accent);
        v.setFont(new Font("SansSerif", Font.BOLD, 34));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        l.setAlignmentX(LEFT_ALIGNMENT); v.setAlignmentX(LEFT_ALIGNMENT);
        stack.add(l); stack.add(Box.createVerticalStrut(6)); stack.add(v);

        card.add(stack, BorderLayout.CENTER);
        return card;
    }

    private JComponent featureGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 16));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(20,0,0,0));
        grid.add(infoCard("Fleet Operations","Vehicles, branches, suppliers, maintenance & fuel logs — all in one place."));
        grid.add(infoCard("Customer Lifecycle","Bookings, contracts, loyalty promotions and reviews tracked end-to-end."));
        grid.add(infoCard("Financials & Risk","Payments by type, fines, accidents and insurance coverage tracked."));
        return grid;
    }

    private JComponent infoCard(String t, String body) {
        JPanel c = roundedCard();
        c.setLayout(new BorderLayout());
        c.setBorder(new EmptyBorder(18,20,18,20));
        JLabel title = new JLabel(t);
        title.setForeground(Theme.PRIMARY);
        title.setFont(Theme.H2);
        JTextArea desc = new JTextArea(body);
        desc.setEditable(false); desc.setLineWrap(true); desc.setWrapStyleWord(true);
        desc.setBackground(Theme.SURFACE); desc.setForeground(Theme.MUTED); desc.setFont(Theme.BODY);
        desc.setBorder(new EmptyBorder(8,0,0,0));
        c.add(title, BorderLayout.NORTH);
        c.add(desc, BorderLayout.CENTER);
        return c;
    }

    private JPanel roundedCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,18,18);
                g2.dispose();
            }
            { setOpaque(false); }
        };
    }

    private String count(String fromClause) {
        try (Connection c = Database.get(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + fromClause)) {
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (Exception e) { return "—"; }
        return "0";
    }
}
