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
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "WebPages.JumpServlet", urlPatterns = "/api/movielist-jump")
public class JumpServlet extends HttpServlet {
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
        PrintWriter out = response.getWriter();

        JsonObject responseJsonObject = new JsonObject();
        HttpSession session = request.getSession();

        String prev_movielist = (String) session.getAttribute("prev-movielist");
        // System.out.println("getting prev url: " + prev_movielist);
        if (prev_movielist == null) {
            prev_movielist = "";
        }
        responseJsonObject.addProperty("prev-movielist", prev_movielist);

        out.write(responseJsonObject.toString());
        out.close();
        response.setStatus(200);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String currMovielist = request.getParameter("curr-movielist");

        JsonObject responseJsonObject = new JsonObject();
        if (currMovielist != null) {
            HttpSession session = request.getSession();
            session.setAttribute("prev-movielist", currMovielist);
            // System.out.println("setting prev-movielist: " + session.getAttribute("prev-movielist"));
            responseJsonObject.addProperty("prev-movielist", currMovielist);
        }
        response.getWriter().write(responseJsonObject.toString());
    }
}
