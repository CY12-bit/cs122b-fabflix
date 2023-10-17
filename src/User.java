import java.util.HashMap;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {
    private final String uid;
    private HashMap<String, Integer> shoppingCart;

    public User(String uid) {
        this.uid = uid;
    }

    public void addItem(String itemId) {
        if (shoppingCart.containsKey(itemId)) {
            shoppingCart.put(itemId, shoppingCart.get(itemId) + 1);
        } else {
            shoppingCart.put(itemId, 1);
        }
    }

    public void removeItem(String itemId) {
        if (shoppingCart.containsKey(itemId) && shoppingCart.get(itemId) > 1) {
            shoppingCart.put(itemId, shoppingCart.get(itemId) - 1);
        } else {
            shoppingCart.remove(itemId);
        }
    }

}
