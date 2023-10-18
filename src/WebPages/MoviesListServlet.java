package WebPages;

import java.util.ArrayList;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

@WebServlet(name="WebPages.MoviesListServlet",urlPatterns="/api/movielist")
public class MoviesListServlet extends HttpServlet {
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

            String movie_query = "SELECT * FROM movies"
                    + " LEFT JOIN ratings ON ratings.movieId = movies.id";

            // Find the correct WHERE CLAUSE filters
            Map<String,String[]> search_parameters = request.getParameterMap();
            System.out.println(search_parameters.size());
            ArrayList<String> where_cause = new ArrayList<String>();
            where_cause.add("\nWHERE ");
            for (Map.Entry<String,String[]> entry : search_parameters.entrySet() ) {
                if (!entry.getKey().equals("genre") && !entry.getKey().equals("star") && !entry.getKey().equals("year")) {
                    where_cause.add(String.format("%1$s LIKE '%2$s%%'", entry.getKey(), entry.getValue()[0]));
                }
                else if (entry.getKey().equals("year")) {
                    where_cause.add(String.format("%1$s = %2$s", entry.getKey(), entry.getValue()[0]));
                }
                else {
                    where_cause.add(String.format("EXISTS (SELECT * FROM %1$ss_in_movies JOIN %1$ss ON %1$ss.id = %1$ss_in_movies.%1$sId WHERE movieId = movies.id AND %1$ss.name LIKE '%2$s%%')",
                            entry.getKey(), entry.getValue()[0]));
                }
                where_cause.add(" AND ");
            }

            if (where_cause.size() != 1) {
                where_cause.remove(where_cause.size()-1);
                movie_query += String.join("",where_cause);
            }

            movie_query += " ORDER BY rating DESC LIMIT 20";
            System.out.println(movie_query);
            ResultSet movie_rs = statement.executeQuery(movie_query);

            JsonArray movieList = new JsonArray();

            String genre_query = "SELECT * FROM genres JOIN genres_in_movies ON genres.id = genres_in_movies.genreId" +
                    " WHERE movieId = ? ORDER BY name ASC LIMIT 3";

            String stars_query = "SELECT a.id, a.name, a.birthYear, COUNT(*) AS numMovies\n" +
                    "FROM stars a\n" +
                    "JOIN stars_in_movies ON a.id = stars_in_movies.starId\n" +
                    "WHERE EXISTS (\n" +
                    "\tSELECT b.movieId\n" +
                    "    FROM stars_in_movies b\n" +
                    "    WHERE b.movieId = ? \n" +
                    "    AND a.id = b.starId\n" +
                    ")\n" +
                    "GROUP BY a.id, a.name, a.birthYear\n" +
                    "ORDER BY numMovies DESC\n" +
                    "LIMIT 3";

            while (movie_rs.next()) {
                String movie_id = movie_rs.getString("id");

                PreparedStatement prep_query = conn.prepareStatement(genre_query);
                prep_query.setString(1, movie_id);
                ResultSet temp_set = prep_query.executeQuery();

                JsonArray genre_array = new JsonArray();
                while (temp_set.next()) {
                    JsonObject temp_object = new JsonObject();
                    temp_object.addProperty("genre_id",temp_set.getString("id"));
                    temp_object.addProperty("genre_name",temp_set.getString("name"));
                    genre_array.add(temp_object);
                }

                prep_query = conn.prepareStatement(stars_query);
                prep_query.setString(1,movie_id);
                temp_set = prep_query.executeQuery();

                JsonArray stars_array = new JsonArray();
                while (temp_set.next()) {
                    JsonObject temp_object = new JsonObject();
                    temp_object.addProperty("star_id",temp_set.getString("id"));
                    temp_object.addProperty("star_name",temp_set.getString("name"));
                    stars_array.add(temp_object);
                }

                String movie_title = movie_rs.getString("title");
                String movie_year = movie_rs.getString("year");
                String movie_director = movie_rs.getString("director");
                Float movie_rating = movie_rs.getFloat("rating");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating",movie_rating);

                jsonObject.add("movie_genres", genre_array); // Have to check if that's okay
                jsonObject.add("movie_stars", stars_array);

                movieList.add(jsonObject);
                prep_query.close();
                temp_set.close();
            }
            movie_rs.close();
            conn.close();
            // Write JSON string to output
            out.write(movieList.toString());
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
