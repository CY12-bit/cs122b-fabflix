package WebPages;

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

@WebServlet(name = "WebPages.SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.setContentType("application/json");

        String id = request.getParameter("id");

        // logging progress
        request.getServletContext().log("getting id: " + id);
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            String star_query = "SELECT * FROM movies " +
                    "LEFT JOIN ratings ON movies.id = ratings.movieId " +
                    "WHERE movies.id = ?";
            PreparedStatement movie_statement = conn.prepareStatement(star_query);
            movie_statement.setString(1, id);

            String movie_genre_query = "SELECT g.id, g.name FROM genres_in_movies AS gim " +
                    "JOIN genres AS g ON gim.genreId = g.id " +
                    "WHERE gim.movieId = ? " +
                    "ORDER BY g.name";
            PreparedStatement genre_statement = conn.prepareStatement(movie_genre_query);
            genre_statement.setString(1, id);


            String movie_star_query = "SELECT s.id, s.name, s.birthYear FROM stars_in_movies AS sim " +
                    "JOIN stars AS s ON sim.starId = s.id " +
                    "WHERE EXISTS (" +
                        "SELECT movieId FROM stars_in_movies AS sim2 " +
                        "WHERE sim2.movieId = ? AND s.id = sim2.starId )" +
                    "GROUP BY s.id, s.name, s.birthYear " +
                    "ORDER BY COUNT(*) DESC, s.name";
            PreparedStatement star_statement = conn.prepareStatement(movie_star_query);
            star_statement.setString(1, id);

            JsonArray jsonArray = new JsonArray();
            JsonObject jsonObj = new JsonObject();

            ResultSet movie_data = movie_statement.executeQuery();
            while(movie_data.next()) {
                String title = movie_data.getString("title");
                int year = movie_data.getInt("year");
                String director = movie_data.getString("director");
                float rating = movie_data.getFloat("rating");
                int numVotes = movie_data.getInt("numVotes");

                // id retrieved from param
                jsonObj.addProperty("id", id);
                jsonObj.addProperty("title", title);
                jsonObj.addProperty("year", year);
                jsonObj.addProperty("director", director);
                jsonObj.addProperty("rating", rating);
                jsonObj.addProperty("numVotes", numVotes);
            }

            ResultSet genre_data = genre_statement.executeQuery();
            JsonArray genre_list = new JsonArray();
            while(genre_data.next()) {
                JsonObject genreObj = new JsonObject();
                String genre_id = genre_data.getString("id");
                String genre_name = genre_data.getString("name");

                genreObj.addProperty("id", genre_id);
                genreObj.addProperty("name", genre_name);
                genre_list.add(genreObj);
            }

            ResultSet star_data = star_statement.executeQuery();
            JsonArray star_list = new JsonArray();
            while (star_data.next()) {
                String star_id = star_data.getString("id");
                String star_name = star_data.getString("name");
                int star_birth_year = star_data.getInt("birthYear");
                boolean yearIsNull = star_data.wasNull();

                JsonObject single_star_obj = new JsonObject();
                single_star_obj.addProperty("id", star_id);
                single_star_obj.addProperty("name", star_name);
                if (!yearIsNull)
                    single_star_obj.addProperty("birthYear", star_birth_year);
                star_list.add(single_star_obj);
            }

            if (!jsonObj.isEmpty()) {
                jsonObj.add("genres", genre_list);
                jsonObj.add("stars", star_list);
                jsonArray.add(jsonObj);
            }

            movie_data.close();
            movie_statement.close();
            genre_data.close();
            genre_statement.close();
            star_data.close();
            star_statement.close();
            conn.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
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
