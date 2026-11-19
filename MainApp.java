package do_an;   
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * PHẦN MỀM QUẢN LÝ BÁN HÀNG - QUÁN CÀ PHÊ & TRÀ SỮA
 *   1. LoginFrame     : đăng nhập
 *   2. SalesPanel     : giao diện bán hàng (chọn món -> giỏ hàng -> thanh toán)
 *   3. ProductPanel   : quản lý thực đơn (thêm / sửa / xóa)
 *   4. HistoryPanel   : lịch sử hóa đơn và doanh thu
 */
public class MainApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

/* ===================================================================== */
/*                         LỚP TIỆN ÍCH DÙNG CHUNG                       */
/* ===================================================================== */
class DB {
    // Windows Authentication (giữ nguyên như bản cũ của bạn)
    static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyQuanCafe;"
            + "integratedSecurity=true;encrypt=false;trustServerCertificate=true;";

    static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    static String money(double v) {
        return String.format("%,.0f đ", v);
    }

    static double parseMoney(String s) {
        String digits = s.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Double.parseDouble(digits);
    }

    static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    static DefaultTableCellRenderer moneyRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object v) {
                setText(v instanceof Number ? money(((Number) v).doubleValue()) : String.valueOf(v));
                setHorizontalAlignment(RIGHT);
            }
        };
    }

    static void error(Component parent, Exception e) {
        JOptionPane.showMessageDialog(parent, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}

/** Phần tử cho JComboBox: hiển thị tên nhưng giữ id */
class Item {
    final int id;
    final String name;

    Item(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

/* ===================================================================== */
/*                              ĐĂNG NHẬP                                */
/* ===================================================================== */
class LoginFrame extends JFrame {
    private final JTextField txtUser = new JTextField(18);
    private final JPasswordField txtPass = new JPasswordField(18);

    LoginFrame() {
        setTitle("Đăng nhập hệ thống bán hàng");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 260);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("QUÁN CÀ PHÊ & TRÀ SỮA", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(111, 78, 55));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;
        form.add(new JLabel("Tên đăng nhập:"), g);
        g.gridx = 1;
        form.add(txtUser, g);
        g.gridx = 0; g.gridy = 1;
        form.add(new JLabel("Mật khẩu:"), g);
        g.gridx = 1;
        form.add(txtPass, g);
        root.add(form, BorderLayout.CENTER);

        JButton btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JPanel south = new JPanel();
        south.add(btnLogin);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(btnLogin); // nhấn Enter để đăng nhập
        btnLogin.addActionListener(e -> login());

        txtUser.setText("admin"); // gợi ý nhanh khi test, có thể xóa dòng này
    }

    private void login() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đăng nhập và mật khẩu!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sql = "SELECT full_name FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DB.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String fullName = rs.getString(1);
                    new MainFrame(fullName).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Sai tên đăng nhập hoặc mật khẩu!", "Đăng nhập thất bại",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            DB.error(this, ex);
        }
    }
}

/* ===================================================================== */
/*                           KHUNG CHƯƠNG TRÌNH                          */
/* ===================================================================== */
class MainFrame extends JFrame {
    MainFrame(String staff) {
        setTitle("Phần mềm quản lý bán hàng - Quán Cà Phê & Trà Sữa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Thanh trên cùng
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(111, 78, 55));
        top.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel lbl = new JLabel("QUẢN LÝ BÁN HÀNG", new CartIcon(26, Color.WHITE), SwingConstants.LEFT);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel lblUser = new JLabel("Nhân viên: " + staff + "   ");
        lblUser.setForeground(Color.WHITE);
        JButton btnLogout = new JButton("Đăng xuất");
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        right.setOpaque(false);
        right.add(lblUser);
        right.add(btnLogout);
        top.add(lbl, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Các tab chức năng
        SalesPanel sales = new SalesPanel(staff);
        ProductPanel products = new ProductPanel();
        HistoryPanel history = new HistoryPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.addTab("Bán hàng", new CartIcon(16, Color.DARK_GRAY), sales);
        tabs.addTab("Quản lý thực đơn", products);
        tabs.addTab("Hóa đơn & doanh thu", history);
        add(tabs, BorderLayout.CENTER);

        // Khi chuyển tab thì nạp lại dữ liệu mới nhất
        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c == sales) sales.reload();
            else if (c == history) history.reload();
        });

        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }
}
/* ===================================================================== */
/*                        TAB 1: GIAO DIỆN BÁN HÀNG                      */
/* ===================================================================== */
class SalesPanel extends JPanel {
    private final String staff;

