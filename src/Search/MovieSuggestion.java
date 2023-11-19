package Search;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "Search.MovieSuggestion", urlPatterns="/api/movie-suggestion")
public class MovieSuggestion extends HttpServlet {
    private DataSource dataSource;

    private static HashSet<String> stopwords = null;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String titleQuery = request.getParameter("title");

        try (Connection conn = dataSource.getConnection()) {

            if (titleQuery == null || titleQuery.trim().isEmpty()) {
                response.getWriter().write((new JsonArray()).toString());
                return;
            }

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

            String query = "SELECT id,title FROM movies" + "%1$s %2$s LIMIT 10";

            titleQuery = titleQuery.trim();
            String[] tokens = titleQuery.split("\\s+");

            final HashSet<String> search_words = new HashSet<String>();
            ArrayList<String> keywords = new ArrayList<String>();
            ArrayList<String> stopWords = new ArrayList<String>();
            String key_pattern = "";
            String like_pattern = "";

            for (String t : tokens) {
                if (t != "") {
                    String tempWord = t.toLowerCase();
                    if (!search_words.contains(tempWord)) {
                        if (stopwords.contains(tempWord)) {
                            stopWords.add("%"+t+"%");
                            like_pattern += "AND title LIKE ?";
                        }
                        else {
                            keywords.add("+"+t+"*");
                        }
                        search_words.add(t.toLowerCase());
                    }
                }
            }
            if (!keywords.isEmpty()) {
                key_pattern = " WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE)";
            }
            else if (!stopWords.isEmpty()) { // If the keywords are empty but there are stopwords
                like_pattern = "WHERE" + like_pattern.substring(3) + " ";
            }
            query = String.format(query,key_pattern,like_pattern);

            PreparedStatement titleStatement = conn.prepareStatement(query);
            int index = 1;
            if (!keywords.isEmpty()) {
                titleStatement.setString(index,String.join(" ",keywords));
                index++;
            }
            for (String s : stopWords) { // Add all the stop words to the LIKE operator
                titleStatement.setString(index,s);
                index++;
            }

            System.out.println(titleStatement);

            ResultSet titleSet = titleStatement.executeQuery();
            JsonArray resultArray = new JsonArray();
            while (titleSet.next()) {
                String id = titleSet.getString("id");
                String title = titleSet.getString("title");
                resultArray.add(generateMovieObject(id, title));
            }

            titleSet.close();
            titleStatement.close();

            response.getWriter().write(resultArray.toString());
            response.setStatus(200);
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        }
    }

    private static JsonObject generateMovieObject(String movieID, String movieName) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieName);
        jsonObject.addProperty("data", movieID);

        return jsonObject;
    }
}
