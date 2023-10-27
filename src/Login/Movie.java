package Login;

public class Movie {
    private final String mid;
    private int quantity;
    private final String title;
    private float price;

    public Movie(String mid, String title) {
        this.mid = mid;
        this.quantity = 1;
        this.title = title;
        this.price = 0;
    }
    public Movie(String mid, String title, int quantity) {
        this.mid = mid;
        this.quantity = quantity;
        this.title = title;
        this.price = 0;
    }

    public void changeQuantity(Integer change) {
        quantity += change;
    }

    public int getMovieQuantity() {
        return this.quantity;
    }

    public String getTitle() {
        return this.title;
    }

    public float getPrice(){ return this.price; }

    public void setPrice(float price) { this.price = price; }
}
