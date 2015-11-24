package net.tbennett.mysunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    private WeatherViewHolder viewHolder;

  public ForecastAdapter(Context context, Cursor c, int flags) {
    super(context, c, flags);
  }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    /**
   * Prepare the weather high/lows for presentation.
   */
  private String formatHighLows(double high, double low) {
    boolean isMetric = Utility.isMetric(mContext);
    String highLowStr = Utility.formatTemperature(mContext, high, isMetric) + "/" + Utility.formatTemperature(mContext, low, isMetric);
    return highLowStr;
  }

  /*
      This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
      string.
   */
  private String convertCursorRowToUXFormat(Cursor cursor) {
    // get row indices for our cursor
    String highAndLow = formatHighLows(
            cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
            cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

    return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
            " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
            " - " + highAndLow;
  }

  /*
      Remember that these views are reused as needed.
   */
  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
      int viewType = getItemViewType(cursor.getPosition());
      int layoutId = (viewType == VIEW_TYPE_TODAY) ? R.layout.list_item_forecast_today : R.layout.list_item_forecast;
      View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

      viewHolder = new WeatherViewHolder();
      viewHolder.date = (TextView) view.findViewById(R.id.list_item_date_textview);
      viewHolder.maxTemp = (TextView) view.findViewById(R.id.list_item_high_textview);
      viewHolder.minTemp = (TextView) view.findViewById(R.id.list_item_low_textview);
      viewHolder.forecast = (TextView) view.findViewById(R.id.list_item_forecast_textview);
      viewHolder.icon = (ImageView) view.findViewById(R.id.list_item_icon);

    return view;
  }

  /*
      This is where we fill-in the views with the contents of the cursor.
   */
  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    // our view is pretty simple here --- just a text view
    // we'll keep the UI functional with a simple (and slow!) binding.

      //Read image
      int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
      viewHolder.icon.setImageResource(R.drawable.ic_launcher);

      //Read date
      long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
      viewHolder.date.setText(Utility.formatDate(dateInMillis));

      //Read description
      String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
      viewHolder.forecast.setText(description);

      boolean isMetric = Utility.isMetric(context);

      //Red max
      double max = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
      viewHolder.maxTemp.setText(Utility.formatTemperature(context, max, isMetric));

      //Red min
      double min = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
      viewHolder.minTemp.setText(Utility.formatTemperature(context, min, isMetric));
  }

    static class WeatherViewHolder {
        TextView forecast;
        TextView maxTemp;
        TextView minTemp;
        TextView date;
        ImageView icon;
    }
}