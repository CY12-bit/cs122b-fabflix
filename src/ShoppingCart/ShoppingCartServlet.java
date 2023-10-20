package ShoppingCart;

import Login.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;


@WebServlet(name = "Login.ShoppingCartServlet", urlPatterns = "/api/shopping-cart") // Why is it SingleStarServlet before?
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    /**
     * Function returns current contents of the cart
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        PrintWriter out = response.getWriter();

        JsonArray cart = new JsonArray();

        Map<String, Integer> cart_quantities = user.getShoppingCart();
        Map<String, String> cart_titles = user.getCartTitles();

        for (Map.Entry<String, Integer> item: cart_quantities.entrySet()) {
            JsonObject cart_item = new JsonObject();
            cart_item.addProperty("Id",item.getKey());
            cart_item.addProperty("Title",cart_titles.get(item.getKey()));
            cart_item.addProperty("Quantity", item.getValue());

            cart.add(cart_item);
        }

        // Write JSON string to output
        out.write(cart.toString());
        // Set response status to 200 (OK)
        response.setStatus(200);
    }

    /**
     * Function increases or decreases movie amount from cart
     * depending on value
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

         response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("movieId");
        String title = request.getParameter("movieTitle");
        String value = request.getParameter("value");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (value.equals("inc"))
            user.addMovie(id,title);
        else if (value.equals("dec"))
            user.removeMovie(id);
        else if (value.equals("remove"))
            user.clearMovie(id);

        JsonObject cart = new JsonObject();
        for (Map.Entry<String, Integer> item: user.getShoppingCart().entrySet()) {
            cart.addProperty(item.getKey(), item.getValue());
        }

        response.getWriter().write(cart.toString());

    }

}
