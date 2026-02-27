public class Book extends Publication {
    private static final long serialVersionUID = 1L;

    private String author;
    private String publisher;
    private String genre;

    public Book() {}

    public Book(String title, int year, String author, String publisher, String genre) {
        super(title, year);
        this.author = author;
        this.publisher = publisher;
        this.genre = genre;
    }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    @Override
    public String toString() {
        return String.format(
                "Книга: \"%s\" (%d) | Автор: %s | Видавництво: %s | Жанр: %s",
                getTitle(), getYear(),
                author == null ? "" : author,
                publisher == null ? "" : publisher,
                genre == null ? "" : genre
        );
    }
}