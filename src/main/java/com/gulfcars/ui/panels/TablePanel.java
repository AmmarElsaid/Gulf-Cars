package com.gulfcars.ui.panels;

import com.gulfcars.db.Database;
import com.gulfcars.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.time.*;
import java.util.Timer;
import java.util.TimerTask;

/** panel that displays a table and lets users browse, search, insert and delete rows. */
public class TablePanel extends JPanel {

    private final String tableName;
    private final String pretty;
    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel();
    private final PlaceholderTextField search = new PlaceholderTextField("Search...");//JTextField();
    private JPanel display = new JPanel(new BorderLayout());
    private List<String> columns = new ArrayList<>();
    private List<String> tempColumns = new ArrayList<>();
    private List<Integer> sqlTypes = new ArrayList<>();
    private List<Integer> tempTypes = new ArrayList<>();
    private String pkColumn = null;
    private String tempPkCol = null;
    private Boolean isUnique = false; // Used to signal table headers are unique(joined columns)
    private Boolean isAutoIncr = false; // Used to determine if pk is auto increment
    private Boolean isTempAutoIncr = false; // Used to determine if pk of scope insertion pk is auto increment
    private int selRow;

    public TablePanel(String pretty, String tableName) {
        this.pretty = pretty;
        this.tableName = tableName;
        this.isUnique = false;
        this.isAutoIncr = false;
        this.isTempAutoIncr = false;
        this.selRow = -1;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);

        // Special pseudo-table: render a Reports view instead of the standard table editor
        if ("__reports__".equals(tableName)) {
            add(buildReportsHeader(), BorderLayout.NORTH);
            add(buildReportsView(), BorderLayout.CENTER);
            return;
        }

        add(buildHeader(), BorderLayout.NORTH);
        display.add(buildTable(), BorderLayout.CENTER);
        add(display, BorderLayout.CENTER);

