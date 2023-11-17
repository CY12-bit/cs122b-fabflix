package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import org.json.JSONArray; // Not the com.google ones
import org.json.JSONObject;
import org.json.JSONException;
import edu.uci.ics.fabflixmobile.ui.urlContstants;

import java.util.ArrayList;

public class MovieListActivity extends AppCompatActivity {

    // Function retrieves movies from database
    // If successful, instantly loads them into app
    private void retrieveMovies(String page) {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        if (page == null) {
            page = "1";
        }
        final StringRequest movieRequest = new StringRequest(
                Request.Method.GET,
                urlContstants.baseURL + "/api/movielist?page="+page+"&limit=10",
                response -> {
                    Log.d("Received Movies", response);
                    finish();
                    try {
                        loadMovies(new JSONArray(response));
                    } catch (Exception E) {
                        Log.d("JSON Parse Error", response);
                    }
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                });
        queue.add(movieRequest);
    }

    // Function creates an array of movie objects and
    // inserts them into a list view
    private void loadMovies(JSONArray ms) throws JSONException {
        final ArrayList<Movie> movies = new ArrayList<Movie>();

        for (int c = 0; c < ms.length(); c++) {
            JSONObject m = ms.getJSONObject(c);

            String tempId = m.getString("movie_id");
            String tempName = m.getString("movie_title");
            Integer tempYear = m.getInt("movie_year");

            String tempDirector;
            try {tempDirector = m.getString("movie_director"); }
            catch (Exception e) {tempDirector = null; }
            JSONArray tempGenres;
            try { tempGenres = m.getJSONArray("movie_genres"); }
            catch (Exception e) { tempGenres = null; }
            JSONArray tempStars;
            try { tempStars = m.getJSONArray("movie_stars"); }
            catch (Exception e) { tempStars = null; }

            Movie tempMovie = new Movie(tempId,tempName,tempYear);
            if (tempDirector != null) { tempMovie.setDirector(tempDirector); }
            if (tempGenres != null) {
                for (int i = 0; i < tempGenres.length(); i++) {
                    JSONObject tempGenre = tempGenres.getJSONObject(i);
                    tempMovie.addGenre(tempGenre.getInt("genre_id"), tempGenre.getString("genre_name"));
                }
            }
            if (tempStars != null) {
                for (int i = 0; i < tempStars.length(); i++) {
                    JSONObject tempStar = tempStars.getJSONObject(i);
                    tempMovie.addStar(tempStar.getString("star_id"), tempStar.getString("star_name"));
                }
            }

            movies.add(tempMovie);
        }

        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = movies.get(position);
            @SuppressLint("DefaultLocale") String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getName(), movie.getYear());
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        // TODO: this should be retrieved from the backend server
        // By default the next page is one
        retrieveMovies("1");
    }



}