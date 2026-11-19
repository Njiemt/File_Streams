import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

public final class RandProductMaker extends JFrame {
    private static final Path DATA_FILE = Path.of("products.dat");

    private final JTextField nameField = new JTextField(30);
    private final JTextField descriptionField = new JTextField(30);
    private final JTextField idField = new JTextField(10);
    private final JTextField costField = new JTextField(12);
    private final JTextField recordCountField = new JTextField(8);

    private RandProductMaker() {
        super("Random Access Product Maker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        recordCountField.setEditable(false);
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, constraints, 0, "Name (max 35 characters)", nameField);
        addRow(form, constraints, 1, "Description (max 75 characters)", descriptionField);
        addRow(form, constraints, 2, "ID (max 6 characters)", idField);
        addRow(form, constraints, 3, "Cost", costField);
        addRow(form, constraints, 4, "Record count", recordCountField);

        JButton addButton = new JButton("Add Product");
        addButton.addActionListener(event -> addProduct());
        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(event -> dispose());
        JPanel buttons = new JPanel();
        buttons.add(addButton);
        buttons.add(quitButton);

        JLabel fileLabel = new JLabel("Data file: " + DATA_FILE.toAbsolutePath());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        add(fileLabel, BorderLayout.NORTH);
        pack();
        setLocationByPlatform(true);

        try {
            recordCountField.setText(Integer.toString(getRecordCount()));
        } catch (IOException exception) {
            showError("Could not read the product file: " + exception.getMessage());
        }
    }

    private static void addRow(JPanel panel, GridBagConstraints constraints, int row, String label, JComponent field) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        panel.add(new JLabel(label), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(field, constraints);
    }

    private int getRecordCount() throws IOException {
        if (!java.nio.file.Files.exists(DATA_FILE)) {
            return 0;
        }
        try (RandomAccessFile file = new RandomAccessFile(DATA_FILE.toFile(), "r")) {
            long length = file.length();
            if (length % Product.RECORD_LENGTH != 0) {
                throw new IOException("The file contains an incomplete product record.");
            }
            return Math.toIntExact(length / Product.RECORD_LENGTH);
        } catch (ArithmeticException exception) {
            throw new IOException("The product file contains too many records.", exception);
        }
    }

    private void addProduct() {
        try {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            String id = idField.getText().trim();
            String costText = costField.getText().trim();

            Product.validateTextField("Name", name, Product.NAME_LENGTH);
            Product.validateTextField("Description", description, Product.DESCRIPTION_LENGTH);
            Product.validateTextField("ID", id, Product.ID_LENGTH);
            if (costText.isEmpty()) {
                throw new IllegalArgumentException("Cost is required.");
            }
            double cost;
            try {
                cost = Double.parseDouble(costText);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Cost must be a valid number.");
            }
            if (!Double.isFinite(cost) || cost < 0) {
                throw new IllegalArgumentException("Cost must be a finite, non-negative number.");
            }

            Product product = new Product(name, description, id, cost);
            try (RandomAccessFile file = new RandomAccessFile(DATA_FILE.toFile(), "rw")) {
                if (file.length() % Product.RECORD_LENGTH != 0) {
                    throw new IOException("The file contains an incomplete product record.");
                }
                file.seek(file.length());
                product.writeTo(file);
            }
            recordCountField.setText(Integer.toString(getRecordCount()));
            nameField.setText("");
            descriptionField.setText("");
            idField.setText("");
            costField.setText("");
            nameField.requestFocusInWindow();
        } catch (IllegalArgumentException | IOException exception) {
            showError(exception.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Product Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RandProductMaker().setVisible(true));
    }
}
