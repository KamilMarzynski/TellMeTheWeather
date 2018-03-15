package org.nachosapps.weatherapplication.Adapters;

import static org.nachosapps.weatherapplication.Activities.MainActivity.NUMBER_OF_FORECAST;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.nachosapps.weatherapplication.Common.CommonHelpers;
import org.nachosapps.weatherapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kamil on 2018-02-27.
 */

public class GridElementAdapter extends RecyclerView.Adapter<GridElementAdapter.SimpleViewHolder> {

    private Context context;
    private List<String> dates;
    private List<String> images;
    private List<String> temperatures;

    public GridElementAdapter(String dates[], String images[], String temperatures[],
            Context context) {
        this.dates = new ArrayList<>();
        this.images = new ArrayList<>();
        this.temperatures = new ArrayList<>();
        this.context = context;

        for (int i = 0; i < NUMBER_OF_FORECAST; i++) {
            this.dates.add(i, dates[i]);
            this.images.add(i, images[i]);
            this.temperatures.add(i, temperatures[i]);
        }
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {
        final ImageView weatherImage;
        final TextView txtDate;
        final TextView txtTemp;
        final TextView txtHour;

        SimpleViewHolder(View view) {
            super(view);
            weatherImage = view.findViewById(R.id.weatherImage);
            txtDate = view.findViewById(R.id.forecastDate);
            txtTemp = view.findViewById(R.id.forecastTemperature);
            txtHour = view.findViewById(R.id.forecastHour);
        }
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(this.context).inflate(R.layout.forecast_item_layout,
                parent, false);
        TransitionManager.beginDelayedTransition(parent, new Slide());

        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.botom_up_slow);
        holder.txtTemp.setText(temperatures.get(position));
        holder.txtTemp.startAnimation(animation);
        if (dates.get(position) != null) {
            holder.txtDate.setText(dates.get(position).substring(0, 11));
            holder.txtDate.startAnimation(animation);
            holder.txtHour.setText(dates.get(position).substring(11, 16));
            holder.txtHour.startAnimation(animation);
        }
        Picasso.with(this.context).load(CommonHelpers.getImage(images.get(position), context)).placeholder(
                R.drawable.ierror).error(R.drawable.ierror).into
                (holder.weatherImage);
        holder.weatherImage.startAnimation(animation);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return this.temperatures.size();
    }


}
