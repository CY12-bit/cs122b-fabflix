import java.util.HashMap;
import java.util.Map;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {
    private final String uid;
    private Map<String, Integer> shoppingCart;

    public User(String uid) {
        this.uid = uid;
        this.shoppingCart = new HashMap<>();
    }

    public void addMovie(String movieId) {
        if (shoppingCart.containsKey(movieId)) {
            shoppingCart.put(movieId, shoppingCart.get(movieId) + 1);
        } else {
            shoppingCart.put(movieId, 1);
        }
    }

    public void removeMovie(String movieId) {
        if (shoppingCart.containsKey(movieId) && shoppingCart.get(movieId) > 1) {
            shoppingCart.put(movieId, shoppingCart.get(movieId) - 1);
        } else {
            shoppingCart.remove(movieId);
        }
    }

    public Map<String, Integer> getShoppingCart() {
        return shoppingCart;
    }

}
