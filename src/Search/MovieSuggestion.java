package Search;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

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
            titleQuery = titleQuery.trim();
            String[] tokens = titleQuery.split("\\s+");
            for (int i=0; i<tokens.length; i++) {
                tokens[i] = "+" + tokens[i] + "*";
            }
            String query = "SELECT id, title FROM movies WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE) LIMIT 10";
            PreparedStatement titleStatement = conn.prepareStatement(query);
            titleStatement.setString(1, String.join(" ", tokens));
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