    private final JComboBox<Item> cbCategory = new JComboBox<>();
    private final JTextField txtSearch = new JTextField(12);
    private final JSpinner spQty = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));

    private final DefaultTableModel menuModel = DB.model("ID", "Tên món", "Giá", "Danh mục", "STT");
    private final JTable menuTable = new JTable(menuModel);

    private final DefaultTableModel cartModel = DB.model("ID", "Tên món", "SL", "Đơn giá", "Thành tiền");
    private final JTable cartTable = new JTable(cartModel);

    private final JLabel lblTotal = new JLabel("0 đ");
    private final JLabel lblChange = new JLabel("0 đ");
    private final JTextField txtPaid = new JTextField(10);

    private boolean loadingCategory = false;

    SalesPanel(String staff) {
        this.staff = staff;
        setLayout(new GridLayout(1, 2, 10, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildMenuSide());
        add(buildCartSide());

        loadCategories();
        loadMenu();
    }

    /* ---------- Bên trái: thực đơn ---------- */
    private JPanel buildMenuSide() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Thực đơn"));

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        filter.add(new JLabel("Danh mục:"));
        filter.add(cbCategory);
        filter.add(new JLabel("Tìm:"));
        filter.add(txtSearch);
        p.add(filter, BorderLayout.NORTH);

        menuTable.setRowHeight(26);
        menuTable.removeColumn(menuTable.getColumnModel().getColumn(0));   // ẩn cột ID
        menuTable.moveColumn(menuTable.getColumnCount() - 1, 0);           // đưa STT lên đầu
        menuTable.getColumnModel().getColumn(0).setMaxWidth(45);
        menuTable.getColumnModel().getColumn(2).setCellRenderer(DB.moneyRenderer());
        p.add(new JScrollPane(menuTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        bottom.add(new JLabel("Số lượng:"));
        spQty.setPreferredSize(new Dimension(60, 28));
        bottom.add(spQty);
        JButton btnAdd = new JButton("Thêm vào giỏ", new CartIcon(20, Color.DARK_GRAY));
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bottom.add(btnAdd);
        p.add(bottom, BorderLayout.SOUTH);

        // Sự kiện
        btnAdd.addActionListener(e -> addToCart());
        cbCategory.addActionListener(e -> {
            if (!loadingCategory) loadMenu();
        });
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadMenu(); }
            public void removeUpdate(DocumentEvent e) { loadMenu(); }
            public void changedUpdate(DocumentEvent e) { loadMenu(); }
        });
        menuTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) addToCart(); // bấm đúp để thêm nhanh
            }
        });
        return p;
    }

    /* ---------- Bên phải: giỏ hàng + thanh toán ---------- */
    private JPanel buildCartSide() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Hóa đơn hiện tại"));

        cartTable.setRowHeight(26);
        // Ẩn cột ID (vẫn lấy được qua model)
        cartTable.removeColumn(cartTable.getColumnModel().getColumn(0));
        cartTable.getColumnModel().getColumn(2).setCellRenderer(DB.moneyRenderer());
        cartTable.getColumnModel().getColumn(3).setCellRenderer(DB.moneyRenderer());
        p.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(5, 5));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JButton btnMinus = new JButton("−");
        JButton btnPlus = new JButton("+");
        JButton btnRemove = new JButton("Xóa dòng");
        JButton btnClear = new JButton("Xóa giỏ");
        btns.add(new JLabel("Số lượng:"));
        btns.add(btnMinus);
        btns.add(btnPlus);
        btns.add(btnRemove);
        btns.add(btnClear);
        south.add(btns, BorderLayout.NORTH);

        JPanel pay = new JPanel(new GridLayout(3, 2, 5, 5));
        JLabel t1 = new JLabel("TỔNG TIỀN:");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotal.setForeground(new Color(200, 30, 30));
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        lblChange.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblChange.setHorizontalAlignment(SwingConstants.RIGHT);
        txtPaid.setHorizontalAlignment(SwingConstants.RIGHT);
        pay.add(t1);
        pay.add(lblTotal);
        pay.add(new JLabel("Tiền khách đưa:"));
        pay.add(txtPaid);
        pay.add(new JLabel("Tiền thừa:"));
        pay.add(lblChange);
        south.add(pay, BorderLayout.CENTER);

        JButton btnPay = new JButton("THANH TOÁN");
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnPay.setBackground(new Color(46, 139, 87));
        btnPay.setForeground(Color.WHITE);
        btnPay.setPreferredSize(new Dimension(100, 42));
        south.add(btnPay, BorderLayout.SOUTH);

        p.add(south, BorderLayout.SOUTH);

        // Sự kiện
        btnPlus.addActionListener(e -> changeQty(1));
        btnMinus.addActionListener(e -> changeQty(-1));
        btnRemove.addActionListener(e -> removeSelected());
        btnClear.addActionListener(e -> clearCart());
        btnPay.addActionListener(e -> checkout());
        txtPaid.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateChange(); }
            public void removeUpdate(DocumentEvent e) { updateChange(); }
            public void changedUpdate(DocumentEvent e) { updateChange(); }
        });
        return p;
    }

    /* ---------- Nạp dữ liệu ---------- */
    void reload() {
        loadCategories();
        loadMenu();
    }

    private void loadCategories() {
        loadingCategory = true;
        Item selected = (Item) cbCategory.getSelectedItem();
        cbCategory.removeAllItems();
        cbCategory.addItem(new Item(0, "Tất cả"));
        try (Connection conn = DB.get();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name FROM categories ORDER BY name")) {
            while (rs.next()) {
                Item it = new Item(rs.getInt(1), rs.getString(2));
                cbCategory.addItem(it);
                if (selected != null && selected.id == it.id) cbCategory.setSelectedItem(it);
            }
        } catch (SQLException ex) {
            DB.error(this, ex);
        }
        loadingCategory = false;
    }

    private void loadMenu() {
        menuModel.setRowCount(0);
        Item cat = (Item) cbCategory.getSelectedItem();
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.name, p.price, c.name FROM products p "
                        + "LEFT JOIN categories c ON p.category_id = c.id "
                        + "WHERE p.status = N'Còn món' AND p.name LIKE ? ");
        if (cat != null && cat.id != 0) sql.append("AND p.category_id = ? ");
        sql.append("ORDER BY p.id");

        try (Connection conn = DB.get(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, "%" + txtSearch.getText().trim() + "%");
            if (cat != null && cat.id != 0) ps.setInt(2, cat.id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt(1));
                    row.add(rs.getString(2));
                    row.add(rs.getDouble(3));
                    row.add(rs.getString(4));
                    row.add(menuModel.getRowCount() + 1);   // STT
                    menuModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            DB.error(this, ex);
        }
    }

    /* ---------- Giỏ hàng ---------- */
    private void addToCart() {
        int r = menuTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một món trong thực đơn!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) menuModel.getValueAt(r, 0);
        String name = (String) menuModel.getValueAt(r, 1);
        double price = (double) menuModel.getValueAt(r, 2);
        int qty = (int) spQty.getValue();

        // Nếu món đã có trong giỏ thì cộng dồn số lượng
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if ((int) cartModel.getValueAt(i, 0) == id) {
                setCartQty(i, (int) cartModel.getValueAt(i, 2) + qty);
                return;
            }
        }
        Vector<Object> row = new Vector<>();
        row.add(id);
        row.add(name);
        row.add(qty);
        row.add(price);
        row.add(price * qty);
        cartModel.addRow(row);
        updateTotal();
    }

    private void setCartQty(int row, int qty) {
        double price = (double) cartModel.getValueAt(row, 3);
        cartModel.setValueAt(qty, row, 2);
        cartModel.setValueAt(price * qty, row, 4);
        updateTotal();
    }

    private void changeQty(int delta) {
        int r = cartTable.getSelectedRow();
        if (r == -1) return;
        int qty = (int) cartModel.getValueAt(r, 2) + delta;
        if (qty <= 0) {
            cartModel.removeRow(r);
            updateTotal();
        } else {
            setCartQty(r, qty);
            cartTable.setRowSelectionInterval(r, r);
        }
    }

    private void removeSelected() {
        int r = cartTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa trong giỏ!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        cartModel.removeRow(r);
        updateTotal();
    }

    private void clearCart() {
        cartModel.setRowCount(0);
        txtPaid.setText("");
        updateTotal();
    }

    private double getTotal() {
        double sum = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            sum += (double) cartModel.getValueAt(i, 4);
        }
        return sum;
    }

    private void updateTotal() {
        lblTotal.setText(DB.money(getTotal()));
        updateChange();
    }

    private void updateChange() {
        double paid = DB.parseMoney(txtPaid.getText());
        double change = paid - getTotal();
        lblChange.setText(paid == 0 ? "0 đ" : DB.money(Math.max(change, 0)));
    }

    /* ---------- Thanh toán: lưu hóa đơn vào CSDL (có transaction) ---------- */
    private void checkout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double total = getTotal();
        double paid = DB.parseMoney(txtPaid.getText());
        if (paid != 0 && paid < total) {
            JOptionPane.showMessageDialog(this, "Tiền khách đưa chưa đủ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Thanh toán hóa đơn " + DB.money(total) + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int orderId;
        try (Connection conn = DB.get()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO orders (staff, total) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, staff);
                    ps.setDouble(2, total);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        orderId = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO order_details (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)")) {
                    for (int i = 0; i < cartModel.getRowCount(); i++) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, (int) cartModel.getValueAt(i, 0));
                        ps.setInt(3, (int) cartModel.getValueAt(i, 2));
                        ps.setDouble(4, (double) cartModel.getValueAt(i, 3));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            DB.error(this, ex);
            return;
        }

        showReceipt(orderId, total, paid);
        clearCart();
    }

    private void showReceipt(int orderId, double total, double paid) {
        StringBuilder sb = new StringBuilder();
        sb.append("        QUÁN CÀ PHÊ & TRÀ SỮA\n");
        sb.append("        HÓA ĐƠN #").append(orderId).append("\n");
        sb.append("Ngày: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date())).append("\n");
        sb.append("Thu ngân: ").append(staff).append("\n");
        sb.append("--------------------------------------\n");
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            sb.append(String.format("%-20s %2d x %,9.0f%n",
                    cartModel.getValueAt(i, 1), (int) cartModel.getValueAt(i, 2),
                    (double) cartModel.getValueAt(i, 3)));
        }
        sb.append("--------------------------------------\n");
        sb.append(String.format("TỔNG TIỀN:   %,.0f đ%n", total));
        if (paid > 0) {
            sb.append(String.format("Khách đưa:   %,.0f đ%n", paid));
            sb.append(String.format("Tiền thừa:   %,.0f đ%n", paid - total));
        }
        sb.append("\n      Cảm ơn quý khách!\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Thanh toán thành công",
                JOptionPane.INFORMATION_MESSAGE);
    }
}

