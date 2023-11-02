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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name="Browse.BrowseGenreServlet",urlPatterns="/api/browse-genre")
public class BrowseGenreServlet extends HttpServlet{
    private static final long serialVersionUID = 1L; // This does nothing
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String genreId = request.getParameter("genre");
        request.getServletContext().log("getting id: " + genreId);
        String page = request.getParameter("page");
        String records = request.getParameter("records");
        String sortOrder = request.getParameter("sortOrder");

        int pageNum = 0;
        int limit = 25;
        try {
            if (page != null)
                pageNum = Integer.parseInt(page);
            if (records != null)
                limit = Integer.parseInt(records);
        } catch (Exception e) {System.out.println(e.getMessage());}

        String orderByStr = "ORDER BY r.rating DESC, m.title ";
        // t = title, r = rating, a = ascending, d = descending
        // [primary][direction][secondary][direction]
        if (sortOrder != null) {
            switch (sortOrder) {
                case "tara":
                    orderByStr = "ORDER BY m.title, r.rating ";
                    break;
                case "tard":
                    orderByStr = "ORDER BY m.title, r.rating DESC ";
                    break;
                case "tdra":
                    orderByStr = "ORDER BY m.title DESC, r.rating ";
                    break;
                case "tdrd":
                    orderByStr = "ORDER BY m.title DESC, r.rating DESC ";
                    break;
                case "rata":
                    orderByStr = "ORDER BY r.rating, m.title ";
                    break;
                case "ratd":
                    orderByStr = "ORDER BY r.rating, m.title DESC ";
                    break;
                case "rdta":
                    orderByStr = "ORDER BY r.rating DESC, m.title ";
                    break;
                case "rdtd":
                    orderByStr = "ORDER BY r.rating DESC, m.title DESC ";
                    break;
            }
        }

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            JsonArray resArray = new JsonArray();
            String movie_query = "SELECT m.id, m.title, m.year, m.director, r.rating FROM movies m " +
                    "JOIN genres_in_movies gim ON m.id = gim.movieId " +
                    "LEFT JOIN ratings r ON r.movieId = m.id " +
                    "WHERE gim.genreId = ? " +
                    orderByStr +
                    "LIMIT ? OFFSET ?";

            PreparedStatement movie_statement = conn.prepareStatement(movie_query);

            movie_statement.setString(1, genreId);
            movie_statement.setInt(2, limit + 1);
            movie_statement.setInt(3, limit*pageNum);
            System.out.println(movie_statement.toString());

            ResultSet movie_data = movie_statement.executeQuery();
            while (movie_data.next()) {
                JsonObject movieObj = new JsonObject();
                String movieId = movie_data.getString("id");
                String title = movie_data.getString("title");
                String year = movie_data.getString("year");
                String director = movie_data.getString("director");
                float rating = movie_data.getFloat("rating");

                String star_query = "SELECT s.id, s.name FROM stars_in_movies AS sim " +
                        "JOIN stars AS s ON sim.starId = s.id " +
                        "WHERE EXISTS (" +
                            "SELECT movieId FROM stars_in_movies AS sim2 " +
                            "WHERE sim2.movieId = ? AND s.id = sim2.starId )" +
                        "GROUP BY s.id, s.name, s.birthYear " +
                        "ORDER BY COUNT(*) DESC, s.name " +
                        "LIMIT 3";
                PreparedStatement star_statement = conn.prepareStatement(star_query);
                star_statement.setString(1, movieId);
                ResultSet star_data = star_statement.executeQuery();
                JsonArray star_array = new JsonArray();
                while (star_data.next()) {
                    JsonObject starObj = new JsonObject();
                    String starId = star_data.getString("id");
                    String starName = star_data.getString("name");

                    starObj.addProperty("star_id", starId);
                    starObj.addProperty("star_name", starName);
                    star_array.add(starObj);
                }

                String movie_genre_query = "SELECT g.id, g.name FROM genres_in_movies AS gim " +
                        "JOIN genres AS g ON gim.genreId = g.id " +
                        "WHERE gim.movieId = ? " +
                        "ORDER BY g.name " +
                        "LIMIT 3";
                PreparedStatement genre_statement = conn.prepareStatement(movie_genre_query);
                genre_statement.setString(1, movieId);
                ResultSet genre_data = genre_statement.executeQuery();
                JsonArray genre_array = new JsonArray();
                while(genre_data.next()) {
                    JsonObject genreObj = new JsonObject();
                    String gId = genre_data.getString("id");
                    String genreName = genre_data.getString("name");

                    genreObj.addProperty("genre_id", gId);
                    genreObj.addProperty("genre_name", genreName);
                    genre_array.add(genreObj);
                }

                movieObj.addProperty("movie_id", movieId);
                movieObj.addProperty("movie_title", title);
                movieObj.addProperty("movie_year", year);
                movieObj.addProperty("movie_director", director);
                movieObj.addProperty("movie_rating",rating);
                movieObj.add("movie_genres", genre_array);
                movieObj.add("movie_stars", star_array);
                resArray.add(movieObj);

                genre_statement.close();
                genre_data.close();
                star_statement.close();
                star_data.close();
            }

            movie_statement.close();
            movie_data.close();
            conn.close();

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


    }
}
