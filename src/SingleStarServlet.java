import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            JsonArray resArray = new JsonArray();
            JsonObject starObj = new JsonObject();

            String star_query = "SELECT * FROM stars WHERE id = ?";
            PreparedStatement star_statement = conn.prepareStatement(star_query);
            star_statement.setString(1, id);

            String movie_query = "SELECT m.id, m.title, m.director, m.year FROM stars AS S " +
                    "LEFT JOIN stars_in_movies AS SIM ON S.id = SIM.starId " +
                    "LEFT JOIN movies AS m ON m.id = SIM.movieId " +
                    "WHERE S.id = ?";
            PreparedStatement movie_statement = conn.prepareStatement(movie_query);
            movie_statement.setString(1, id);

            ResultSet star_data = star_statement.executeQuery();
            while(star_data.next()) {
                String name = star_data.getString("name");
                int birthYear = star_data.getInt("birthYear");
                boolean yearIsNull = star_data.wasNull();

                starObj.addProperty("name", name);
                if (!yearIsNull)
                    starObj.addProperty("birthYear", birthYear);
            }

            ResultSet movie_data = movie_statement.executeQuery();
            JsonArray movie_list = new JsonArray();
            while (movie_data.next()) {
                JsonObject movieObj = new JsonObject();
                String movie_id = movie_data.getString("id");
                String movie_title = movie_data.getString("title");
                int movie_year = movie_data.getInt("year");
                String movie_director = movie_data.getString("director");

                movieObj.addProperty("id", movie_id);
                movieObj.addProperty("title", movie_title);
                movieObj.addProperty("year", movie_year);
                movieObj.addProperty("director", movie_director);
                movie_list.add(movieObj);
            }

            if (!movie_list.isEmpty())
                starObj.add("movies", movie_list);
            if (!starObj.isEmpty())
                resArray.add(starObj);

            star_statement.close();
            star_data.close();
            movie_statement.close();
            movie_data.close();

            // Write JSON string to output
            out.write(resArray.toString());
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

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
