package WebPages;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
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
import java.util.concurrent.TimeUnit;

/*

1. implement servlet for full text against title
- remove the 4 search options in index.html and replace it with just one search bar that uses full text search against title
2. combine the 3 queries into 1
   - ie right now the code queries for a movie's genre + stars separate from the movie titles
   - browserGenreServlet and browseTitleServlet
 */

@WebServlet(name="WebPages.MoviesListServlet",urlPatterns="/api/movielist")
public class MoviesListServlet extends HttpServlet {
    // IDK man
    private static final long serialVersionUID = 1L; // This does nothing

    private static HashSet<String> stopwords = null;

    private DataSource dataSource;

    private FileWriter timeLog = null;

    // Function initiates servlet?
    public void init(ServletConfig config) {
        try {
            super.init(config);
        } catch (Exception ignore) {}

        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }

        String contextPath = getServletContext().getRealPath("/");
        String xmlFilePath=contextPath+"timeLog.txt";
        System.out.println(xmlFilePath);
        try {
            timeLog = new FileWriter(xmlFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long servletStartTime = System.nanoTime();
        long totalJDBCTime = 0;
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter(); // This will stream out static html code.

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // If we haven't loaded the stopwords yet, we load them from the database
            if (stopwords == null) {
                Statement stop_statement = conn.createStatement();
                stopwords = new HashSet<String>();
                ResultSet sWords = stop_statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.INNODB_FT_DEFAULT_STOPWORD;");
                while (sWords.next()) {
                    stopwords.add(sWords.getString("value"));
                }
                System.out.println("Retrieved stop words:" + stopwords.toString());
                sWords.close();
                stop_statement.close();
            }

            String movie_query = "SELECT * FROM movies"
                    + " LEFT JOIN ratings ON ratings.movieId = movies.id" +
                    "%1$s %2$s";

            String val = request.getParameter("title");
            // If there is a title for us to do, let's add the necessary where cause

            String sortOrder = request.getParameter("sortOrder");
            String orderByStr = " ORDER BY rating DESC, title ";
            // t = title, r = rating, a = ascending, d = descending
            // [primary][direction][secondary][direction]
            if (sortOrder != null) {
                switch (sortOrder) {
                    case "tara":
                        orderByStr = " ORDER BY title, rating ";
                        break;
                    case "tard":
                        orderByStr = " ORDER BY title, rating DESC ";
                        break;
                    case "tdra":
                        orderByStr = " ORDER BY title DESC, rating ";
                        break;
                    case "tdrd":
                        orderByStr = " ORDER BY title DESC, rating DESC ";
                        break;
                    case "rata":
                        orderByStr = " ORDER BY rating, title ";
                        break;
                    case "ratd":
                        orderByStr = " ORDER BY rating, title DESC ";
                        break;
                    case "rdta":
                        orderByStr = " ORDER BY rating DESC, title ";
                        break;
                    case "rdtd":
                        orderByStr = " ORDER BY rating DESC, title DESC ";
                        break;
                }
            }

            String page = request.getParameter("page");
            String records = request.getParameter("records");

            int pageNum = 0;
            int limit = 25;
            try {
                if (page != null)
                    pageNum = Integer.parseInt(page);
                if (records != null)
                    limit = Integer.parseInt(records);
            } catch (Exception e) {System.out.println(e.getMessage());}

            movie_query += orderByStr + "LIMIT ? OFFSET ?";

            HashSet<String> search_words = new HashSet<String>();
            ArrayList<String> keywords = new ArrayList<String>();
            ArrayList<String> stopWords = new ArrayList<String>();
            String key_pattern = "";
            String like_pattern = "";
            if (val != null) {
                String[] words = val.split("[ ]+");
                for (String w : words) {
                    if (w != "") {
                        String tempWord = w.toLowerCase();
                        if (!search_words.contains(tempWord)) {
                            if (stopwords.contains(tempWord)) {
                                stopWords.add("%"+w+"%");
                                like_pattern += "AND title LIKE ?";
                            }
                            else {
                                keywords.add("+"+w+"*");
                            }
                            search_words.add(w.toLowerCase());
                        }
                    }
                }
                if (!keywords.isEmpty()) {
                    key_pattern = " WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE)";
                }
                else if (!stopWords.isEmpty()) { // If the keywords are empty but there are stopwords
                    like_pattern = "WHERE" + like_pattern.substring(3) + " ";
                }
            }
            movie_query = String.format(movie_query,key_pattern,like_pattern);
            PreparedStatement prepared_movie_query = conn.prepareStatement(movie_query);
            int index = 1;
            if (!keywords.isEmpty()) {
                prepared_movie_query.setString(index,String.join(" ",keywords));
                index++;
            }
            for (String s : stopWords) { // Add all the stop words to the LIKE operator
                prepared_movie_query.setString(index,s);
                index++;
            }

            prepared_movie_query.setInt(index,limit+1);
            prepared_movie_query.setInt(index+1,limit*pageNum);

            System.out.println(prepared_movie_query.toString());

            long jdbcStart = System.nanoTime();
            ResultSet movie_rs = prepared_movie_query.executeQuery();
            long jdbcEnd = System.nanoTime();
            totalJDBCTime += jdbcEnd - jdbcStart;

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
            PreparedStatement prep_genre_query = conn.prepareStatement(genre_query);
            PreparedStatement prep_star_query = conn.prepareStatement(stars_query);

            while (movie_rs.next()) {
                String movie_id = movie_rs.getString("id");

                prep_genre_query.setString(1, movie_id);
                jdbcStart = System.nanoTime();
                ResultSet temp_set = prep_genre_query.executeQuery();
                jdbcEnd = System.nanoTime();
                totalJDBCTime += jdbcEnd - jdbcStart;

                JsonArray genre_array = new JsonArray();
                while (temp_set.next()) {
                    JsonObject temp_object = new JsonObject();
                    temp_object.addProperty("genre_id",temp_set.getString("id"));
                    temp_object.addProperty("genre_name",temp_set.getString("name"));
                    genre_array.add(temp_object);
                }

                prep_star_query.setString(1,movie_id);
                jdbcStart = System.nanoTime();
                temp_set = prep_star_query.executeQuery();
                jdbcEnd = System.nanoTime();
                totalJDBCTime += jdbcEnd - jdbcStart;

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
                temp_set.close();
            }
            prep_star_query.close();
            prep_genre_query.close();
            prepared_movie_query.close();
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
        long servletEndTime = System.nanoTime();
        long totalServletTime = servletEndTime - servletStartTime;

        // System.out.println("ts: " + TimeUnit.NANOSECONDS.toMillis(totalServletTime) + "; tj: "+ TimeUnit.NANOSECONDS.toMillis(totalJDBCTime) + "\n");
        timeLog.write("ts: " + TimeUnit.NANOSECONDS.toMillis(totalServletTime) + "; tj: "+ TimeUnit.NANOSECONDS.toMillis(totalJDBCTime) + "\n");
    }

    public void destroy() {
        try {
            timeLog.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.destroy();
    }
}
