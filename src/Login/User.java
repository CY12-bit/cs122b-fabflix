package Login;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonObject;
import Login.Movie;

/**
 * This d.User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {
    private final String uid;
    private Map<String,Movie> shoppingCart;

    public User(String uid) {
        this.uid = uid;
        this.shoppingCart = new HashMap<>();
    }

    public void addMovie(String movieId, String movieTitle) {
        if (shoppingCart.containsKey(movieId)) {
            shoppingCart.get(movieId).changeQuantity(1);
        } else {
            shoppingCart.put(movieId, new Movie(movieId,movieTitle));
        }
    }

    public void removeMovie(String movieId) {
        if (shoppingCart.containsKey(movieId) && shoppingCart.get(movieId).getMovieQuantity() > 1) {
            shoppingCart.get(movieId).changeQuantity(-1);
        } else {
            clearMovie(movieId);
        }
    }

    public void clearMovie(String movieId) { shoppingCart.remove(movieId); }

    public void clearCart() { shoppingCart.clear(); }

    public Map<String, Movie> getShoppingCart() {
        return shoppingCart;
    }

    public String getUserId() {
        return this.uid;
    }
}
