package Login;

import java.util.HashMap;
import java.util.Map;

/**
 * This d.User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {
    private final String uid;
    private Map<String, Integer> shoppingCart_quantities;
    private Map<String, String> shoppingCart_titles;

    public User(String uid) {
        this.uid = uid;
        this.shoppingCart_quantities = new HashMap<>();
        this.shoppingCart_titles = new HashMap<>();
    }

    public void addMovie(String movieId, String movieTitle) {
        if (shoppingCart_quantities.containsKey(movieId)) {
            shoppingCart_quantities.put(movieId, shoppingCart_quantities.get(movieId) + 1);
        } else {
            shoppingCart_quantities.put(movieId, 1);
            shoppingCart_titles.put(movieId,movieTitle);
        }
    }

    public void removeMovie(String movieId) {
        if (shoppingCart_quantities.containsKey(movieId) && shoppingCart_quantities.get(movieId) > 1) {
            shoppingCart_quantities.put(movieId, shoppingCart_quantities.get(movieId) - 1);
        } else {
            shoppingCart_quantities.remove(movieId);
            shoppingCart_titles.remove(movieId);
        }
    }

    public void clearMovie(String movieId) {
        if (shoppingCart_quantities.containsKey(movieId)) {
            shoppingCart_quantities.remove(movieId);
            shoppingCart_titles.remove(movieId);
        }
    }

    public Map<String, Integer> getShoppingCart() {
        return shoppingCart_quantities;
    }

    public Map<String, String> getCartTitles() { return shoppingCart_titles; }
}
