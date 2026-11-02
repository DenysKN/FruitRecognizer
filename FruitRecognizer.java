package lab4;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class FruitRecognizer extends JFrame {

    // Еталонні об'єкти: колір, кісточка, насіння, вага (г), діаметр (см), клас.
    private static final String[] COLORS = {"жовтий", "оранжевий", "червоний", "зелений"};
    private static final double MAX_DISTANCE = 1.35;

    private static final List<Fruit> DATA = Arrays.asList(
            new Fruit("червоний", true,  false, 8,    2.5, "Вишня"),
            new Fruit("червоний", true,  false, 10,   2.8, "Вишня"),
            new Fruit("червоний", true,  false, 7,    2.3, "Вишня"),

            new Fruit("оранжевий", false, true, 180,  7.5, "Апельсин"),
            new Fruit("оранжевий", false, true, 200,  8.0, "Апельсин"),
            new Fruit("оранжевий", false, true, 160,  7.0, "Апельсин"),

            new Fruit("червоний", false, true, 170,  7.5, "Яблуко"),
            new Fruit("жовтий",   false, true, 160,  7.0, "Яблуко"),
            new Fruit("зелений",  false, true, 180,  7.8, "Яблуко"),

            new Fruit("жовтий",   false, true, 1200, 20.0, "Диня"),
            new Fruit("жовтий",   false, true, 1500, 22.0, "Диня"),
            new Fruit("зелений",  false, true, 1300, 21.0, "Диня")
    );

    private final JComboBox<String> colorBox = new JComboBox<>(COLORS);
    private final JComboBox<String> stoneBox = new JComboBox<>(new String[]{"так", "ні"});
    private final JComboBox<String> seedsBox = new JComboBox<>(new String[]{"так", "ні"});
    private final JTextField weightField = new JTextField("9", 12);
    private final JTextField diameterField = new JTextField("2.6", 12);
    private final JLabel resultLabel = new JLabel(
            "Введіть характеристики та натисніть «Розпізнати об'єкт»."
    );

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Еталонний фрукт", "Евклідова відстань"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public FruitRecognizer() {
        super("Розпізнавання образів — фрукти");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 620);
        setMinimumSize(new Dimension(680, 560));
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        setContentPane(main);

        JLabel title = new JLabel(
                "<html><div style='text-align:center'>Система розпізнавання фруктів"
                        + "<br><span style='font-size:12px;font-weight:normal'>"
                        + "Метод найближчого сусіда • Евклідова відстань</span></div></html>",
                SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 21));
        main.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        main.add(center, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Характеристики досліджуваного об'єкта"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(form, gbc, 0, "Колір:", colorBox);
        addFormRow(form, gbc, 1, "Наявність кісточки:", stoneBox);
        addFormRow(form, gbc, 2, "Наявність насіння:", seedsBox);
        addFormRow(form, gbc, 3, "Вага (г):", weightField);
        addFormRow(form, gbc, 4, "Діаметр (см):", diameterField);

        JButton recognizeButton = new JButton("Розпізнати об'єкт");
        recognizeButton.setFont(new Font("Arial", Font.BOLD, 14));
        recognizeButton.addActionListener(e -> recognizeObject());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(recognizeButton);

        JPanel resultPanel = new JPanel(new BorderLayout(5, 5));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Результат розпізнавання"));
        resultLabel.setFont(new Font("Arial", Font.BOLD, 14));
        resultLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        resultPanel.add(resultLabel, BorderLayout.CENTER);

        JTable table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Відстані до еталонних об'єктів"));

        center.add(form);
        center.add(buttonPanel);
        center.add(resultPanel);
        center.add(scrollPane);

        JLabel note = new JLabel(
                "Поріг відмови: " + MAX_DISTANCE
                        + ". Якщо найближчий еталон далі за поріг, об'єкт вважається невідомим."
        );
        note.setFont(new Font("Arial", Font.PLAIN, 11));
        main.add(note, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc,
                            int row, String label, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    // Перетворення характеристик у числовий вектор.
    // Колір кодується чотирма двійковими ознаками, інші якісні ознаки — 0 або 1.
    private static double[] encode(String color, boolean stone, boolean seeds,
                                   double weight, double diameter) {
        double[] vector = new double[8];

        for (int i = 0; i < COLORS.length; i++) {
            vector[i] = COLORS[i].equals(color) ? 1.0 : 0.0;
        }

        vector[4] = stone ? 1.0 : 0.0;
        vector[5] = seeds ? 1.0 : 0.0;

        // Нормалізація кількісних характеристик у межах навчальної вибірки.
        vector[6] = (weight - 7.0) / (1500.0 - 7.0);
        vector[7] = (diameter - 2.3) / (22.0 - 2.3);

        return vector;
    }

    private static double euclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double difference = a[i] - b[i];
            sum += difference * difference;
        }
        return Math.sqrt(sum);
    }

    private void recognizeObject() {
        double weight;
        double diameter;

        try {
            weight = Double.parseDouble(weightField.getText().trim().replace(',', '.'));
            diameter = Double.parseDouble(diameterField.getText().trim().replace(',', '.'));

            if (weight <= 0 || diameter <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Вага та діаметр мають бути додатними числами.",
                    "Помилка введення",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (weight < 7 || weight > 1500 || diameter < 2.3 || diameter > 22) {
            JOptionPane.showMessageDialog(
                    this,
                    "Для цієї навчальної вибірки введіть вагу від 7 до 1500 г "
                            + "та діаметр від 2,3 до 22 см.",
                    "Перевірка даних",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String color = (String) colorBox.getSelectedItem();
        boolean stone = "так".equals(stoneBox.getSelectedItem());
        boolean seeds = "так".equals(seedsBox.getSelectedItem());

        double[] query = encode(color, stone, seeds, weight, diameter);

        // Знаходимо мінімальну відстань до кожного класу.
        Map<String, Double> bestByClass = new HashMap<>();
        for (Fruit fruit : DATA) {
            double[] reference = encode(
                    fruit.color, fruit.stone, fruit.seeds, fruit.weight, fruit.diameter
            );
            double distance = euclideanDistance(query, reference);

            if (!bestByClass.containsKey(fruit.name)
                    || distance < bestByClass.get(fruit.name)) {
                bestByClass.put(fruit.name, distance);
            }
        }

        List<Map.Entry<String, Double>> sorted = new ArrayList<>(bestByClass.entrySet());
        sorted.sort(Map.Entry.comparingByValue());

        tableModel.setRowCount(0);
        for (Map.Entry<String, Double> entry : sorted) {
            tableModel.addRow(new Object[]{
                    entry.getKey(),
                    String.format(Locale.US, "%.4f", entry.getValue())
            });
        }

        String bestFruit = sorted.get(0).getKey();
        double bestDistance = sorted.get(0).getValue();

        if (bestDistance <= MAX_DISTANCE) {
            resultLabel.setText(
                    "Розпізнаний об'єкт: " + bestFruit
                            + ". Відстань до найближчого еталона: "
                            + String.format(Locale.US, "%.4f", bestDistance)
            );
        } else {
            resultLabel.setText(
                    "Об'єкт не вдалося надійно розпізнати. Найближчий клас — "
                            + bestFruit + ", але відстань "
                            + String.format(Locale.US, "%.4f", bestDistance)
                            + " перевищує поріг " + MAX_DISTANCE + "."
            );
        }
    }

    private static class Fruit {
        String color;
        boolean stone;
        boolean seeds;
        double weight;
        double diameter;
        String name;

        Fruit(String color, boolean stone, boolean seeds,
              double weight, double diameter, String name) {
            this.color = color;
            this.stone = stone;
            this.seeds = seeds;
            this.weight = weight;
            this.diameter = diameter;
            this.name = name;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FruitRecognizer().setVisible(true));
    }
}
