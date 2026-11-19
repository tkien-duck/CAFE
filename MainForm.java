package do_an;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MainForm extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtId, txtName, txtPrice, txtCategoryId;
    private ProductDAO productDAO;

    public MainForm() {
        productDAO = new ProductDAO();
        
        setTitle("Phần Mềm Quản Lý Quán Cà Phê Và Trà Sữa");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Khởi tạo giao diện chuẩn đồ án
        initUI();
        
        // Load dữ liệu sản phẩm lên bảng ngay khi mở ứng dụng
        loadDataToTable();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // --- 1. THANH MENU PHÍA TRÊN ---
        JMenuBar menuBar = new JMenuBar();
        JMenu menuSystem = new JMenu("Hệ thống");
        JMenuItem itemExit = new JMenuItem("Thoát");
        itemExit.addActionListener(e -> System.exit(0));
        menuSystem.add(itemExit);
        
        JMenu menuManage = new JMenu("Quản lý");
        menuManage.add(new JMenuItem("Quản lý Sản Phẩm"));
        menuManage.add(new JMenuItem("Quản lý Danh Mục"));
        
        JMenu menuHelp = new JMenu("Trợ giúp");
        menuHelp.add(new JMenuItem("Giới thiệu đồ án"));

        menuBar.add(menuSystem);
        menuBar.add(menuManage);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);

        // --- 2. TIÊU ĐỀ PHẦN MỀM ---
        JLabel lblTitle = new JLabel("DANH SÁCH SẢN PHẨM QUÁN CÀ PHÊ", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(new Color(0, 102, 204));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblTitle, BorderLayout.NORTH);

        // --- 3. BẢNG HIỂN THỊ DỮ LIỆU Ở GIỮA ---
        tableModel = new DefaultTableModel(new String[]{"ID", "Tên sản phẩm", "Giá (VNĐ)", "Mã danh mục", "Trạng thái"}, 0);
        table = new JTable(tableModel);
        table.setRowHeight(25);
        
        // Sự kiện click chọn dòng trên bảng để đưa dữ liệu lên ô nhập liệu
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    txtId.setText(tableModel.getValueAt(selectedRow, 0).toString());
                    txtName.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    txtPrice.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    txtCategoryId.setText(tableModel.getValueAt(selectedRow, 3).toString());
                }
            }
        });
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- 4. KHU VỰC NHẬP LIỆU VÀ NÚT CHỨC NĂNG Ở DƯỚI ---
        JPanel panelBottom = new JPanel(new BorderLayout());
        panelBottom.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết sản phẩm"));

        // Panel chứa các ô nhập liệu
        JPanel panelInputs = new JPanel(new GridLayout(2, 4, 10, 10));
        panelInputs.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelInputs.add(new JLabel("ID (Tự tăng/Khóa):"));
        txtId = new JTextField();
        txtId.setEditable(false);
        panelInputs.add(txtId);

        panelInputs.add(new JLabel("Tên sản phẩm:"));
        txtName = new JTextField();
        panelInputs.add(txtName);

        panelInputs.add(new JLabel("Giá tiền (VNĐ):"));
        txtPrice = new JTextField();
        panelInputs.add(txtPrice);

        panelInputs.add(new JLabel("Mã danh mục:"));
        txtCategoryId = new JTextField();
        panelInputs.add(txtCategoryId);

        panelBottom.add(panelInputs, BorderLayout.CENTER);

        // Panel chứa các nút bấm (Thêm, Sửa, Xóa, Làm mới)
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        JButton btnAdd = new JButton("THÊM MỚI");
        JButton btnUpdate = new JButton("CẬP NHẬT");
        JButton btnDelete = new JButton("XÓA");
        JButton btnReset = new JButton("LÀM MỚI");

        // Định dạng màu sắc nút bấm cho bắt mắt
        btnAdd.setBackground(new Color(40, 167, 69)); btnAdd.setForeground(Color.WHITE);
        btnUpdate.setBackground(new Color(23, 162, 184)); btnUpdate.setForeground(Color.WHITE);
        btnDelete.setBackground(new Color(220, 53, 69)); btnDelete.setForeground(Color.WHITE);
        btnReset.setBackground(new Color(108, 117, 125)); btnReset.setForeground(Color.WHITE);

        panelButtons.add(btnAdd);
        panelButtons.add(btnUpdate);
        panelButtons.add(btnDelete);
        panelButtons.add(btnReset);

        panelBottom.add(panelButtons, BorderLayout.SOUTH);
        add(panelBottom, BorderLayout.SOUTH);

        // --- 5. LẬP TRÌNH SỰ KIỆN CHO CÁC NÚT ---

        // Nút Thêm
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = txtName.getText().trim();
                    double price = Double.parseDouble(txtPrice.getText().trim());
                    int categoryId = Integer.parseInt(txtCategoryId.getText().trim());

                    if (name.isEmpty()) {
                        JOptionPane.showMessageDialog(MainForm.this, "Tên sản phẩm không được để trống!");
                        return;
                    }

                    Product p = new Product(0, name, price, categoryId, "Còn món");
                    if (productDAO.addProduct(p)) {
                        JOptionPane.showMessageDialog(MainForm.this, "Thêm sản phẩm thành công!");
                        loadDataToTable();
                        clearForm();
                    } else {
                        JOptionPane.showMessageDialog(MainForm.this, "Thêm sản phẩm thất bại!");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(MainForm.this, "Vui lòng nhập đúng định dạng số cho Giá và Mã danh mục!");
                }
            }
        });

        // Nút Cập nhật (Sửa)
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (txtId.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(MainForm.this, "Vui lòng chọn sản phẩm cần sửa trên bảng!");
                        return;
                    }
                    int id = Integer.parseInt(txtId.getText().trim());
                    String name = txtName.getText().trim();
                    double price = Double.parseDouble(txtPrice.getText().trim());
                    int categoryId = Integer.parseInt(txtCategoryId.getText().trim());

                    Product p = new Product(id, name, price, categoryId, "Còn món");
                    if (productDAO.updateProduct(p)) { // Đảm bảo ProductDAO có hàm updateProduct tương ứng
                        JOptionPane.showMessageDialog(MainForm.this, "Cập nhật sản phẩm thành công!");
                        loadDataToTable();
                        clearForm();
                    } else {
                        JOptionPane.showMessageDialog(MainForm.this, "Cập nhật thất bại!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainForm.this, "Lỗi dữ liệu nhập vào!");
                }
            }
        });

        // Nút Xóa
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(MainForm.this, "Vui lòng chọn sản phẩm cần xóa trên bảng!");
                    return;
                }

                int id = (int) tableModel.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(MainForm.this, "Bạn có chắc chắn muốn xóa sản phẩm này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (productDAO.deleteProduct(id)) {
                        JOptionPane.showMessageDialog(MainForm.this, "Xóa sản phẩm thành công!");
                        loadDataToTable();
                        clearForm();
                    } else {
                        JOptionPane.showMessageDialog(MainForm.this, "Xóa thất bại!");
                    }
                }
            }
        });

        // Nút Làm mới
        btnReset.addActionListener(e -> {
            loadDataToTable();
            clearForm();
        });
    }

    // Đưa dữ liệu từ Database lên JTable
    public void loadDataToTable() {
        tableModel.setRowCount(0); 
        List<Product> list = productDAO.getAllProducts();
        for (Product p : list) {
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getCategoryId(),
                p.getStatus()
            });
        }
    }

    // Xóa trắng ô nhập liệu
    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtPrice.setText("");
        txtCategoryId.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainForm().setVisible(true));
    }
}