package net.tbennett.mysunshine;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import net.tbennett.mysunshine.data.WeatherContract.WeatherEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 0;
    private ShareActionProvider mShareActionProvider;
    private String forecast;
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private DetailViewHolder viewHolder;

    private static final String[] DETAIL_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_DEGREES,
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_WIND = 6;
    private static final int COL_WEATHER_PRESSURE = 7;
    private static final int COL_WEATHER_DEGREES = 8;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewHolder = new DetailViewHolder();
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        viewHolder.day = (TextView)rootView.findViewById(R.id.detail_day_text);
        viewHolder.date = (TextView)rootView.findViewById(R.id.detail_date_text);
        viewHolder.maxTemp = (TextView)rootView.findViewById(R.id.detail_high_text);
        viewHolder.minTemp = (TextView)rootView.findViewById(R.id.detail_low_text);
        viewHolder.icon = (ImageView)rootView.findViewById(R.id.detail_icon);
        viewHolder.desc = (TextView)rootView.findViewById(R.id.detail_desc_text);
        viewHolder.humidity = (TextView)rootView.findViewById(R.id.detail_humidity_text);
        viewHolder.windspeed = (TextView)rootView.findViewById(R.id.detail_wind_text);
        viewHolder.pressure = (TextView)rootView.findViewById(R.id.detail_pressure_text);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Locate menu item with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        //Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if(forecast != null) {
            mShareActionProvider.setShareIntent(setupShareIntent());
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    private Intent setupShareIntent(){

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        String shareString = getString(R.string.share_pre_text) + "\n" + forecast + " " + getString(R.string.share_append_text);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareString);
        sendIntent.setType("text/plain");
        setShareIntent(sendIntent);
        return sendIntent;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        Intent intent = getActivity().getIntent();
        if(intent == null){
            return null;
        }

        return new CursorLoader(getActivity(),
                intent.getData(),
                DETAIL_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");if (!data.moveToFirst()) { return; }
        long rawDate = data.getLong(COL_WEATHER_DATE);

        String friendlyDateText = Utility.getDayName(getActivity(), rawDate);
        String dateText = Utility.getFormattedMonthDay(getActivity(), rawDate);

        String weatherDescription = data.getString(COL_WEATHER_DESC);
        float degrees = data.getFloat(COL_WEATHER_DEGREES);
        String wind = Utility.getFormattedWind(getContext(), data.getFloat(COL_WEATHER_WIND), degrees);
        String humidity = getContext().getString(R.string.format_humidity, data.getFloat(COL_WEATHER_HUMIDITY));
        String pressure = getContext().getString(R.string.format_pressure, data.getFloat(COL_WEATHER_PRESSURE));

        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        forecast = String.format("%s - %s - %s/%s", dateText, weatherDescription, high, low);

        viewHolder.date.setText(dateText);
        viewHolder.day.setText(friendlyDateText);
        viewHolder.maxTemp.setText(high);
        viewHolder.minTemp.setText(low);
        viewHolder.desc.setText(weatherDescription);
        viewHolder.windspeed.setText(wind);
        viewHolder.humidity.setText(humidity);
        viewHolder.pressure.setText(pressure);

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(setupShareIntent());
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    static class DetailViewHolder {
        TextView day;
        TextView date;
        TextView maxTemp;
        TextView minTemp;
        ImageView icon;
        TextView desc;
        TextView humidity;
        TextView windspeed;
        TextView pressure;
    }
}
