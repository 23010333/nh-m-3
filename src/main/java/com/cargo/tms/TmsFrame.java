package com.cargo.tms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class TmsFrame extends JFrame {
    private final JTextField code = new JTextField();
    private final JTextField customer = new JTextField();
    private final JTextField origin = new JTextField();
    private final JTextField destination = new JTextField();
    private final JTextField weight = new JTextField();
    private final JTextField fee = new JTextField();
    private final JTextField eta = new JTextField();
    private final JComboBox<String> status = new JComboBox<>(new String[]{"Mới tạo", "Đang lấy hàng", "Đang vận chuyển", "Giao thành công", "Hoàn đơn"});

    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Mã đơn","Khách hàng","Tuyến","Khối lượng","ETA","Trạng thái","Phí"}, 0);
    private final JTable table = new JTable(model);
    private final JLabel dashboard = new JLabel();
    private final List<Shipment> shipments = new ArrayList<>();
    private final Path dbFile = Paths.get("shipments.csv");

    public TmsFrame() {
        setTitle("Cargo TMS - NetBeans (Java Swing)");
        setSize(1100, 650);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        loadData();
        refreshAll();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 6, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Tạo vận đơn"));
        panel.add(new JLabel("Mã đơn")); panel.add(new JLabel("Khách hàng")); panel.add(new JLabel("Điểm gửi")); panel.add(new JLabel("Điểm nhận")); panel.add(new JLabel("Khối lượng (kg)")); panel.add(new JLabel("ETA (yyyy-mm-dd)"));
        panel.add(code); panel.add(customer); panel.add(origin); panel.add(destination); panel.add(weight); panel.add(eta);
        panel.add(new JLabel("Phí (VND)")); panel.add(new JLabel("Trạng thái"));
        panel.add(fee); panel.add(status);

        JButton addBtn = new JButton("Thêm vận đơn");
        JButton delBtn = new JButton("Xóa dòng chọn");
        JButton saveBtn = new JButton("Lưu dữ liệu");
        JButton resetBtn = new JButton("Làm mới");

        addBtn.addActionListener(e -> addShipment());
        delBtn.addActionListener(e -> deleteSelected());
        saveBtn.addActionListener(e -> saveData());
        resetBtn.addActionListener(e -> clearForm());

        panel.add(addBtn); panel.add(delBtn); panel.add(saveBtn); panel.add(resetBtn);
        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new BorderLayout());
        dashboard.setBorder(BorderFactory.createTitledBorder("Dashboard"));
        p.add(dashboard, BorderLayout.CENTER);
        return p;
    }

    private void addShipment() {
        try {
            Shipment s = new Shipment(code.getText().trim(), customer.getText().trim(), origin.getText().trim(), destination.getText().trim(),
                    Double.parseDouble(weight.getText().trim()), eta.getText().trim(), (String) status.getSelectedItem(), Double.parseDouble(fee.getText().trim()));
            if (s.code.isBlank() || s.customer.isBlank()) throw new IllegalArgumentException();
            shipments.add(s);
            refreshAll();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        shipments.remove(row);
        refreshAll();
    }

    private void refreshAll() {
        model.setRowCount(0);
        double revenue = 0, totalWeight = 0;
        int inTransit = 0, delivered = 0;
        for (Shipment s : shipments) {
            model.addRow(new Object[]{s.code, s.customer, s.origin + " → " + s.destination, s.weight, s.eta, s.status, String.format("%,.0f", s.fee)});
            revenue += s.fee;
            totalWeight += s.weight;
            if ("Đang vận chuyển".equals(s.status)) inTransit++;
            if ("Giao thành công".equals(s.status)) delivered++;
        }
        dashboard.setText("Tổng đơn: " + shipments.size() + " | Đang vận chuyển: " + inTransit + " | Giao thành công: " + delivered + " | Doanh thu: " + String.format("%,.0f", revenue) + " VND | Tổng khối lượng: " + String.format("%.1f", totalWeight) + " kg");
    }

    private void clearForm() {
        code.setText(""); customer.setText(""); origin.setText(""); destination.setText(""); weight.setText(""); fee.setText(""); eta.setText(""); status.setSelectedIndex(0);
    }

    private void saveData() {
        try (BufferedWriter writer = Files.newBufferedWriter(dbFile)) {
            for (Shipment s : shipments) {
                writer.write(String.join("|", s.code, s.customer, s.origin, s.destination, String.valueOf(s.weight), s.eta, s.status, String.valueOf(s.fee)));
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Đã lưu dữ liệu vào shipments.csv");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lưu dữ liệu thất bại", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        if (!Files.exists(dbFile)) return;
        try {
            for (String line : Files.readAllLines(dbFile)) {
                String[] p = line.split("\\|");
                if (p.length != 8) continue;
                shipments.add(new Shipment(p[0], p[1], p[2], p[3], Double.parseDouble(p[4]), p[5], p[6], Double.parseDouble(p[7])));
            }
        } catch (Exception ignored) {
        }
    }

    private record Shipment(String code, String customer, String origin, String destination, double weight, String eta, String status, double fee) {}
}
