import java.io.IOException;
import java.io.RandomAccessFile;

public final class Product {
    public static final int NAME_LENGTH = 35;
    public static final int DESCRIPTION_LENGTH = 75;
    public static final int ID_LENGTH = 6;
    public static final int RECORD_LENGTH =
            (NAME_LENGTH + DESCRIPTION_LENGTH + ID_LENGTH) * Character.BYTES + Double.BYTES;

    private final String name;
    private final String description;
    private final String id;
    private final double cost;

    public Product(String name, String description, String id, double cost) {
        validateTextField("Name", name, NAME_LENGTH);
        validateTextField("Description", description, DESCRIPTION_LENGTH);
        validateTextField("ID", id, ID_LENGTH);
        if (!Double.isFinite(cost) || cost < 0) {
            throw new IllegalArgumentException("Cost must be a finite, non-negative number.");
        }
        this.name = name;
        this.description = description;
        this.id = id;
        this.cost = cost;
    }

    public static void validateTextField(String label, String value, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(label + " must be at most " + maxLength + " characters.");
        }
    }

    public void writeTo(RandomAccessFile file) throws IOException {
        writeFixedString(file, name, NAME_LENGTH);
        writeFixedString(file, description, DESCRIPTION_LENGTH);
        writeFixedString(file, id, ID_LENGTH);
        file.writeDouble(cost);
    }

    public static Product readFrom(RandomAccessFile file) throws IOException {
        String name = readFixedString(file, NAME_LENGTH);
        String description = readFixedString(file, DESCRIPTION_LENGTH);
        String id = readFixedString(file, ID_LENGTH);
        return new Product(name, description, id, file.readDouble());
    }

    private static void writeFixedString(RandomAccessFile file, String value, int length) throws IOException {
        String padded = value + " ".repeat(length - value.length());
        for (int i = 0; i < length; i++) {
            file.writeChar(padded.charAt(i));
        }
    }

    private static String readFixedString(RandomAccessFile file, int length) throws IOException {
        StringBuilder value = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            value.append(file.readChar());
        }
        return value.toString().trim();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %s) - $%.2f%n%s", name, id, cost, description);
    }
}
