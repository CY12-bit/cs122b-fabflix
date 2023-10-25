package ShoppingCart;

import Login.Movie;
import Login.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;


@WebServlet(name = "Login.ShoppingCartServlet", urlPatterns = "/api/shopping-cart") // Why is it SingleStarServlet before?
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    // Function initiates servlet?
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function returns current contents of the cart
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // DO WE NEED TO SOLVE SYNCHRONIZATION?!? synchronized for adding to session?

        PrintWriter out = response.getWriter();

        JsonArray cart = new JsonArray();

        // MAKE A SQL QUERY TO EXTRACT THE PRICES FROM MOVIES IN THE CART
        try (Connection conn = dataSource.getConnection()) {
            for (Map.Entry<String, Movie> item : user.getShoppingCart().entrySet()) {
                JsonObject cart_item = new JsonObject();
                cart_item.addProperty("Id", item.getKey());
                cart_item.addProperty("Title", item.getValue().getTitle());
                cart_item.addProperty("Quantity", item.getValue().getMovieQuantity());

                String price_statement = "SELECT price FROM movies WHERE id = ?";

                PreparedStatement price_query = conn.prepareStatement(price_statement);
                price_query.setString(1,item.getKey());
                ResultSet price_set = price_query.executeQuery();
                Float price_value = null;
                while (price_set.next()) {
                    price_value = price_set.getFloat("price");
                }
                if (price_value == null) {
                    price_value = 10.0F;
                }
                cart_item.addProperty("Price",price_value);
                price_query.close();

                cart.add(cart_item);
            }

            conn.close();

            // Write JSON string to output
            out.write(cart.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }


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
        else if (value.equals("clearCart")) {
            user.clearCart();
        }

        JsonObject cart = new JsonObject();
        for (Map.Entry<String, Movie> item: user.getShoppingCart().entrySet()) {
            cart.addProperty(item.getKey(),item.getValue().getMovieQuantity());
        }

        response.getWriter().write(cart.toString());

    }

}
