package Employee;

import Recapta.RecaptchaVerifyUtils;
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

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "Employee.EmployeeLoginServlet", urlPatterns = "/api/employee/login")
public class EmployeeLoginServlet extends HttpServlet {

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
        JsonObject responseJsonObject = new JsonObject();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);

        } catch (Exception E){
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "reCapta Failed");
            System.out.println("ReCapta Failed" + E.getMessage());
            out.write(responseJsonObject.toString());
            out.close();
            return ;
        }

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            String user_query = "SELECT e.email, e.password FROM employees e " +
                    "WHERE e.email = ?";
            PreparedStatement user_statement = conn.prepareStatement(user_query);
            user_statement.setString(1, username);

            ResultSet user_data = user_statement.executeQuery();
            Employee currEmployee = null;
            boolean passwordCorrect = false;
            while (user_data.next()) {
                String email = user_data.getString("email");
                String encryptedPassword = user_data.getString("password");
                passwordCorrect = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                currEmployee = new Employee(email);
            }

            // username doesn't exist
            if (currEmployee != null && passwordCorrect) {
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
                request.getSession().setAttribute("employee", currEmployee); // Creates a new session
                request.getServletContext().log("Login success");
            } else if (currEmployee == null) {
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
        } finally {
            out.close();
        }
    }
}
