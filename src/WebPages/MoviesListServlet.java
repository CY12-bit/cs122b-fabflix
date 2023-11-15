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

            String val = request.getParameter("title");
            if (val != null) {
                movie_query += " WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE)";
            }

            /*
            // Find the correct WHERE CLAUSE filters
            ArrayList<String> where_cause = new ArrayList<String>();
            ArrayList<Integer> parameter_presence = new ArrayList<Integer>();

            final String[] categories = {"title", "year", "director", "star"};
            int parameter_counter = 1;

            // IN THE FUTURE, I WOULD NEED A HASTABLE WITH PARAMETERS AND SEPARATE SLOTS TO USE PREPAREDSTATEMENTS
            for (String c: categories) {
                String val = request.getParameter(c);
                if (val == null) {
                    parameter_presence.add(0);
                    continue;
                }
                if (c.equals("title")) {
                    where_cause.add(String.format("MATCH(title) AGAINST (? IN BOOLEAN MODE)", c));
                }
                else if (c.equals("director")) {
                    where_cause.add(String.format("%1$s LIKE ?", c));
                }
                else if (c.equals("year")) {
                    where_cause.add(String.format("%1$s = ?", c));
                }
                else {
                    where_cause.add(String.format("EXISTS (SELECT * FROM %1$ss_in_movies JOIN %1$ss ON %1$ss.id = %1$ss_in_movies.%1$sId WHERE movieId = movies.id AND %1$ss.name LIKE ?)", c));
                }
                parameter_presence.add(parameter_counter);
                parameter_counter++;
            }

            if (!where_cause.isEmpty()) {
                movie_query += "\nWHERE " + String.join(" AND ",where_cause);
            }
            */

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
            // movie_query += orderByStr + " LIMIT " + (limit + 1) + " OFFSET " + (limit*pageNum);
            
            PreparedStatement prepared_movie_query = conn.prepareStatement(movie_query);
            String match_pattern = "";
            if (val != null) {
                String[] words = val.split("[ ]+");
                for (String w : words) {
                    if (w != "") {
                        match_pattern += ("+" + w + "* ");
                    }
                }

                prepared_movie_query.setString(1,match_pattern);
            }
            prepared_movie_query.setInt(2,limit+1);
            prepared_movie_query.setInt(3,limit*pageNum);

            /*
            for (int counter = 0; counter < categories.length; counter++) {
                if (parameter_presence.get(counter) != 0) {
                    if (categories[counter].equals("year")) {
                        prepared_movie_query.setInt(parameter_presence.get(counter), Integer.parseInt(request.getParameter("year")));
                    }
                    else {
                        if (categories[counter].equals("title")) {
                            prepared_movie_query.setString(parameter_presence.get(counter),request.getParameter(categories[counter])+"*");
                        }
                        else {
                            prepared_movie_query.setString(parameter_presence.get(counter),"%" + request.getParameter(categories[counter])+"%");
                        }
                    }
                }
            }
            prepared_movie_query.setInt(parameter_counter,limit+1);
            prepared_movie_query.setInt(parameter_counter+1,limit*pageNum);
            */

            ResultSet movie_rs = prepared_movie_query.executeQuery();

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
