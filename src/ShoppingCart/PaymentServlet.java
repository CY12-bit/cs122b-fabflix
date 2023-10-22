package ShoppingCart;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name="ShoppingCart.PaymentServlet",urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
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

    // Function checks credit card information in table
    // If the credit card is invalid, then it will send a status of failure
    // If the credit card is valid, then it will send a success message
    // along with inserting the orders into the sales table
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        final String creditCard = request.getParameter("creditCardCode");
        final String firstName = request.getParameter("firstName");
        final String lastName = request.getParameter("lastName");
        final String expirationDate = request.getParameter("expirationDate");

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
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
                // INSERT DATA INTO SALES TABLE
            }
            else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "no credit card found");
                request.getServletContext().log("Payment failed");
            }

            credit_data.close();
            credit_statement.close();
            conn.close();
            out.write(responseJsonObject.toString());
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
        }
        finally {
            out.close();
        }

    }

}
