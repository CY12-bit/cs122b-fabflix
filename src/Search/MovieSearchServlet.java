package Search;

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
import java.util.HashSet;

// @WebServlet(name="Search.MovieSearchServlet",urlPatterns="/api/movielistv2")
public class MovieSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L; // This does nothing
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private String determineOrder(String sortOrder) {
        String orderByStr = "ORDER BY rating DESC, title ";
        if (sortOrder != null) {
            switch (sortOrder) {
                case "tara":
                    orderByStr = "ORDER BY title, rating ";
                    break;
                case "tard":
                    orderByStr = "ORDER BY title, rating DESC ";
                    break;
                case "tdra":
                    orderByStr = "ORDER BY title DESC, rating ";
                    break;
                case "tdrd":
                    orderByStr = "ORDER BY title DESC, rating DESC ";
                    break;
                case "rata":
                    orderByStr = "ORDER BY rating, title ";
                    break;
                case "ratd":
                    orderByStr = "ORDER BY rating, title DESC ";
                    break;
                case "rdta":
                    orderByStr = "ORDER BY rating DESC, title ";
                    break;
                case "rdtd":
                    orderByStr = "ORDER BY rating DESC, title DESC ";
                    break;
            }
        }
        return orderByStr;
    }
    private int[] determinePage(String page, String records) {
        int pageNum = 0;
        int limit = 25;
        try {
            if (page != null)
                pageNum = Integer.parseInt(page);
            if (records != null)
                limit = Integer.parseInt(records);
        } catch (Exception e) {System.out.println(e.getMessage());}
        return new int[]{pageNum,limit};
    }
    private String buildQuery() { // Query is different from the three smaller queries
        return "WITH movie_page AS (\n" +
                "\tSELECT *\n" +
                "    FROM movies\n" +
                "    LEFT JOIN ratings ON ratings.movieId = movies.id\n" +
                "\t WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE)" +
                "\t%1$s\n" + // Order by
                "\tLIMIT ? OFFSET ?\n" + // Limit and offset
                "), genre_page AS (\n" +
                "\tSELECT id AS genre_id, name as genre_name, movieId,\n" +
                "\tROW_NUMBER() OVER(\n" +
                "\t\tPARTITION BY genres_in_movies.movieId\n" +
                "\t\tORDER BY genres.name \n" +
                "\t) AS row_num\n" +
                "\tFROM genres\n" +
                "\tJOIN genres_in_movies on genres.id = genres_in_movies.genreId\n" +
                "    WHERE EXISTS (SELECT id from movie_page WHERE movie_page.id = movieId)\n" +
                "), star_page AS (\n" +
                "\tSELECT id AS star_id, name as star_name, movieId,\n" +
                "\tROW_NUMBER() OVER (\n" +
                "\t\tPARTITION BY stars_in_movies.movieId\n" +
                "\t\tORDER BY COUNT(*) DESC\n" +
                "\t) AS row_num\n" +
                "\tFROM stars\n" +
                "\tJOIN stars_in_movies ON stars.id = stars_in_movies.starId\n" +
                "    WHERE EXISTS (SELECT id from movie_page WHERE movie_page.id = stars_in_movies.movieId)\n" +
                "\tGROUP BY stars.id\n" +
                ")\n" +
                "SELECT id,title,year,director,price,\n" +
                "genre_id,genre_name,star_id,star_name,rating\n" +
                "FROM movie_page\n" +
                "LEFT JOIN star_page ON movie_page.id = star_page.movieId\n" +
                "LEFT JOIN genre_page ON genre_page.movieId = movie_page.id\n" +
                "WHERE star_page.row_num <= 3 AND genre_page.row_num <= 3;";
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Build the Query
        // Make PreparedStatement
        // Execute PreparedStatement
        // Translate Results
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter(); // This will stream out static html code.

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            String movieQuery = buildQuery();

            String sortOrder = request.getParameter("sortOrder");
            String orderByStr = determineOrder(sortOrder);
            String page = request.getParameter("page");
            String records = request.getParameter("records");
            final int[] pagination_info = determinePage(page,records);
            final int pageNum = pagination_info[0];
            final int limit = pagination_info[1];
            movieQuery = String.format(movieQuery,orderByStr);

            String searchedMovie = request.getParameter("title");

            PreparedStatement movie_prep = conn.prepareStatement(movieQuery);
            movie_prep.setString(1,searchedMovie+"*");
            movie_prep.setInt(2,limit);
            movie_prep.setInt(3,limit*pageNum);

            ResultSet movie_rs = movie_prep.executeQuery();

            JsonArray movieList = new JsonArray();
            JsonArray genreArray = new JsonArray();
            JsonArray starArray = new JsonArray();
            JsonObject tempObject = new JsonObject();

            HashSet<String> movie_ids = new HashSet<String>();
            HashSet<String> movie_genres = new HashSet<String>();
            HashSet<String> movie_stars = new HashSet<String>();

            while(movie_rs.next()) {
                String tempId = movie_rs.getString("id");
                String tempTitle = movie_rs.getString("title");
                String tempYear = movie_rs.getString("year");
                String tempDirector = movie_rs.getString("director");
                Float tempRating = movie_rs.getFloat("rating");
                String genreId = movie_rs.getString("genre_id");
                String genreName = movie_rs.getString("genre_name");
                String starId = movie_rs.getString("star_id");
                String starName = movie_rs.getString("star_name");
                if (!movie_ids.contains(tempId)) {
                    if (!tempObject.isEmpty()) {
                        tempObject.add("movie_genres", genreArray);
                        tempObject.add("movie_stars", starArray);
                        System.out.println(tempTitle);
                        movieList.add(tempObject);
                        movie_genres.clear();
                        movie_stars.clear();
                        tempObject = new JsonObject();
                        genreArray = new JsonArray();
                        starArray = new JsonArray();
                    }
                    tempObject.addProperty("movie_id",tempId);
                    tempObject.addProperty("movie_title",tempTitle);
                    tempObject.addProperty("movie_year", tempYear);
                    tempObject.addProperty("movie_director", tempDirector);
                    tempObject.addProperty("movie_rating",tempRating);
                    movie_ids.add(tempId);
                }
                if (genreId != null && !movie_genres.contains(genreId)) {
                    JsonObject genreObj = new JsonObject();
                    genreObj.addProperty("genre_id", genreId);
                    genreObj.addProperty("genre_name", genreName);
                    genreArray.add(genreObj);
                    movie_genres.add(genreId);
                }
                if (starId != null && !movie_stars.contains(starId)) {
                    JsonObject starObj = new JsonObject();
                    starObj.addProperty("star_id", starId);
                    starObj.addProperty("star_name", starName);
                    starArray.add(starObj);
                    movie_stars.add(starId);
                }
            }
            if (!tempObject.isEmpty()) {
                tempObject.add("movie_genres", genreArray);
                tempObject.add("movie_stars", starArray);
                movieList.add(tempObject);
                movie_genres.clear();
                movie_stars.clear();
                tempObject = new JsonObject();
                genreArray = new JsonArray();
                starArray = new JsonArray();
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

        // Return Results
    }

}
