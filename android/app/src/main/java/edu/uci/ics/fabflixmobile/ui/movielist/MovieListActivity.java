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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.uci.ics.fabflixmobile.ui.urlContstants;

import java.util.ArrayList;

public class MovieListActivity extends AppCompatActivity {

    // Function retrieves movies from database
    // If successful, instantly loads them into app
    private void retrieveMovies(String page) {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest movieRequest = new StringRequest(
                Request.Method.GET,
                urlContstants.baseURL + "/api/movielist?page="+page+"&records=10",
                response -> {
                    Log.d("Received Movies", response);
                    finish();
                    try {
                        JsonArray movies = JsonParser.parseString(response).getAsJsonArray();
                        loadMovies(movies);
                    } catch (Exception E) {
                        Log.d("JSON Parse Error", E.getMessage());
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
    private void loadMovies(JsonArray ms) {
        final ArrayList<Movie> movies = new ArrayList<Movie>();

        for (int c = 0; c < ms.size(); c++) {
            JsonObject m = ms.get(c).getAsJsonObject();
            String tempId = m.get("movie_id").getAsString();
            String tempName = m.get("movie_title").getAsString();
            Integer tempYear = Integer.parseInt(m.get("movie_year").getAsString());

            String tempDirector = m.get("movie_director").getAsString();
            JsonArray tempGenres = m.getAsJsonArray("movie_genres");
            JsonArray tempStars = m.getAsJsonArray("movie_stars");

            Movie tempMovie = new Movie(tempId,tempName,tempYear);

            if (tempDirector != null) { tempMovie.setDirector(tempDirector); }
            if (tempGenres != null) {
                for (int i = 0; i < tempGenres.size(); i++) {
                    JsonObject tempGenre = tempGenres.get(i).getAsJsonObject();
                    tempMovie.addGenre(Integer.parseInt(tempGenre.get("genre_id").getAsString()),
                            tempGenre.get("genre_name").getAsString());
                }
            }
            if (tempStars != null) {
                for (int i = 0; i < tempStars.size(); i++) {
                    JsonObject tempStar = tempStars.get(i).getAsJsonObject();
                    tempMovie.addStar(tempStar.get("star_id").getAsString(), tempStar.get("star_name").getAsString());
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
        retrieveMovies("0");
    }



}