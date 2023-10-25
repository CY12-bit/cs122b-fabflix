package Login;

public class Movie {
    private final String mid;

    private Integer quantity;

    private final String title;

    public Movie(String mid, String title) {
        this.mid = mid;
        this.quantity = 1;
        this.title = title;
    }
    public Movie(String mid, String title, Integer quantity) {
        this.mid = mid;
        this.quantity = quantity;
        this.title = title;
    }

    public void changeQuantity(Integer change) {
        quantity += change;
    }

    public Integer getMovieQuantity() {
        return this.quantity;
    }

    public String getTitle() {
        return this.title;
    }
}
