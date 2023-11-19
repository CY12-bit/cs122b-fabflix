package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

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
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieListActivity;
import edu.uci.ics.fabflixmobile.ui.urlContstants;
import java.util.ArrayList;
import android.widget.Toast;

public class MovieListActivity extends AppCompatActivity {

    private String movieQuery;
    private Integer page_num;
    private int list_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        setContentView(R.layout.activity_movielist);
        // TODO: this should be retrieved from the backend server

        movieQuery = intent.getStringExtra("movieQuery");
        Log.d("movielist", "movieQuery: " + movieQuery);

        // Retrieve the movies from the database and load it into the app
        retrieveMovies(page_num);

        // Am I allowed to change the ContextView?
        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        page_num = intent.getIntExtra("page_num",0);

        final Button prev_button = binding.prevButton;
        prev_button.setOnClickListener(view -> prevPage());
        final Button next_button = binding.nextButton;
        next_button.setOnClickListener(view -> nextPage());
        System.out.println("Established pagination");
    }


    @SuppressLint("SetTextI18n")
    private void nextPage() {
        if (list_size > 10) {
            retrieveMovies(page_num+1);
            page_num +=1;
        }
    }

    @SuppressLint("SetTextI18n")
    private void prevPage() {
        if (page_num > 0) {
            retrieveMovies(page_num-1);
            page_num -=1;
        }
    }

    // Function retrieves movies from database
    // If successful, instantly loads them into app
    private void retrieveMovies(Integer page) {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest movieRequest = new StringRequest(
                Request.Method.GET,
                urlContstants.baseURL + "/api/movielist?title="+movieQuery+"&page="+page+"&records=10",
                response -> {
                    Log.d("movielist", response);
                    JsonArray movies = JsonParser.parseString(response).getAsJsonArray();
                    loadMovies(movies);
                },
                error -> {
                    // error
                    Log.d("movielist.error", error.toString());
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
                    tempMovie.addGenre(tempGenre.get("genre_id").getAsString(),
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

        list_size = movies.size();

        if (list_size > 10) {
            movies.remove(movies.size() - 1);
        }

        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = movies.get(position);
            @SuppressLint("DefaultLocale") String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getName(), movie.getYear());
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            toSingleMovie(movie.getId());

        });

    }

    private void toSingleMovie(String movieId) {
        finish();
        System.out.println("Going to Single Star Page");
        Intent SingleMoviePage = new Intent(this, SingleMovieListActivity.class);
        SingleMoviePage.putExtra("movie-id",movieId);
        startActivity(SingleMoviePage);
    }
}