        loadMetadata();
        String tempTable = "";
        switch (tableName) {
            case "Vehicle": {
                tempTable = "Booking";
                
                display.setBorder(BorderFactory.createEmptyBorder());
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        checkVehicleStates();
                    }
                }, 5000);
                break;
            }
            
            case "Booking": {
                tempTable = "RentalContract";

                this.isUnique = true;
                break;
            }

            case "Fine": {
                tempTable = "Payment";

                this.isUnique = true;
                break;
            }

            default:
                break;
        }

        if (tempTable.length() > 0) {
            loadTempMetadata(tempTable);
        }

        reload();
    }

    private JComponent vehicleCard(String make, String model, String catgry, String status, String pk) {
        JPanel c = roundedCard();
        c.setLayout(new BorderLayout());
        c.setBorder(new EmptyBorder(18,20,18,20));
        JLabel title = new JLabel(make);
        title.setForeground(Theme.PRIMARY);
        title.setFont(Theme.H2);
        JTextArea desc = new JTextArea(model+"\n"+catgry);
        JPanel actionSpace = new JPanel(new BorderLayout());
        actionSpace.setBackground(Theme.SURFACE);
        JLabel state = new JLabel(status);
        state.setFont(new Font("Roboto", getFont().BOLD, 18));
        
        if (status.equals("Available")) {
            state.setForeground(Theme.ACCENT);
            JButton reserveBtn = Theme.primaryButton("Reserve");
            reserveBtn.addActionListener(e -> openTempAddDialog("Booking", "VehicleID", pk));
            actionSpace.add(reserveBtn, BorderLayout.EAST);
        }
        else {
            if (status.equals("Rented")) {
                state.setForeground(Theme.MUTED);
            }
            else {
                state.setForeground(Theme.DANGER);
                JButton maintenanceDone = Theme.primaryButton("Done");
                maintenanceDone.addActionListener(e -> setMaintained(pk));
                actionSpace.add(maintenanceDone, BorderLayout.EAST);
            }
        }

        actionSpace.add(state, BorderLayout.WEST);

        desc.setEditable(false); desc.setLineWrap(true); desc.setWrapStyleWord(true);
        desc.setBackground(Theme.SURFACE); desc.setForeground(Theme.MUTED); desc.setFont(Theme.BODY);
        desc.setBorder(new EmptyBorder(8,0,0,0));
        c.add(title, BorderLayout.NORTH);
        c.add(desc, BorderLayout.CENTER);
        c.add(actionSpace, BorderLayout.SOUTH);
        c.setPreferredSize(new Dimension(250, 250));
        desc.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e){ c.setBackground(Theme.SURFACE_ALT); c.revalidate(); c.repaint();}
            @Override
            public void mouseExited (java.awt.event.MouseEvent e){ c.setBackground(Theme.SURFACE); c.revalidate(); c.repaint();}
            @Override
            public void mouseClicked (java.awt.event.MouseEvent e) { setVehicleSelection(pk);}
        });
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

    private JComponent buildVehicleFlow() {
        JPanel vhPanel = new JPanel();
        vhPanel.setOpaque(false);
        vhPanel.setBorder(BorderFactory.createEmptyBorder());
        
        JScrollPane sp = new JScrollPane(vhPanel);
        sp.getViewport().setBackground(null);

        for (int i = 0; i < table.getRowCount(); i++) {
            vhPanel.add(vehicleCard(table.getValueAt(i,2).toString(), table.getValueAt(i,3).toString(), table.getValueAt(i,9).toString(), table.getValueAt(i,7).toString(), table.getValueAt(i,0).toString()));    
        }
        
        Integer numRows =(Integer) model.getRowCount() / 3;

        if (numRows % 2 == 0) {
            vhPanel.setLayout(new GridLayout(numRows, 3, 8, 8));
        }
        else {
            if (numRows <= 2) {
                vhPanel.setLayout(new GridLayout(2, 3, 8, 8));
            }
            else {
                vhPanel.setLayout(new GridLayout(numRows - 1, 3, 8, 8));
            }
        }

        sp.setVisible(true);
        sp.setBorder(BorderFactory.createEmptyBorder());
        vhPanel.revalidate();
        vhPanel.repaint();

        return sp;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12,12));
        header.setOpaque(false);

        JLabel title = new JLabel(pretty);
        title.setForeground(Theme.TEXT);
        title.setFont(Theme.DISPLAY);

        JLabel sub = new JLabel("MySQL table: " + tableName);
        sub.setForeground(Theme.MUTED);
        sub.setFont(Theme.BODY);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        title.setAlignmentX(LEFT_ALIGNMENT); sub.setAlignmentX(LEFT_ALIGNMENT);
        left.add(title); left.add(sub);

        // search.putClientProperty("JTextField.placeholderText","Search...");
        search.setPreferredSize(new Dimension(220, 36));
        search.addActionListener(e -> reload());

        JButton refresh = Theme.ghostButton("Refresh");
        refresh.addActionListener(e -> {checkVehicleStates();});

        JButton add = Theme.primaryButton("+ Add");
        add.addActionListener(e -> openAddDialog());

        JButton del = Theme.ghostButton("Delete");
        del.addActionListener(e -> deleteSelected());

        JButton edit = Theme.ghostButton("Edit");
        edit.addActionListener(e -> openEditDialog());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(search); right.add(refresh); right.add(edit); right.add(del); right.add(add);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildTable() {
        table.setModel(model);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(Theme.BORDER);
        table.setFont(Theme.BODY);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Theme.SURFACE);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        if (tableName == "Vehicle") {
            sp.setVisible(false);
        }
        

        return sp;
    }

    private void loadMetadata() {
        try (Connection c = Database.get()) {
            DatabaseMetaData md = c.getMetaData();
            // IMPORTANT: scope to the current database. Passing null for catalog returns columns
            // from EVERY database that has a table with this name (e.g. sakila.customer would
            // appear alongside gulfcars.Customer), which produces duplicate / unrelated fields.
            String catalog = c.getCatalog();
            try (ResultSet rs = md.getColumns(catalog, null, tableName, null)) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME"));
                    sqlTypes.add(rs.getInt("DATA_TYPE"));
                    if (rs.getString("IS_AUTOINCREMENT").equals("YES")) {
                        isAutoIncr = true;
                    }
                }
            }
            try (ResultSet rs = md.getPrimaryKeys(catalog, null, tableName)) {
                if (rs.next()) pkColumn = rs.getString("COLUMN_NAME");
            }
        } catch (SQLException e) { showError(e); }
    }

    private void loadTempMetadata(String tempTable) {
        try (Connection c = Database.get()) {
            DatabaseMetaData md = c.getMetaData();
            String catalog = c.getCatalog();
            try (ResultSet rs = md.getColumns(catalog, null, tempTable, null)) {
                while (rs.next()) {
                    tempColumns.add(rs.getString("COLUMN_NAME"));
                    tempTypes.add(rs.getInt("DATA_TYPE"));
                    if (rs.getString("IS_AUTOINCREMENT").equals("YES")) {
                        isTempAutoIncr = true;
                    }
                }
            }
            try (ResultSet rs = md.getPrimaryKeys(catalog, null, tempTable)) {
                if (rs.next()) tempPkCol = rs.getString("COLUMN_NAME");
            }
        } catch (SQLException e) { showError(e); }
    }

    private void reload() {
        String q = search.getText().trim();
        StringBuilder sql;
        if (isUnique == false) {
            sql = new StringBuilder("SELECT * FROM `").append(tableName).append("`");
         } else {
            switch (tableName) {
                case "Booking": {
                    sql = new StringBuilder("SELECT B.BookingID, Concat(C.FirstName,' ',C.LastName) AS 'Booking For', V.Make, V.Model, B.PickupDate, B.ReturnDate, B.BookingStatus from Booking AS B Inner Join Customer AS C On B.customerID=C.customerID Inner Join Vehicle AS V ON B.VehicleID=V.VehicleID");
                    break;
                }

                case "Fine": {
                    sql = new StringBuilder("SELECT F.FineID, Concat(C.FirstName,' ',C.LastName) AS 'Fine For', V.Make, V.Model, Amount, Reason FROM FINE AS F Inner Join Customer AS C On F.customerID=C.customerID Inner Join Vehicle AS V ON F.VehicleID=V.VehicleID");
                    break;
                }

                default: {
                    sql = new StringBuilder("SELECT * FROM `").append(tableName).append("`");
                    break;
                }
            }
        }
        
        List<Object> params = new ArrayList<>();
        Boolean isAdded = false;
        if (!q.isEmpty() && !columns.isEmpty()) {
            sql.append(" WHERE ");
            for (int i=0;i<columns.size();i++) {
                if (i>0 && isAdded && columns.get(i) != "") sql.append(" OR ");
                if (isUnique && (columns.get(i).equals("CustomerID") || columns.get(i).equals("VehicleID"))) {
                    if (columns.get(i).equals("CustomerID")) {
                        sql.append("Concat(C.FirstName,' ',C.LastName) LIKE ?");
                        params.add("%" + q + "%");
                        isAdded = true;
                    }
                    else if (columns.get(i).equals("VehicleID")) {
                        sql.append("Concat(V.Make, ' ', V.Model) LIKE ?");
                        params.add("%" + q + "%");
                        isAdded = true;
                    }
                }
                else {
                    sql.append("CAST(`").append(columns.get(i)).append("` AS CHAR) LIKE ?");
                    params.add("%" + q + "%");
                    isAdded = true;
                }
            }
        }
        
        sql.append(" LIMIT 500");

        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i=0;i<params.size();i++) ps.setObject(i+1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                String[] headers = new String[cols];
                for (int i=0;i<cols;i++) headers[i] = md.getColumnLabel(i+1);
                model.setDataVector(new Object[0][cols], headers);
                while (rs.next()) {
                    Object[] row = new Object[cols];
                    for (int i=0;i<cols;i++) row[i] = rs.getObject(i+1);
                    model.addRow(row);
                }

            }

            if (tableName == "Vehicle") {
                display.removeAll();
                display.add(buildVehicleFlow(), BorderLayout.CENTER);
                display.revalidate();
                display.repaint();
            }
        } catch (SQLException e) { showError(e); }
    }

    private void openTempAddDialog(String tempTable, String targetColumn, String targetValue) {
        if (tempColumns.isEmpty()) return;
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.SURFACE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,8,6,8);
        g.fill = GridBagConstraints.HORIZONTAL;
        JComponent[] fields = new JComponent[tempColumns.size()];

        int startPoint = 0;
        if (isTempAutoIncr) {
            startPoint = 1;
        }

        for (int i=startPoint;i<tempColumns.size();i++) {
            g.gridx=0; g.gridy=i; g.weightx=0;
            JLabel l = new JLabel(tempColumns.get(i));
            l.setForeground(Theme.TEXT);
            form.add(l, g);
            g.gridx=1; g.weightx=1;
            if (isDateType(tempTypes.get(i))) {
                DatePicker dp = new DatePicker();
                fields[i] = dp;
            } else {
                String[] allowed = allowedValuesFor(tempTable, tempColumns.get(i));
                if (allowed != null) {
                    JComboBox<String> cb = new JComboBox<>(allowed);
                    cb.setEditable(false);
                    fields[i] = cb;
                } else {
                    FkOption[] fk = loadFkOptions(tempTable, tempColumns.get(i));
                    if (fk != null && fk.length > 0) {
                        JComboBox<FkOption> cb = new JComboBox<>(fk);
                        cb.setEditable(false);
                        // If the caller pre-fills this column (e.g. VehicleID from Reserve),
                        // pre-select the matching option in the dropdown.
                        if (tempColumns.get(i).equals(targetColumn) && targetValue != null) {
                            for (FkOption opt : fk) {
                                if (opt.id.equals(targetValue)) { cb.setSelectedItem(opt); break; }
                            }
                        }
                        fields[i] = cb;
                    } else {
                        JTextField tf = new JTextField(20);
                        if (tempColumns.get(i).equals(targetColumn)) {
                            tf.setText(targetValue);
                        }
                        fields[i] = tf;
                    }
                }
            }
            form.add(fields[i], g);
        }

        int res = JOptionPane.showConfirmDialog(this, form, "Add row to " + tempTable,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        StringBuilder sql = new StringBuilder("INSERT INTO `").append(tempTable).append("` (");
        StringBuilder ph  = new StringBuilder(" VALUES (");
        List<Object> values = new ArrayList<>();
        boolean first = true;
        
        for (int i=startPoint;i<tempColumns.size();i++) {
            String v = readField(fields[i]);
            if (v.isEmpty()) continue;
            if (!first) { sql.append(","); ph.append(","); }
            sql.append("`").append(tempColumns.get(i)).append("`");
            ph.append("?");
            values.add(coerce(v, tempTypes.get(i)));
            first = false;
        }
        sql.append(")"); ph.append(")");
        if (first) return;

        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql.toString() + ph.toString())) {
            for (int i=0;i<values.size();i++) ps.setObject(i+1, values.get(i));
            ps.executeUpdate();
        } catch (SQLException e) { showError(e); }    

        if (tableName.equals("Vehicle")) {
            checkVehicleStates();
        }
    }

    private void setMaintained(String vehicleID) {
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement("UPDATE `Vehicle` SET Status='Available' WHERE VehicleID=?")) {
            ps.setObject(1, vehicleID);
            ps.executeUpdate();
            reload();
        } catch (SQLException e) { showError(e); }          
    }

    private void checkVehicleStates() {
        LocalDate now = LocalDate.now();
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement("SELECT V.VehicleID, V.Make, V.Status, B.PickupDate, B.ReturnDate, R.StartDate, R.EndDate FROM Vehicle as V LEFT OUTER JOIN Booking as B ON V.VehicleID = B.VehicleID LEFT OUTER JOIN RentalContract AS R ON V.vehicleID=R.vehicleID WHERE B.PickupDate IS NUll OR (CURDATE() NOT BETWEEN B.PickupDate AND B.ReturnDate) OR (CURDATE() NOT BETWEEN R.StartDate AND R.EndDate);")){//"SELECT * FROM Vehicle as V LEFT OUTER JOIN Booking as B ON V.VehicleID = B.VehicleID WHERE B.PickupDate IS NULL OR (CURDATE() NOT BETWEEN B.PickupDate AND B.ReturnDate)")) {
            ResultSet rs2 = ps.executeQuery();
            while (rs2.next()) {
                if (String.valueOf(rs2.getInt(1)).length() > 0) {
                    // JOptionPane.showMessageDialog(this, String.valueOf(rs2.getInt(1)));
                    try (Connection c2 = Database.get(); PreparedStatement ps2 = c2.prepareStatement("UPDATE `Vehicle` SET Status='Available' WHERE VehicleID=?")) {
                        ps2.setObject(1, String.valueOf(rs2.getInt(1)));
                        ps2.executeUpdate();
                    } catch (SQLException e) { showError(e); }  
                }
            }
        } catch (SQLException e) { showError(e); }  
        
        
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement("SELECT * FROM Vehicle as V INNER JOIN VehicleMaintenance as M ON V.VehicleID = M.VehicleID WHERE M.MaintenanceDate=CURDATE()")) {
            ResultSet rs3 = ps.executeQuery();
            while (rs3.next()) {
                if (String.valueOf(rs3.getInt(1)).length() > 0) {
                    try (Connection c2 = Database.get(); PreparedStatement ps2 = c2.prepareStatement("UPDATE `Vehicle` SET Status='Maintenance' WHERE VehicleID=?")) {
                        ps2.setObject(1, String.valueOf(rs3.getInt(1)));
                        ps2.executeUpdate();
                    } catch (SQLException e) { showError(e); }  
                }
            }
        } catch (SQLException e) { showError(e); }  
        
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement("SELECT V.VehicleID, V.Make, V.Status, B.PickupDate, B.ReturnDate, R.StartDate, R.EndDate FROM Vehicle as V LEFT OUTER JOIN Booking as B ON V.VehicleID = B.VehicleID LEFT OUTER JOIN RentalContract AS R ON V.vehicleID=R.vehicleID WHERE B.PickupDate IS NOT NUll OR (CURDATE() BETWEEN B.PickupDate AND B.ReturnDate) OR (CURDATE() BETWEEN R.StartDate AND R.EndDate);")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (String.valueOf(rs.getInt(1)).length() > 0) {
                    // JOptionPane.showMessageDialog(this, String.valueOf(rs.getInt(1)));
                try (Connection c2 = Database.get(); PreparedStatement ps2 = c2.prepareStatement("UPDATE `Vehicle` SET Status='Rented' WHERE VehicleID=?")) {
                    ps2.setObject(1, String.valueOf(rs.getInt(1)));
                    ps2.executeUpdate();
                } catch (SQLException e) { showError(e); }  
            }
            }
        } catch (SQLException e) { showError(e); }  

        reload();
    }

    private void openAddDialog() {
        if (columns.isEmpty()) return;
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.SURFACE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,8,6,8);
        g.fill = GridBagConstraints.HORIZONTAL;
        // Mixed array: each entry is either a JTextField or a DatePicker
        JComponent[] fields = new JComponent[columns.size()];

        int startPoint = 0;
        if (isAutoIncr) {
            startPoint = 1;
        }
        
        for (int i=startPoint;i<columns.size();i++) {
            g.gridx=0; g.gridy=i; g.weightx=0;
            JLabel l = new JLabel(columns.get(i));
            l.setForeground(Theme.TEXT);
            form.add(l, g);
            g.gridx=1; g.weightx=1;
            if (isDateType(sqlTypes.get(i))) {
                fields[i] = new DatePicker();
            } else {
                String[] allowed = allowedValuesFor(tableName, columns.get(i));
                if (allowed != null) {
                    JComboBox<String> cb = new JComboBox<>(allowed);
                    cb.setEditable(false);
                    fields[i] = cb;
                } else {
                    FkOption[] fk = loadFkOptions(tableName, columns.get(i));
                    if (fk != null && fk.length > 0) {
                        JComboBox<FkOption> cb = new JComboBox<>(fk);
                        cb.setEditable(false);
                        fields[i] = cb;
                    } else {
                        fields[i] = new JTextField(20);
                    }
                }
            }
            form.add(fields[i], g);
        }

        int res = JOptionPane.showConfirmDialog(this, form, "Add row to " + tableName,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        StringBuilder sql = new StringBuilder("INSERT INTO `").append(tableName).append("` (");
        StringBuilder ph  = new StringBuilder(" VALUES (");
        List<Object> values = new ArrayList<>();
        boolean first = true;
        for (int i=startPoint;i<columns.size();i++) {
            String v = readField(fields[i]);
            if (v.isEmpty()) continue;
            if (!first) { sql.append(","); ph.append(","); }
            sql.append("`").append(columns.get(i)).append("`");
            ph.append("?");
            values.add(coerce(v, sqlTypes.get(i)));
            first = false;
        }
        sql.append(")"); ph.append(")");
        if (first) return;

        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql.toString() + ph.toString())) {
            for (int i=0;i<values.size();i++) ps.setObject(i+1, values.get(i));
            ps.executeUpdate();
            reload();
        } catch (SQLException e) { showError(e); }
    }

    /** Reads the text value out of a JTextField, DatePicker, or JComboBox. */
    private static String readField(JComponent c) {
        if (c instanceof DatePicker) return ((DatePicker) c).getDateText().trim();
        if (c instanceof JComboBox) {
            Object sel = ((JComboBox<?>) c).getSelectedItem();
            if (sel == null) return "";
            // FK combos hold FkOption — submit the ID, not the label
            if (sel instanceof FkOption) return ((FkOption) sel).id.trim();
            return sel.toString().trim();
        }
        if (c instanceof JTextField) return ((JTextField) c).getText().trim();
        return "";
    }

    /**
     * Returns the list of allowed values for a column that has an enum-style CHECK
     * constraint in the schema, or null if the column is free-text. Centralised here
     * so Add and Edit dialogs both render dropdowns automatically for these columns.
     */
    private static String[] allowedValuesFor(String tableName, String columnName) {
        String key = tableName + "." + columnName;
        switch (key) {
            case "Employee.EmployeeType":
                return new String[]{"RentalAgent","Mechanic"};
            case "RentalAgent.AgentLevel":
                return new String[]{"Junior","Intermediate","Senior"};
            case "Mechanic.CertificationLevel":
                return new String[]{"Basic","Intermediate","Advanced","Master"};
            case "Vehicle.Status":
                return new String[]{"Available","Booked","Rented","Maintenance","Retired"};
            case "Vehicle.CategoryType":
                return new String[]{"Economy","Compact","SUV","Luxury","Van","Truck"};
            case "Booking.BookingStatus":
                return new String[]{"Pending","Confirmed","Cancelled","Completed"};
            case "RentalContract.ContractStatus":
                return new String[]{"Active","Completed","Cancelled"};
            case "Payment.PaymentType":
                return new String[]{"Online","Card","Cash"};
            case "CardPayment.CardType":
                return new String[]{"Visa","Mastercard","Amex","Unknown"};
            case "Review.Rating":
                return new String[]{"1","2","3","4","5"};
            default:
                return null;
        }
    }

    /** Holds a foreign-key option: id is what we send to SQL, label is what the user sees. */
    public static class FkOption {
        public final String id;
        public final String label;
        public FkOption(String id, String label) {
            this.id = id;
            this.label = label;
        }
        @Override public String toString() {
            return label == null || label.isEmpty() ? id : (id + " — " + label);
        }
        @Override public boolean equals(Object o) {
            if (!(o instanceof FkOption)) return false;
            FkOption other = (FkOption) o;
            return id != null && id.equals(other.id);
        }
        @Override public int hashCode() { return id == null ? 0 : id.hashCode(); }
    }

    /**
     * Returns the SQL needed to populate a dropdown for a foreign key column.
     * Result: a 2-element array { idColumnExpr, labelExpr }, plus the FROM table —
     * encoded as "SELECT id, label FROM …" string. Returns null for non-FK columns.
     */
    private static String fkLookupSqlFor(String tableName, String columnName) {
        String key = tableName + "." + columnName;
        // Specific columns first (so e.g. Branch.ManagerID maps to Employee, not Branch)
        switch (key) {
            case "Branch.ManagerID":
                return "SELECT EmployeeID, Name FROM Employee ORDER BY Name";
            case "RentalContract.EmployeeID":
                return "SELECT EmployeeID, Name FROM Employee ORDER BY Name";
            case "VehicleMaintenance.MechanicID":
                return "SELECT E.EmployeeID, E.Name FROM Employee E INNER JOIN Mechanic M ON M.EmployeeID = E.EmployeeID ORDER BY E.Name";
            case "VehicleBranchAssignment.ReturnBranchID":
                return "SELECT BranchID, BranchName FROM Branch ORDER BY BranchName";
            case "Payment.ContractID":
                return "SELECT R.ContractID, CONCAT('Contract #', R.ContractID, ' — ', C.FirstName, ' ', C.LastName) FROM RentalContract R LEFT JOIN Customer C ON C.CustomerID = R.CustomerID ORDER BY R.ContractID DESC";
            case "OnlinePayment.PaymentID":
            case "CardPayment.PaymentID":
            case "CashPayment.PaymentID":
                return "SELECT PaymentID, CONCAT('Payment #', PaymentID, ' — ', Amount) FROM Payment ORDER BY PaymentID DESC";
            default:
                break;
        }
        // Generic name-based fallback
        switch (columnName) {
            case "CustomerID":
                return "SELECT CustomerID, CONCAT(FirstName, ' ', LastName) FROM Customer ORDER BY FirstName, LastName";
            case "VehicleID":
                return "SELECT VehicleID, CONCAT(Make, ' ', Model, ' (', Year, ')') FROM Vehicle ORDER BY Make, Model";
            case "EmployeeID":
                return "SELECT EmployeeID, Name FROM Employee ORDER BY Name";
            case "BranchID":
                return "SELECT BranchID, BranchName FROM Branch ORDER BY BranchName";
            case "SupplierID":
                return "SELECT SupplierID, CompanyName FROM Supplier ORDER BY CompanyName";
            case "PromotionID":
                return "SELECT PromotionID, COALESCE(Description, CONCAT('Promotion #', PromotionID)) FROM Promotion ORDER BY PromotionID";
            default:
                return null;
        }
    }

    /** Loads FK options for a column, or null if it's not a FK. */
    private FkOption[] loadFkOptions(String tableName, String columnName) {
        String sql = fkLookupSqlFor(tableName, columnName);
        if (sql == null) return null;
        List<FkOption> opts = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getObject(1) == null ? "" : rs.getObject(1).toString();
                String label = rs.getString(2);
                opts.add(new FkOption(id, label));
            }
        } catch (SQLException e) { showError(e); return null; }
        return opts.toArray(new FkOption[0]);
    }


    private void openEditDialog() {
        if (columns.isEmpty()) return;

        // For joined views (Booking / Fine), reload metadata only refers to the base table,
        // and the selected row's first column is still the PK because of how reload() builds the SELECT.
        int row = table.getSelectedRow();
        if (selRow > row) row = selRow;
        if (row < 0 || pkColumn == null) {
            JOptionPane.showMessageDialog(this, "Select a row with a primary key to edit.");
            return;
        }

        // Find the PK index in the displayed model (joined views put PK first; native views have it inside `columns`)
        int pkIdxBase = columns.indexOf(pkColumn);
        Object pkVal;
        if (isUnique) {
            // joined views: PK is the first model column (e.g. BookingID, FineID)
            pkVal = model.getValueAt(row, 0);
        } else {
            pkVal = model.getValueAt(row, pkIdxBase);
        }

        // Always re-fetch the FULL row from the base table so we edit ALL columns, not the joined projection.
        // Read by index (with a case-insensitive label map) to avoid driver-specific name resolution issues.
        Object[] currentValues = new Object[columns.size()];
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM `" + tableName + "` WHERE `" + pkColumn + "` = ?")) {
            ps.setObject(1, pkVal);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Record not found.");
                    return;
                }
                ResultSetMetaData rsmd = rs.getMetaData();
                java.util.Map<String,Integer> labelToIdx = new java.util.HashMap<>();
                for (int j = 1; j <= rsmd.getColumnCount(); j++) {
                    labelToIdx.put(rsmd.getColumnLabel(j).toLowerCase(), j);
                }
                for (int i = 0; i < columns.size(); i++) {
                    Integer idxJ = labelToIdx.get(columns.get(i).toLowerCase());
                    currentValues[i] = (idxJ == null) ? null : rs.getObject(idxJ);
                }
            }
        } catch (SQLException e) { showError(e); return; }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.SURFACE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,8,6,8);
        g.fill = GridBagConstraints.HORIZONTAL;
        JComponent[] fields = new JComponent[columns.size()];

        for (int i = 0; i < columns.size(); i++) {
            g.gridx=0; g.gridy=i; g.weightx=0;
            JLabel l = new JLabel(columns.get(i));
            l.setForeground(Theme.TEXT);
            form.add(l, g);
            g.gridx=1; g.weightx=1;
            Object cv = currentValues[i];
            String cvStr = cv == null ? "" : cv.toString();

            if (isDateType(sqlTypes.get(i))) {
                DatePicker dp = new DatePicker();
                dp.setDateText(cvStr);
                if (columns.get(i).equals(pkColumn)) dp.setReadOnly(true);
                fields[i] = dp;
            } else {
                String[] allowed = allowedValuesFor(tableName, columns.get(i));
                if (allowed != null && !columns.get(i).equals(pkColumn)) {
                    JComboBox<String> cb = new JComboBox<>(allowed);
                    cb.setEditable(false);
                    if (!cvStr.isEmpty()) cb.setSelectedItem(cvStr);
                    fields[i] = cb;
                } else {
                    FkOption[] fk = (!columns.get(i).equals(pkColumn))
                            ? loadFkOptions(tableName, columns.get(i)) : null;
                    if (fk != null && fk.length > 0) {
                        JComboBox<FkOption> cb = new JComboBox<>(fk);
                        cb.setEditable(false);
                        // Pre-select the option whose id matches the existing value
                        if (!cvStr.isEmpty()) {
                            for (FkOption opt : fk) {
                                if (opt.id.equals(cvStr)) { cb.setSelectedItem(opt); break; }
                            }
                        }
                        fields[i] = cb;
                    } else {
                        JTextField tf = new JTextField(20);
                        tf.setText(cvStr);
                        if (columns.get(i).equals(pkColumn)) {
                            tf.setEditable(false);
                            tf.setForeground(Theme.MUTED);
                        }
                        fields[i] = tf;
                    }
                }
            }
            form.add(fields[i], g);
        }

        int res = JOptionPane.showConfirmDialog(this, form,
                "Edit row in " + tableName + "  (" + pkColumn + " = " + pkVal + ")",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        StringBuilder sql = new StringBuilder("UPDATE `").append(tableName).append("` SET ");
        List<Object> values = new ArrayList<>();
        boolean first = true;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).equals(pkColumn)) continue; // never update the PK
            String v = readField(fields[i]);
            Object newVal = v.isEmpty() ? null : coerce(v, sqlTypes.get(i));
            // skip column if value unchanged (to avoid touching unrelated fields)
            Object oldVal = currentValues[i];
            String oldStr = oldVal == null ? "" : oldVal.toString();
            // For date columns, normalise the comparison to YYYY-MM-DD
            if (isDateType(sqlTypes.get(i)) && oldStr.length() >= 10) {
                oldStr = oldStr.substring(0, 10);
            }
            if (oldStr.equals(v)) continue;

            if (!first) sql.append(", ");
            sql.append("`").append(columns.get(i)).append("` = ?");
            values.add(newVal);
            first = false;
        }

        if (first) {
            JOptionPane.showMessageDialog(this, "No changes to save.");
            return;
        }

        sql.append(" WHERE `").append(pkColumn).append("` = ?");
        values.add(pkVal);

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.size(); i++) ps.setObject(i+1, values.get(i));
            int updated = ps.executeUpdate();
            JOptionPane.showMessageDialog(this, updated + " row(s) updated.");
            reload();
        } catch (SQLException e) { showError(e); }

        selRow = -1;
    }

    private void setVehicleSelection(String id) {
        for (int i = 0; i < table.getRowCount(); i++) {
            // JOptionPane.showMessageDialog(this, "Selected Row: "+table.getValueAt(i, 0));
            if (table.getValueAt(i, 0).toString().equals(id)) {
                pkColumn = "VehicleID";
                selRow = i;
                // table.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (selRow > row) {
            row = selRow;
        }

        if (row < 0 || pkColumn == null) {
            JOptionPane.showMessageDialog(this, "Select a row with a primary key to delete.");
            return;
        }

        int idx = columns.indexOf(pkColumn);
        Object pk = model.getValueAt(row, idx);
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete " + pkColumn + " = " + pk + " from " + tableName + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM `" + tableName + "` WHERE `" + pkColumn + "` = ?")) {
            ps.setObject(1, pk);
            ps.executeUpdate();
            reload();
        } catch (SQLException e) { showError(e); }

        selRow = -1;
    }

    private Object coerce(String v, int sqlType) {
        try {
            switch (sqlType) {
                case Types.INTEGER: case Types.SMALLINT: case Types.BIGINT: case Types.TINYINT:
                    return Long.parseLong(v);
                case Types.DECIMAL: case Types.NUMERIC: case Types.DOUBLE: case Types.FLOAT: case Types.REAL:
                    return Double.parseDouble(v);
                case Types.DATE: return java.sql.Date.valueOf(v);
                default: return v;
            }
        } catch (Exception ex) { return v; }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
    }

    // ============================================================
    // Reports view — complex multi-table SQL queries (3+ tables)
    // ============================================================

    private JComboBox<String> reportSelector;
    private JTable reportTable;
    private DefaultTableModel reportModel;
    private JLabel reportSummary;

    private static final String[][] REPORTS = {
        // {Display name, description}
        {"Customer Rental Revenue",
         "Total amount each customer has paid across rental contracts (Customer + RentalContract + Payment)"},
        {"Vehicle Performance",
         "Per-vehicle bookings, contracts, revenue and supplier (Vehicle + Supplier + RentalContract + Payment)"},
        {"Branch Operations Summary",
         "Per-branch employee count, vehicles assigned, and revenue handled (Branch + Employee + RentalContract + VehicleBranchAssignment + Payment)"},
        {"Maintenance & Mechanic Workload",
         "Mechanics ranked by jobs handled, total maintenance cost, and vehicles serviced (Employee + Mechanic + VehicleMaintenance + Vehicle)"},
        {"Top Customers by Loyalty & Spend",
         "Customers ranked by combined spend and loyalty (Customer + License + RentalContract + Payment)"},
    };

    private JComponent buildReportsHeader() {
        JPanel header = new JPanel(new BorderLayout(12,12));
        header.setOpaque(false);

        JLabel title = new JLabel(pretty);
        title.setForeground(Theme.TEXT);
        title.setFont(Theme.DISPLAY);

        JLabel sub = new JLabel("Complex multi-table SQL reports");
        sub.setForeground(Theme.MUTED);
        sub.setFont(Theme.BODY);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        title.setAlignmentX(LEFT_ALIGNMENT); sub.setAlignmentX(LEFT_ALIGNMENT);
        left.add(title); left.add(sub);

        String[] names = new String[REPORTS.length];
        for (int i = 0; i < REPORTS.length; i++) names[i] = REPORTS[i][0];
        reportSelector = new JComboBox<>(names);
        reportSelector.setPreferredSize(new Dimension(320, 36));
        reportSelector.addActionListener(e -> runSelectedReport());

        JButton runBtn = Theme.primaryButton("Run Report");
        runBtn.addActionListener(e -> runSelectedReport());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(reportSelector);
        right.add(runBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildReportsView() {
        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setOpaque(false);

        reportSummary = new JLabel(" ");
        reportSummary.setForeground(Theme.MUTED);
        reportSummary.setFont(Theme.BODY);
        reportSummary.setBorder(new EmptyBorder(4, 4, 6, 4));

        reportModel = new DefaultTableModel();
        reportTable = new JTable(reportModel);
        reportTable.setRowHeight(30);
        reportTable.setShowGrid(true);
        reportTable.setGridColor(Theme.BORDER);
        reportTable.setFont(Theme.BODY);
        reportTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane sp = new JScrollPane(reportTable);
        sp.getViewport().setBackground(Theme.SURFACE);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        container.add(reportSummary, BorderLayout.NORTH);
        container.add(sp, BorderLayout.CENTER);

        // run the first report by default
        SwingUtilities.invokeLater(this::runSelectedReport);
        return container;
    }

    private void runSelectedReport() {
        if (reportSelector == null) return;
        int idx = reportSelector.getSelectedIndex();
        if (idx < 0) return;

        String sql;
        switch (idx) {
            case 0: // Customer Rental Revenue — 3 tables
                sql =
                    "SELECT C.CustomerID, " +
                    "       CONCAT(C.FirstName,' ',C.LastName) AS Customer, " +
                    "       C.Email, " +
                    "       COUNT(DISTINCT R.ContractID) AS Contracts, " +
                    "       COALESCE(SUM(P.Amount),0)     AS TotalPaid, " +
                    "       C.LoyaltyPoints " +
                    "FROM Customer C " +
                    "LEFT JOIN RentalContract R ON R.CustomerID = C.CustomerID " +
                    "LEFT JOIN Payment        P ON P.ContractID = R.ContractID " +
                    "GROUP BY C.CustomerID, C.FirstName, C.LastName, C.Email, C.LoyaltyPoints " +
                    "ORDER BY TotalPaid DESC, Contracts DESC";
                break;

            case 1: // Vehicle Performance — 4 tables
                sql =
                    "SELECT V.VehicleID, " +
                    "       CONCAT(V.Make,' ',V.Model,' (',V.Year,')') AS Vehicle, " +
                    "       V.CategoryType, " +
                    "       COALESCE(S.CompanyName,'(no supplier)') AS Supplier, " +
                    "       COUNT(DISTINCT B.BookingID)   AS Bookings, " +
                    "       COUNT(DISTINCT R.ContractID)  AS Contracts, " +
                    "       COALESCE(SUM(P.Amount),0)     AS Revenue, " +
                    "       V.Status " +
                    "FROM Vehicle V " +
                    "LEFT JOIN Supplier       S ON S.SupplierID = V.SupplierID " +
                    "LEFT JOIN Booking        B ON B.VehicleID  = V.VehicleID " +
                    "LEFT JOIN RentalContract R ON R.VehicleID  = V.VehicleID " +
                    "LEFT JOIN Payment        P ON P.ContractID = R.ContractID " +
                    "GROUP BY V.VehicleID, V.Make, V.Model, V.Year, V.CategoryType, S.CompanyName, V.Status " +
                    "ORDER BY Revenue DESC, Contracts DESC";
                break;

            case 2: // Branch Operations Summary — 5 tables
                sql =
                    "SELECT B.BranchID, " +
                    "       B.BranchName, " +
                    "       B.Location, " +
                    "       COUNT(DISTINCT E.EmployeeID)         AS Employees, " +
                    "       COUNT(DISTINCT VBA.VehicleID)        AS VehiclesAssigned, " +
                    "       COUNT(DISTINCT R.ContractID)         AS ContractsHandled, " +
                    "       COALESCE(SUM(P.Amount),0)            AS RevenueHandled " +
                    "FROM Branch B " +
                    "LEFT JOIN Employee                E   ON E.BranchID   = B.BranchID " +
                    "LEFT JOIN VehicleBranchAssignment VBA ON VBA.BranchID = B.BranchID " +
                    "LEFT JOIN RentalContract          R   ON R.EmployeeID = E.EmployeeID " +
                    "LEFT JOIN Payment                 P   ON P.ContractID = R.ContractID " +
                    "GROUP BY B.BranchID, B.BranchName, B.Location " +
                    "ORDER BY RevenueHandled DESC, Employees DESC";
                break;

            case 3: // Maintenance & Mechanic Workload — 4 tables
                sql =
                    "SELECT E.EmployeeID, " +
                    "       E.Name        AS Mechanic, " +
                    "       M.Specialisation, " +
                    "       M.CertificationLevel, " +
                    "       COUNT(DISTINCT VM.MaintenanceID) AS Jobs, " +
                    "       COUNT(DISTINCT V.VehicleID)      AS VehiclesServiced, " +
                    "       COALESCE(SUM(VM.Cost),0)         AS TotalMaintenanceCost " +
                    "FROM Employee E " +
                    "INNER JOIN Mechanic            M  ON M.EmployeeID  = E.EmployeeID " +
                    "LEFT  JOIN VehicleMaintenance  VM ON VM.MechanicID = E.EmployeeID " +
                    "LEFT  JOIN Vehicle             V  ON V.VehicleID   = VM.VehicleID " +
                    "GROUP BY E.EmployeeID, E.Name, M.Specialisation, M.CertificationLevel " +
                    "ORDER BY Jobs DESC, TotalMaintenanceCost DESC";
                break;

            case 4: // Top Customers by Loyalty & Spend — 4 tables
                sql =
                    "SELECT C.CustomerID, " +
                    "       CONCAT(C.FirstName,' ',C.LastName) AS Customer, " +
                    "       COALESCE(L.DriverLicenseNumber,'(none)') AS LicenseNo, " +
                    "       L.LicenseExpiryDate, " +
                    "       COUNT(DISTINCT R.ContractID) AS Contracts, " +
                    "       COALESCE(SUM(P.Amount),0)    AS TotalSpend, " +
                    "       C.LoyaltyPoints " +
                    "FROM Customer C " +
                    "LEFT JOIN License        L ON L.CustomerID  = C.CustomerID " +
                    "LEFT JOIN RentalContract R ON R.CustomerID  = C.CustomerID " +
                    "LEFT JOIN Payment        P ON P.ContractID  = R.ContractID " +
                    "GROUP BY C.CustomerID, C.FirstName, C.LastName, L.DriverLicenseNumber, L.LicenseExpiryDate, C.LoyaltyPoints " +
                    "ORDER BY TotalSpend DESC, C.LoyaltyPoints DESC " +
                    "LIMIT 50";
                break;

            default:
                return;
        }

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            String[] headers = new String[cols];
            for (int i = 0; i < cols; i++) headers[i] = md.getColumnLabel(i+1);
            reportModel.setDataVector(new Object[0][cols], headers);
            int rowCount = 0;
            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int i = 0; i < cols; i++) row[i] = rs.getObject(i+1);
                reportModel.addRow(row);
                rowCount++;
            }
            reportSummary.setText(REPORTS[idx][1] + "  ·  " + rowCount + " row(s)");
        } catch (SQLException e) { showError(e); }
    }

    /** Returns true if the given JDBC type code is a date/timestamp type. */
    private static boolean isDateType(int sqlType) {
        return sqlType == Types.DATE
            || sqlType == Types.TIMESTAMP
            || sqlType == Types.TIMESTAMP_WITH_TIMEZONE;
    }

    /**
     * A date picker built from three dropdowns (Year, Month, Day) so the user
     * can never type an invalid date manually. Renders the date as YYYY-MM-DD
     * for the existing coerce() / SQL DATE pipeline.
     */
    public static class DatePicker extends JPanel {
        private final JComboBox<Integer> yearBox;
        private final JComboBox<String>  monthBox;
        private final JComboBox<Integer> dayBox;
        private final JCheckBox enabled;

        private static final String[] MONTHS = {
            "01 - Jan","02 - Feb","03 - Mar","04 - Apr","05 - May","06 - Jun",
            "07 - Jul","08 - Aug","09 - Sep","10 - Oct","11 - Nov","12 - Dec"
        };

        public DatePicker() {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

            int currentYear = LocalDate.now().getYear();
            Integer[] years = new Integer[120];
            // range: 100 years in the past, 20 years into the future
            for (int i = 0; i < years.length; i++) years[i] = (currentYear - 100) + i;

            yearBox  = new JComboBox<>(years);
            monthBox = new JComboBox<>(MONTHS);
            dayBox   = new JComboBox<>();

            yearBox.setSelectedItem(currentYear);
            monthBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

            // refill day list whenever year/month change so 31/Feb etc. are impossible
            java.awt.event.ActionListener refill = e -> refillDays(true);
            yearBox.addActionListener(refill);
            monthBox.addActionListener(refill);
            refillDays(false);
            dayBox.setSelectedItem(LocalDate.now().getDayOfMonth());

            enabled = new JCheckBox("Set");
            enabled.setSelected(true);
            enabled.setOpaque(false);
            enabled.setForeground(Theme.MUTED);
            enabled.setFont(Theme.BODY);
            enabled.addActionListener(e -> {
                boolean on = enabled.isSelected();
                yearBox.setEnabled(on);
                monthBox.setEnabled(on);
                dayBox.setEnabled(on);
            });

            add(yearBox);
            add(monthBox);
            add(dayBox);
            add(enabled);
        }

        private void refillDays(boolean preserveSelection) {
            Integer prev = (Integer) dayBox.getSelectedItem();
            int year  = (Integer) yearBox.getSelectedItem();
            int month = monthBox.getSelectedIndex() + 1;
            int max = LocalDate.of(year, month, 1).lengthOfMonth();
            dayBox.removeAllItems();
            for (int d = 1; d <= max; d++) dayBox.addItem(d);
            if (preserveSelection && prev != null) {
                dayBox.setSelectedItem(Math.min(prev, max));
            } else {
                dayBox.setSelectedItem(1);
            }
        }

        /** Returns date as "YYYY-MM-DD" or empty string if the user opted out. */
        public String getDateText() {
            if (!enabled.isSelected()) return "";
            int year  = (Integer) yearBox.getSelectedItem();
            int month = monthBox.getSelectedIndex() + 1;
            int day   = (Integer) dayBox.getSelectedItem();
            return String.format("%04d-%02d-%02d", year, month, day);
        }

        /** Sets the picker from "YYYY-MM-DD" (or anything java.sql.Date can parse).
         *  Pass null/empty to leave the picker on today's date but with "Set" off. */
        public void setDateText(String s) {
            if (s == null || s.isEmpty()) {
                enabled.setSelected(false);
                yearBox.setEnabled(false);
                monthBox.setEnabled(false);
                dayBox.setEnabled(false);
                return;
            }
            try {
                // Take only the date portion in case a timestamp string is passed in.
                String datePart = s.length() >= 10 ? s.substring(0, 10) : s;
                LocalDate ld = LocalDate.parse(datePart);
                yearBox.setSelectedItem(ld.getYear());
                monthBox.setSelectedIndex(ld.getMonthValue() - 1);
                refillDays(false);
                dayBox.setSelectedItem(ld.getDayOfMonth());
                enabled.setSelected(true);
            } catch (Exception ignored) { /* leave defaults */ }
        }

        public void setReadOnly(boolean readOnly) {
            yearBox.setEnabled(!readOnly);
            monthBox.setEnabled(!readOnly);
            dayBox.setEnabled(!readOnly);
            enabled.setEnabled(!readOnly);
        }
    }

    public class PlaceholderTextField extends JTextField {
        private String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Roboto",getFont().PLAIN, 16));
                g2.drawString(placeholder, getInsets().left + 2, g.getFontMetrics().getMaxAscent() + getInsets().top + 1);
                
                g2.dispose();
            }
        }
    }
}
