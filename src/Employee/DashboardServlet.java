package Employee;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;

@WebServlet(name = "Employee.DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /*
    Function handles all inserts from employee.
    - Insert a new star into the database
    - Calls a stored procedure that would insert a new movie into the database
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    // Function provides metadata of database
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }
}
