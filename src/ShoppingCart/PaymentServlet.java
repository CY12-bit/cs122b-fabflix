package ShoppingCart;

import Login.Movie;
import Login.User;
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
import java.sql.*;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@WebServlet(name="ShoppingCart.PaymentServlet",urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    // Function initiates servlet?
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_master");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Function checks credit card information in table
    // If the credit card is invalid, then it will send a status of failure
    // If the credit card is valid, then it will send a success message
    // along with inserting the orders into the sales table
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        final String creditCard = request.getParameter("card_num");
        final String firstName = request.getParameter("firstName");
        final String lastName = request.getParameter("lastName");
        final String expirationDate = request.getParameter("expirationDate");

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            // 1. Need to check if the credit card information is correct
            // [IN THE FUTURE] possibly cross-reference the customers table so that we know the user logged in is using the right credit card?

            String creditCardQuery = "SELECT * FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?";
            PreparedStatement credit_statement = conn.prepareStatement(creditCardQuery);

            JsonObject responseJsonObject = new JsonObject();

            credit_statement.setString(1,creditCard);
            credit_statement.setString(2,firstName);
            credit_statement.setString(3,lastName);
            credit_statement.setString(4,expirationDate);
            ResultSet credit_data = credit_statement.executeQuery();
            String credit_id = null;

            while(credit_data.next()) {
                credit_id = credit_data.getString("id");
            }

            if (credit_id != null) {
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

                HttpSession session = request.getSession();
                User user = (User) session.getAttribute("user");
                session.setAttribute("paid", true);

                JsonObject confirmation_cart = new JsonObject();

                for (Map.Entry<String, Movie> item: user.getShoppingCart().entrySet()) {
                    LocalDate current_date = LocalDate.now();
                    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String formattedDate = current_date.format(myFormatObj);
                    String insert_query = "INSERT INTO sales(customerId,movieId,quantity,saleDate) VALUES (?, ?, ?, ?)";
                    PreparedStatement insert_statement = conn.prepareStatement(insert_query);
                    insert_statement.setString(1, user.getUserId());
                    insert_statement.setString(2, item.getKey());
                    insert_statement.setInt(3,  item.getValue().getMovieQuantity());
                    insert_statement.setString(4,formattedDate);
                    insert_statement.executeUpdate();
                    confirmation_cart.addProperty(item.getKey(), item.getValue().getMovieQuantity());
                    insert_statement.close();
                }

                responseJsonObject.add("confirmation_cart",confirmation_cart);
            }
            else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "no credit card found");
                request.getServletContext().log("Payment failed");
            }

            credit_data.close();
            credit_statement.close();
            // conn.close();

            out.write(responseJsonObject.toString());
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            System.out.println("payment fail: " + e.getMessage());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        finally {
            out.close();
        }

    }

}
