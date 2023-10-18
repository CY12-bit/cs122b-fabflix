package ShoppingCart;

import Login.User;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

// Declaring a WebServlet called WebPages.SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "Login.ShoppingCartServlet", urlPatterns = "/api/shopping-cart") // Why is it SingleStarServlet before?
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
//    private DataSource dataSource;
//
//    public void init(ServletConfig config) {
//        try {
//            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
//        } catch (NamingException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

         response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("movieId");
        String value = request.getParameter("value");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (value.equals("inc"))
            user.addMovie(id);
        else if (value.equals("dec"))
            user.removeMovie(id);

        JsonObject cart = new JsonObject();
        for (Map.Entry<String, Integer> item: user.getShoppingCart().entrySet()) {
            cart.addProperty(item.getKey(), item.getValue());
        }

        response.getWriter().write(cart.toString());

    }

}
