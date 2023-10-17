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

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            String user_query = "SELECT c.id, c.password FROM customers c " +
                                "WHERE c.email = ?";
            PreparedStatement user_statement = conn.prepareStatement(user_query);
            user_statement.setString(1, username);

            JsonObject responseJsonObject = new JsonObject();

            ResultSet user_data = user_statement.executeQuery();
            User currUser = null;
            String actualPw = "";
            while (user_data.next()) {
                String userId = user_data.getString("id");
                // String firstName = user_data.getString("firstName");
                // String lastName = user_data.getString("lastName");
                actualPw = user_data.getString("password");

                currUser = new User(userId);
            }

            // username doesn't exist
            if (currUser != null && actualPw.equals(password)) {
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
                // adding user to session
                request.getSession().setAttribute("user", currUser);
                request.getServletContext().log("Login success");
            } else if (currUser == null) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
                request.getServletContext().log("Login failed");
            } else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "incorrect password");
                request.getServletContext().log("Login failed");
            }

            user_data.close();
            user_statement.close();

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
        } finally {
            out.close();
        }
    }
}
