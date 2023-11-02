package Employee;

import com.google.gson.JsonArray;
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
import java.sql.*;

@WebServlet(name = "Employee.DashboardServlet", urlPatterns = "/api/employee/dashboard")
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
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            // type = star or movie
            JsonObject responseObj = new JsonObject();
            String type = request.getParameter("type");
            if (type != null && type.equals("star")) {
                responseObj = addStar(request.getParameter("starName"), request.getParameter("birthYear"), conn);
            } else if (type != null && type.equals("movie")) {
                // TODO: call addMovie
            }
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

    private JsonObject addStar(String starName, String birthYearStr, Connection conn) throws SQLException {
        if (starName == null || birthYearStr == null)
            return null;
        int birthYear = 0;
        try {
            birthYear = Integer.parseInt(birthYearStr);
        } catch (Exception e) {
            return null;
        }

        String maxIdQuery = "SELECT id FROM stars ORDER BY SUBSTRING(id, 3) DESC LIMIT 1";
        PreparedStatement maxIdStatement = conn.prepareStatement(maxIdQuery);
        ResultSet maxIdSet = maxIdStatement.executeQuery();
        String maxId = "";
        while (maxIdSet.next()) {
            maxId = maxIdSet.getString("id");
        }

        String newStarId = "nm" + (Integer.parseInt(maxId.substring(2)) + 1);

        String insertStarQuery = "INSERT INTO stars (id, name, birthYear) VALUES(?, ?, ?)";
        PreparedStatement insertStarStatement = conn.prepareStatement(insertStarQuery);
        insertStarStatement.setString(1, newStarId);
        insertStarStatement.setString(2, starName);
        insertStarStatement.setInt(3, birthYear);

        JsonObject responseObj = new JsonObject();
        responseObj.addProperty("status", "success");
        responseObj.addProperty("star_id", newStarId);

        return responseObj;
    }

    // TODO: finish implementing
    private JsonObject addMovie(
            String movieTitle, String movieYear,
            String movieDirector, String starName,
            String genreName, Connection conn
    ) {
        return null;
    }

    // Function provides metadata of database


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            JsonArray tableArray = new JsonArray();
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tableNamesSet = metaData.getTables(null, null, null, new String[]{"TABLE"});
            while (tableNamesSet.next()) {
                JsonObject tableObject = new JsonObject();
                String tableName = tableNamesSet.getString("TABLE_NAME");
                tableObject.addProperty("table_name", tableName);

                JsonArray columnArray = new JsonArray();
                ResultSet columnSet = metaData.getColumns(null, null, tableName, null);
                while (columnSet.next()) {
                    String columnName = columnSet.getString("COLUMN_NAME");
                    String dataType = JDBCType.valueOf(columnSet.getInt("DATA_TYPE")).getName();
                    if (dataType.equals("VARCHAR")) {
                        dataType += "(" + columnSet.getString("COLUMN_SIZE") + ")";
                    }

                    JsonObject dataObj = new JsonObject();
                    dataObj.addProperty("column_name", columnName);
                    dataObj.addProperty("data_type", dataType);
                    columnArray.add(dataObj);
                }
                tableObject.add("columns", columnArray);
                tableArray.add(tableObject);
                columnSet.close();
            }

            tableNamesSet.close();

            out.write(tableArray.toString());
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