/* ===================================================================== */
/*                    TAB 2: QUẢN LÝ THỰC ĐƠN (CRUD)                     */
/* ===================================================================== */
class ProductPanel extends JPanel {
    private final DefaultTableModel model = DB.model("ID", "Tên món", "Giá (VNĐ)", "Danh mục", "Trạng thái");
    private final JTable table = new JTable(model);

    private final JTextField txtName = new JTextField();
    private final JTextField txtPrice = new JTextField();
    private final JComboBox<Item> cbCategory = new JComboBox<>();
    private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Còn món", "Hết món"});

    ProductPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        table.setRowHeight(26);
        table.getColumnModel().getColumn(2).setCellRenderer(DB.moneyRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Form nhập liệu
        JPanel form = new JPanel(new GridLayout(2, 5, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Thông tin món"));
        form.add(new JLabel("Tên món:"));
        form.add(new JLabel("Giá tiền:"));
        form.add(new JLabel("Danh mục:"));
        form.add(new JLabel("Trạng thái:"));
        form.add(new JLabel(""));
        form.add(txtName);
        form.add(txtPrice);
        form.add(cbCategory);
        form.add(cbStatus);
        JButton btnNewCat = new JButton("+ Danh mục");
        form.add(btnNewCat);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnAdd = new JButton("Thêm món");
        JButton btnUpdate = new JButton("Cập nhật");
        JButton btnDelete = new JButton("Xóa món");
        JButton btnClear = new JButton("Làm mới");
        btns.add(btnAdd);
        btns.add(btnUpdate);
        btns.add(btnDelete);
        btns.add(btnClear);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(btns, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        loadCategories();
        loadTable();

        // Chọn dòng -> đổ dữ liệu lên form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int r = table.getSelectedRow();
            if (r == -1) return;
            txtName.setText(String.valueOf(model.getValueAt(r, 1)));
            txtPrice.setText(String.format("%.0f", (double) model.getValueAt(r, 2)));
            String catName = String.valueOf(model.getValueAt(r, 3));
            for (int i = 0; i < cbCategory.getItemCount(); i++) {
                if (cbCategory.getItemAt(i).name.equals(catName)) cbCategory.setSelectedIndex(i);
            }
            cbStatus.setSelectedItem(String.valueOf(model.getValueAt(r, 4)));
        });

        btnAdd.addActionListener(e -> save(true));
        btnUpdate.addActionListener(e -> save(false));
        btnDelete.addActionListener(e -> delete());
        btnClear.addActionListener(e -> {
            clearForm();
            loadCategories();
            loadTable();
        });
        btnNewCat.addActionListener(e -> addCategory());
    }

    private void loadCategories() {
        cbCategory.removeAllItems();
        try (Connection conn = DB.get();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name FROM categories ORDER BY name")) {
            while (rs.next()) cbCategory.addItem(new Item(rs.getInt(1), rs.getString(2)));
        } catch (SQLException ex) {
            DB.error(this, ex);
        }
    }

    private void loadTable() {
        model.setRowCount(0);
        String sql = "SELECT p.id, p.name, p.price, c.name, p.status FROM products p "
                + "LEFT JOIN categories c ON p.category_id = c.id ORDER BY p.id";
        try (Connection conn = DB.get();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt(1));
                row.add(rs.getString(2));
                row.add(rs.getDouble(3));
                row.add(rs.getString(4));
                row.add(rs.getString(5));
                model.addRow(row);
            }
        } catch (SQLException ex) {
            DB.error(this, ex);
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtPrice.setText("");
        cbStatus.setSelectedIndex(0);
        table.clearSelection();
    }

