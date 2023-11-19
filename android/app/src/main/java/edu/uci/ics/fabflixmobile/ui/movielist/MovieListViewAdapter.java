package edu.uci.ics.fabflixmobile.ui.movielist;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MovieListViewAdapter extends ArrayAdapter<Movie> {
    private final ArrayList<Movie> movies;

    // View lookup cache
    private static class ViewHolder {
        TextView movie_title;
        TextView movie_info;
        TextView genres;
        TextView stars;
    }

    public MovieListViewAdapter(Context context, ArrayList<Movie> movies) {
        super(context, R.layout.movielist_row, movies);
        this.movies = movies;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the movie item for this position
        Movie movie = movies.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.movielist_row, parent, false);
            viewHolder.movie_title = convertView.findViewById(R.id.star_name);
            viewHolder.movie_info = convertView.findViewById(R.id.star_birthYear);
            viewHolder.genres = convertView.findViewById(R.id.genres);
            viewHolder.stars = convertView.findViewById(R.id.stars);

            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.
        viewHolder.movie_title.setText(movie.getName());
        viewHolder.movie_info.setText(movie.getDirector() + " | " + movie.getYear());

        StringBuilder genreStr = new StringBuilder();
        for (String[] g : movie.getGenres()) {
            genreStr.append(", ").append(g[1]);
        }

        StringBuilder starStr = new StringBuilder();
        for (String[] s : movie.getStars()) {
            starStr.append(", ").append(s[1]);
        }

        viewHolder.genres.setText("Genres: " + genreStr.toString().replaceFirst(",",""));
        viewHolder.stars.setText("Stars: " + starStr.toString().replaceFirst(",",""));

        // Return the completed view to render on screen
        return convertView;
    }
}