package edu.uci.ics.fabflixmobile.ui.singlemovie;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.chip.Chip;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.ActivitySingleMovieBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListViewAdapter;
import edu.uci.ics.fabflixmobile.ui.urlContstants;

public class SingleMovieListActivity extends AppCompatActivity {

    private Movie singleMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        setContentView(R.layout.activity_single_movie);
        // TODO: this should be retrieved from the backend server

        String movie_id = intent.getStringExtra("movie-id");
        Log.d("single-movie", "movie-id: " + movie_id);

        // Retrieve the movies from the database and load it into the app
        retrieveMovieInfo(movie_id);
    }

    private void retrieveMovieInfo(String movieId) {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest singleMovieRequest = new StringRequest(
                Request.Method.GET,
                urlContstants.baseURL + "/api/single-movie?id="+movieId,
                response -> {
                    Log.d("single-movie", response);
                    JsonArray single_movie_array = JsonParser.parseString(response).getAsJsonArray();
                    JsonObject single_movie_object = single_movie_array.get(0).getAsJsonObject();
                    loadMovieInfo(single_movie_object);
                    populateMoviePage();
                },
                error -> {
                    // error
                    Log.d("single-movie.error", error.toString());
                });
        queue.add(singleMovieRequest);
    }

    // Function loads json data into a single movie object and then pushes that movie object
    // to the SingleMovieListViewAdaptor
    private void loadMovieInfo(JsonObject movie_object) {
        Movie tempMovie = new Movie(
                movie_object.get("id").getAsString(),
                movie_object.get("title").getAsString(),
                movie_object.get("year").getAsInt()
        );
        String tempDirector = movie_object.get("director").getAsString();
        if (tempDirector != null) {tempMovie.setDirector(tempDirector);}

        JsonArray tempGenres = movie_object.getAsJsonArray("genres");
        if (tempGenres != null) {
            for (int i = 0; i < tempGenres.size(); i++) {
                JsonObject tempGenre = tempGenres.get(i).getAsJsonObject();
                tempMovie.addGenre(tempGenre.get("id").getAsString(),
                        tempGenre.get("name").getAsString());
            }
        }

        JsonArray tempStars = movie_object.getAsJsonArray("stars");
        if (tempStars != null) {
            for (int i = 0; i < tempStars.size(); i++) {
                JsonObject tempStar = tempStars.get(i).getAsJsonObject();
                String birthYear = null;
                if (tempStar.has("birthYear")) {
                    birthYear = tempStar.get("birthYear").getAsString();
                }
                tempMovie.addStar(tempStar.get("id").getAsString(), tempStar.get("name").getAsString(),
                        birthYear);
            }
        }

        this.singleMovie = tempMovie;
    }

    @SuppressLint("SetTextI18n")
    private void populateMoviePage() {
        ActivitySingleMovieBinding binding = ActivitySingleMovieBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        binding.movieTitle.setText(singleMovie.getName());
        binding.directorYear.setText(singleMovie.getDirector()+" • "+singleMovie.getYear());
        for (String[] g : singleMovie.getGenres()) {
            Chip genre_chip = new Chip(this);
            genre_chip.setText(g[1]);
            genre_chip.setChipBackgroundColorResource(R.color.teal_200);
            genre_chip.setTextColor(this.getResources().getColor(R.color.white));
            binding.genres.addView(genre_chip);
        }
        SingleMovieListViewAdaptor adapter = new SingleMovieListViewAdaptor(this,singleMovie.getStars());
        ListView listView = findViewById(R.id.StarList);
        listView.setAdapter(adapter);
    }
}
