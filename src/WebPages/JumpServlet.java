package WebPages;

import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "WebPages.JumpServlet", urlPatterns = "/api/movielist-jump")
public class JumpServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        JsonObject responseJsonObject = new JsonObject();
        HttpSession session = request.getSession();

        String prev_movielist = (String) session.getAttribute("prev-movielist");
        System.out.println("getting prev url: " + prev_movielist);
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
            System.out.println("setting prev-movielist: " + session.getAttribute("prev-movielist"));
            responseJsonObject.addProperty("prev-movielist", currMovielist);
        }
        response.getWriter().write(responseJsonObject.toString());
    }
}
