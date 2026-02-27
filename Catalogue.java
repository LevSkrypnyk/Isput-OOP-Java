import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Catalogue implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Publication> publications = new ArrayList<>();

    public void addPublication(Publication p) {
        if (p == null) return;
        publications.add(p);
    }

    public void removePublicationByTitle(String title) throws BookNotFoundException {
        Publication found = findPublicationByTitle(title);
        if (found == null) {
            throw new BookNotFoundException("Публікацію з назвою \"" + title + "\" не знайдено.");
        }
        publications.remove(found);
    }

    /**
     * Пошук за ТОЧНОЮ назвою (ігнорує регістр і пробіли по краях).
     */
    public Publication findPublicationByTitle(String title) {
        if (title == null) return null;
        String t = title.trim().toLowerCase();
        for (Publication p : publications) {
            if (p != null && p.getTitle() != null && p.getTitle().trim().toLowerCase().equals(t)) {
                return p;
            }
        }
        return null;
    }

    public List<Publication> getAllPublications() {
        return new ArrayList<>(publications);
    }

    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(publications);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object obj = ois.readObject();
            if (obj instanceof ArrayList) {
                this.publications = (ArrayList<Publication>) obj;
            } else {
                throw new IOException("Невірний формат файлу каталогу.");
            }
        }
    }
}
