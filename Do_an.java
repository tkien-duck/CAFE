package do_an;

import javax.swing.SwingUtilities;

public class Do_an {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CafeManagement frame = new CafeManagement();
                frame.setVisible(true);
            }
        });
    }
}