    private void save(boolean isInsert) {
        String name = txtName.getText().trim();
        Item cat = (Item) cbCategory.getSelectedItem();
        if (name.isEmpty() || txtPrice.getText().trim().isEmpty() || cat == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        double price;
        try {
            price = DB.parseMoney(txtPrice.getText());
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá tiền phải là số lớn hơn 0!", "Lỗi định dạng",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = -1;
        if (!isInsert) {
            int r = table.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn món cần cập nhật trong bảng!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            id = (int) model.getValueAt(r, 0);
        }

        if (nameExists(name, id)) {
            JOptionPane.showMessageDialog(this, "Món \"" + name + "\" đã có trong thực đơn!\n"
                    + "Hãy chọn món đó trong bảng rồi bấm Cập nhật nếu muốn đổi giá.", "Trùng món",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = isInsert
                ? "INSERT INTO products (name, price, category_id, status) VALUES (?, ?, ?, ?)"
                : "UPDATE products SET name = ?, price = ?, category_id = ?, status = ? WHERE id = ?";
        try (Connection conn = DB.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, cat.id);
            ps.setString(4, (String) cbStatus.getSelectedItem());
            if (!isInsert) ps.setInt(5, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, isInsert ? "Thêm món thành công!" : "Cập nhật thành công!");
            clearForm();
            loadTable();
        } catch (SQLException ex) {
            DB.error(this, ex);
        }
    }

    // Kiểm tra tên món đã tồn tại chưa (bỏ qua chính món đang sửa)
    private boolean nameExists(String name, int excludeId) {
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM products WHERE name = ? AND id <> ?")) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            DB.error(this, ex);
            return true; // lỗi kết nối thì không cho thêm
        }
    }

    private void delete() {
        int r = table.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món cần xóa!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(r, 0);
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa món này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            clearForm();
            loadTable();
        } catch (SQLException ex) {
            // Món đã từng bán thì nằm trong hóa đơn -> không xóa được (ràng buộc khóa ngoại)
            JOptionPane.showMessageDialog(this,
                    "Không thể xóa món đã có trong hóa đơn.\nHãy đổi trạng thái thành \"Hết món\" để ẩn khỏi màn hình bán hàng.",
                    "Không thể xóa", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "Tên danh mục mới:");
        if (name == null || name.trim().isEmpty()) return;
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO categories (name) VALUES (?)")) {
            ps.setString(1, name.trim());
            ps.executeUpdate();
            loadCategories();
        } catch (SQLException ex) {
            DB.error(this, ex);
        }
    }
}

/* ===================================================================== */
/*                  TAB 3: LỊCH SỬ HÓA ĐƠN & DOANH THU                   */
/* ===================================================================== */
class HistoryPanel extends JPanel {
    private final DefaultTableModel orderModel = DB.model("Mã HĐ", "Thời gian", "Nhân viên", "Tổng tiền");
    private final JTable orderTable = new JTable(orderModel);
    private final DefaultTableModel detailModel = DB.model("Tên món", "SL", "Đơn giá", "Thành tiền");
    private final JTable detailTable = new JTable(detailModel);
    private final JComboBox<String> cbRange = new JComboBox<>(new String[]{"Hôm nay", "Tất cả"});
    private final JLabel lblSummary = new JLabel(" ");

    HistoryPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        top.add(new JLabel("Xem:"));
        top.add(cbRange);
        JButton btnRefresh = new JButton("Làm mới");
        top.add(btnRefresh);
        lblSummary.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSummary.setForeground(new Color(200, 30, 30));
        top.add(lblSummary);
        add(top, BorderLayout.NORTH);

        orderTable.setRowHeight(24);
        detailTable.setRowHeight(24);
        JScrollPane sp1 = new JScrollPane(orderTable);
        sp1.setBorder(BorderFactory.createTitledBorder("Danh sách hóa đơn"));
        JScrollPane sp2 = new JScrollPane(detailTable);
        sp2.setBorder(BorderFactory.createTitledBorder("Chi tiết hóa đơn đang chọn"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp1, sp2);
        split.setResizeWeight(0.6);
        add(split, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> reload());
        cbRange.addActionListener(e -> reload());
        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadDetails();
        });

        reload();
    }

    void reload() {
        orderModel.setRowCount(0);
        detailModel.setRowCount(0);
        String sql = "SELECT id, created_at, staff, total FROM orders "
                + (cbRange.getSelectedIndex() == 0 ? "WHERE CAST(created_at AS DATE) = CAST(GETDATE() AS DATE) " : "")
                + "ORDER BY id DESC";
        double sum = 0;
        int count = 0;
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try (Connection conn = DB.get();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt(1));
                row.add(fmt.format(rs.getTimestamp(2)));
                row.add(rs.getString(3));
                row.add(DB.money(rs.getDouble(4)));
                orderModel.addRow(row);
                sum += rs.getDouble(4);
                count++;
            }
        } catch (SQLException ex) {
            DB.error(this, ex);
        }
        lblSummary.setText("   Số hóa đơn: " + count + "   |   Doanh thu: " + DB.money(sum));
    }

    private void loadDetails() {
        detailModel.setRowCount(0);
        int r = orderTable.getSelectedRow();
        if (r == -1) return;
        int orderId = (int) orderModel.getValueAt(r, 0);
        String sql = "SELECT p.name, d.quantity, d.price, d.quantity * d.price "
                + "FROM order_details d JOIN products p ON d.product_id = p.id WHERE d.order_id = ?";
        try (Connection conn = DB.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString(1));
                    row.add(rs.getInt(2));
                    row.add(DB.money(rs.getDouble(3)));
                    row.add(DB.money(rs.getDouble(4)));
                    detailModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            DB.error(this, ex);
        }
    }
}
/** Icon giỏ hàng tự vẽ bằng code (không phụ thuộc font emoji) */
class CartIcon implements Icon {
    private final int size;
    private final Color color;

    CartIcon(int size, Color color) {
        this.size = size;
        this.color = color;
    }

    public int getIconWidth() { return size; }
    public int getIconHeight() { return size; }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.translate(x, y);
        float sc = size / 24f;
        g2.scale(sc, sc);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawPolyline(new int[]{2, 5, 8, 19, 21, 6}, new int[]{4, 4, 16, 16, 7, 7}, 6);
        g2.fillOval(9, 19, 3, 3);
        g2.fillOval(17, 19, 3, 3);
        g2.dispose();
    }
}