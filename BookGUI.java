import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BookGUI extends JFrame {
    private final Catalogue catalogue = new Catalogue();

    private final JTextField titleField = new JTextField();
    private final JTextField authorField = new JTextField();
    private final JTextField publisherField = new JTextField();
    private final JTextField genreField = new JTextField();
    private final JTextField yearField = new JTextField();

    private final JTextField searchField = new JTextField();

    private final DefaultListModel<Publication> listModel = new DefaultListModel<>();
    private final JList<Publication> publicationJList = new JList<>(listModel);

    private String currentFilter = ""; // для пошуку (часткового)

    public BookGUI() {
        super("Каталог книг (Swing)");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        add(buildLeftFormPanel(), BorderLayout.WEST);
        add(buildCenterListPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        publicationJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        publicationJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Publication p = publicationJList.getSelectedValue();
                if (p instanceof Book b) {
                    fillFormFromBook(b);
                } else if (p != null) {
                    titleField.setText(p.getTitle());
                    yearField.setText(String.valueOf(p.getYear()));
                    authorField.setText("");
                    publisherField.setText("");
                    genreField.setText("");
                }
            }
        });

        refreshList();
    }

    private JPanel buildLeftFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setPreferredSize(new Dimension(340, 0));

        JLabel header = new JLabel("Дані книги");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(labeled("Назва", titleField));
        form.add(labeled("Автор", authorField));
        form.add(labeled("Видавництво", publisherField));
        form.add(labeled("Жанр", genreField));
        form.add(labeled("Рік", yearField));

        panel.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 6, 6));

        JButton addBtn = new JButton("Додати книгу");
        addBtn.addActionListener(e -> onAdd());
        buttons.add(addBtn);

        JButton deleteBtn = new JButton("Видалити книгу");
        deleteBtn.addActionListener(e -> onDelete());
        buttons.add(deleteBtn);

        JButton updateBtn = new JButton("Оновити книгу");
        updateBtn.addActionListener(e -> onUpdate());
        buttons.add(updateBtn);

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCenterListPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JLabel header = new JLabel("Список публікацій");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(header, BorderLayout.NORTH);

        publicationJList.setVisibleRowCount(12);
        publicationJList.setFixedCellHeight(28);

        JScrollPane scroll = new JScrollPane(publicationJList);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JPanel searchPanel = new JPanel(new BorderLayout(6, 6));
        searchPanel.add(new JLabel("Пошук за назвою:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton searchBtn = new JButton("Пошук");
        searchBtn.addActionListener(e -> onSearch());
        searchButtons.add(searchBtn);

        JButton resetBtn = new JButton("Скинути параметри пошуку");
        resetBtn.addActionListener(e -> onResetSearch());
        searchButtons.add(resetBtn);

        searchPanel.add(searchButtons, BorderLayout.EAST);

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton saveBtn = new JButton("Зберегти у файл");
        saveBtn.addActionListener(e -> onSave());
        filePanel.add(saveBtn);

        JButton loadBtn = new JButton("Завантажити з файлу");
        loadBtn.addActionListener(e -> onLoad());
        filePanel.add(loadBtn);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(filePanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel labeled(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        JLabel l = new JLabel(label + ": ");
        l.setPreferredSize(new Dimension(110, 24));
        p.add(l, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void onAdd() {
        try {
            Book b = readBookFromForm();
            catalogue.addPublication(b);
            refreshList();
            selectByExactTitle(b.getTitle());
            showInfo("Книгу додано.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void onDelete() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showError("Вкажіть назву для видалення.");
            return;
        }
        try {
            catalogue.removePublicationByTitle(title);
            refreshList();
            showInfo("Книгу видалено.");
        } catch (BookNotFoundException ex) {
            showError(ex.getMessage());
        }
    }

    private void onUpdate() {
        try {
            Book newData = readBookFromForm();
            Publication existing = catalogue.findPublicationByTitle(newData.getTitle());

            if (existing == null) {
                // як "оновлення" — якщо не знайдено, можна або кинути, або додати. Тут зробимо помилку:
                throw new IllegalArgumentException("Немає книги з такою назвою для оновлення: \"" + newData.getTitle() + "\"");
            }

            if (existing instanceof Book b) {
                b.setYear(newData.getYear());
                b.setAuthor(newData.getAuthor());
                b.setPublisher(newData.getPublisher());
                b.setGenre(newData.getGenre());
            } else {
                // Якщо раптом була не Book, замінимо на Book:
                try {
                    catalogue.removePublicationByTitle(existing.getTitle());
                } catch (BookNotFoundException ignored) {}
                catalogue.addPublication(newData);
            }

            refreshList();
            selectByExactTitle(newData.getTitle());
            showInfo("Книгу оновлено.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void onSave() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Зберегти каталог у файл");
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try {
            String filename = chooser.getSelectedFile().getAbsolutePath();
            catalogue.saveToFile(filename);
            showInfo("Каталог збережено у файл:\n" + filename);
        } catch (IOException ex) {
            showError("Помилка збереження: " + ex.getMessage());
        }
    }

    private void onLoad() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Завантажити каталог з файлу");
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try {
            String filename = chooser.getSelectedFile().getAbsolutePath();
            catalogue.loadFromFile(filename);
            refreshList();
            showInfo("Каталог завантажено з файлу:\n" + filename);
        } catch (IOException | ClassNotFoundException ex) {
            showError("Помилка завантаження: " + ex.getMessage());
        }
    }

    private void onSearch() {
        currentFilter = searchField.getText().trim();
        refreshList();
        if (!currentFilter.isEmpty() && listModel.isEmpty()) {
            showInfo("За запитом нічого не знайдено.");
        }
    }

    private void onResetSearch() {
        currentFilter = "";
        searchField.setText("");
        refreshList();
    }

    private Book readBookFromForm() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String publisher = publisherField.getText().trim();
        String genre = genreField.getText().trim();
        String yearStr = yearField.getText().trim();

        if (title.isEmpty()) throw new IllegalArgumentException("Назва не може бути порожньою.");
        if (yearStr.isEmpty()) throw new IllegalArgumentException("Рік не може бути порожнім.");

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Рік має бути цілим числом.");
        }

        return new Book(title, year, author, publisher, genre);
    }

    private void fillFormFromBook(Book b) {
        titleField.setText(b.getTitle());
        yearField.setText(String.valueOf(b.getYear()));
        authorField.setText(b.getAuthor() == null ? "" : b.getAuthor());
        publisherField.setText(b.getPublisher() == null ? "" : b.getPublisher());
        genreField.setText(b.getGenre() == null ? "" : b.getGenre());
    }

    private void refreshList() {
        listModel.clear();
        List<Publication> all = catalogue.getAllPublications();

        List<Publication> filtered;
        if (currentFilter == null || currentFilter.isBlank()) {
            filtered = all;
        } else {
            String f = currentFilter.toLowerCase();
            filtered = all.stream()
                    .filter(p -> p != null && p.getTitle() != null && p.getTitle().toLowerCase().contains(f))
                    .collect(Collectors.toList());
        }

        for (Publication p : filtered) listModel.addElement(p);
    }

    private void selectByExactTitle(String title) {
        if (title == null) return;
        String t = title.trim().toLowerCase();
        for (int i = 0; i < listModel.size(); i++) {
            Publication p = listModel.get(i);
            if (p != null && p.getTitle() != null && p.getTitle().trim().toLowerCase().equals(t)) {
                publicationJList.setSelectedIndex(i);
                publicationJList.ensureIndexIsVisible(i);
                return;
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Помилка", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Інформація", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookGUI().setVisible(true));
    }
}