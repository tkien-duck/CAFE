package do_an;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class CafeManagement extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtId, txtName, txtPrice, txtCategoryId;
    private ProductDAO productDAO;

    public CafeManagement() {
        productDAO = new ProductDAO();
        
        setTitle("Phần Mềm Quản Lý Quán Cà Phê Và Trà Sữa");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initUI();
        loadDataToTable();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. Menu phía trên
        JMenuBar menuBar = new JMenuBar();
        JMenu menuSystem = new JMenu("Hệ thống");
        JMenuItem itemExit = new JMenuItem("Thoát");
        itemExit.addActionListener(e -> System.exit(0));
        menuSystem.add(itemExit);
        menuBar.add(menuSystem);
        setJMenuBar(menuBar);

        // 2. Tiêu đề
        JLabel lblTitle = new JLabel("QUẢN LÝ SẢN PHẨM - QUÁN CÀ PHÊ & TRÀ SỮA", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(new Color(0, 102, 204));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblTitle, BorderLayout.NORTH);

        // 3. Bảng hiển thị dữ liệu (JTable)
        tableModel = new DefaultTableModel(new String[]{"ID", "Tên sản phẩm", "Giá (VNĐ)", "Mã danh mục", "Trạng thái"}, 0);
        table = new JTable(tableModel);
        table.setRowHeight(25);
        
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

        // 4. Khu vực nhập liệu và nút bấm phía dưới
        JPanel panelBottom = new JPanel(new BorderLayout());
        panelBottom.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết"));

        JPanel panelInputs = new JPanel(new GridLayout(2, 4, 10, 10));
        panelInputs.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelInputs.add(new JLabel("ID:"));
        txtId = new JTextField();
        txtId.setEditable(false);
        panelInputs.add(txtId);

        panelInputs.add(new JLabel("Tên sản phẩm:"));
        txtName = new JTextField();
        panelInputs.add(txtName);

        panelInputs.add(new JLabel("Giá tiền:"));
        txtPrice = new JTextField();
        panelInputs.add(txtPrice);

        panelInputs.add(new JLabel("Mã danh mục:"));
        txtCategoryId = new JTextField();
        panelInputs.add(txtCategoryId);

        panelBottom.add(panelInputs, BorderLayout.CENTER);

        // Các nút chức năng CRUD
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnAdd = new JButton("THÊM MỚI");
        JButton btnUpdate = new JButton("CẬP NHẬT");
        JButton btnDelete = new JButton("XÓA");
        JButton btnReset = new JButton("LÀM MỚI");

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

        // Sự kiện nút Thêm
        btnAdd.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                double price = Double.parseDouble(txtPrice.getText().trim());
                int categoryId = Integer.parseInt(txtCategoryId.getText().trim());

                Product p = new Product(0, name, price, categoryId, "Còn món");
                if (productDAO.addProduct(p)) {
                    JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!");
                    loadDataToTable();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm thất bại!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng kiểm tra lại định dạng dữ liệu nhập!");
            }
        });

        // Sự kiện nút Sửa
        btnUpdate.addActionListener(e -> {
            try {
                if (txtId.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm trên bảng để sửa!");
                    return;
                }
                int id = Integer.parseInt(txtId.getText().trim());
                String name = txtName.getText().trim();
                double price = Double.parseDouble(txtPrice.getText().trim());
                int categoryId = Integer.parseInt(txtCategoryId.getText().trim());

                Product p = new Product(id, name, price, categoryId, "Còn món");
                if (productDAO.updateProduct(p)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    loadDataToTable();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi dữ liệu đầu vào!");
            }
        });

        // Sự kiện nút Xóa
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần xóa trên bảng!");
                return;
            }
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (productDAO.deleteProduct(id)) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    loadDataToTable();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại!");
                }
            }
        });

        // Sự kiện nút Làm mới
        btnReset.addActionListener(e -> {
            loadDataToTable();
            clearForm();
        });
    }

    public void loadDataToTable() {
        tableModel.setRowCount(0);
        List<Product> list = productDAO.getAllProducts();
        for (Product p : list) {
            tableModel.addRow(new Object[]{
                p.getId(), p.getName(), p.getPrice(), p.getCategoryId(), p.getStatus()
            });
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtPrice.setText("");
        txtCategoryId.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CafeManagement().setVisible(true));
    }
}