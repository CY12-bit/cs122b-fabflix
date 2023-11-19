package edu.uci.ics.fabflixmobile.ui.singlemovie;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.uci.ics.fabflixmobile.R;

import java.util.ArrayList;

public class SingleMovieListViewAdaptor extends ArrayAdapter<String[]> {

    private ArrayList<String[]> stars;

    private static class ViewHolder {
        TextView star_name;
        TextView star_year;
    }

    public SingleMovieListViewAdaptor(Context context, ArrayList<String[]> stars) {
        super(context, R.layout.singlemovielist_row, stars);
        this.stars = stars;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String[] star = stars.get(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new SingleMovieListViewAdaptor.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.singlemovielist_row, parent, false);
            viewHolder.star_name = convertView.findViewById(R.id.star_name);
            viewHolder.star_year = convertView.findViewById(R.id.star_birthYear);

            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (SingleMovieListViewAdaptor.ViewHolder) convertView.getTag();
        }

        viewHolder.star_name.setText(star[1]);
        viewHolder.star_year.setText(star[2]);

        return convertView;
    }
}
