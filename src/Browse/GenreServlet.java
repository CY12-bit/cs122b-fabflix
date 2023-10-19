package Browse;

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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name="Browse.GenresServlet",urlPatterns="/api/allGenres")
public class GenreServlet extends HttpServlet {
    // IDK man
    private static final long serialVersionUID = 1L; // This does nothing

    private DataSource dataSource;

    // Function initiates servlet?
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    /*
    Project 1: Function will return results for movie list.
    - Will query from multiple databases to return results in JSON ARRAY.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter(); // This will stream out static html code.

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            Statement statement = conn.createStatement();

            final String all_genres_query = "SELECT * FROM genres ORDER BY name";

            ResultSet all_genres_rs = statement.executeQuery(all_genres_query);

            JsonArray genreList = new JsonArray();

            while (all_genres_rs.next()) {
                String genre_id = all_genres_rs.getString("id");
                String genre_name = all_genres_rs.getString("name");

                JsonObject temp_object = new JsonObject();
                temp_object.addProperty("genre_id",genre_id);
                temp_object.addProperty("genre_name",genre_name);
                genreList.add(temp_object);
            }

            all_genres_rs.close();
            all_genres_rs.close();

            // Write JSON string to output
            out.write(genreList.toString());
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
}
