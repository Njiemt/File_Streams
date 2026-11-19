import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RandProductSearch extends JFrame {
    private static final Path DATA_FILE = Path.of("products.dat");

    private final JTextField searchField = new JTextField(24);
    private final JTextArea resultsArea = new JTextArea(16, 50);

    private RandProductSearch() {
        super("Random Access Product Search");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Partial product name:"));
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(event -> searchProducts());
        searchPanel.add(searchButton);
        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(event -> dispose());
        searchPanel.add(quitButton);

        resultsArea.setEditable(false);
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultsArea), BorderLayout.CENTER);
        add(new JLabel("Data file: " + DATA_FILE.toAbsolutePath()), BorderLayout.SOUTH);
        pack();
        setLocationByPlatform(true);
    }

    private void searchProducts() {
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter all or part of a product name.",
                    "Search Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (!Files.exists(DATA_FILE)) {
                throw new IOException("The product file does not exist: " + DATA_FILE.toAbsolutePath());
            }
            List<Product> matches = new ArrayList<>();
            try (RandomAccessFile file = new RandomAccessFile(DATA_FILE.toFile(), "r")) {
                if (file.length() % Product.RECORD_LENGTH != 0) {
                    throw new IOException("The file contains an incomplete product record.");
                }
                while (file.getFilePointer() < file.length()) {
                    Product product = Product.readFrom(file);
                    if (product.getName().toLowerCase(Locale.ROOT).contains(query)) {
                        matches.add(product);
                    }
                }
            }

            if (matches.isEmpty()) {
                resultsArea.setText("No products matched \"" + searchField.getText().trim() + "\".");
            } else {
                StringBuilder results = new StringBuilder();
                for (Product product : matches) {
                    results.append(product).append("\n\n");
                }
                resultsArea.setText(results.toString().trim());
                resultsArea.setCaretPosition(0);
            }
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Product File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RandProductSearch().setVisible(true));
    }
}
