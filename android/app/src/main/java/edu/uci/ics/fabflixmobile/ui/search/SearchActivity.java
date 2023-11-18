package edu.uci.ics.fabflixmobile.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import edu.uci.ics.fabflixmobile.databinding.ActivitySearchBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

public class SearchActivity extends AppCompatActivity {
    private EditText movieQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySearchBinding binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        movieQuery = binding.movieQuery;
        final Button searchButton = binding.searchButton;

        searchButton.setOnClickListener(view -> search());
    }

    private void search() {
        finish();
        Intent MovieListPage = new Intent(SearchActivity.this, MovieListActivity.class);
        String query = movieQuery.getText().toString();
        MovieListPage.putExtra("movieQuery", query);
        startActivity(MovieListPage);
    }
}
