package br.com.simplepass.cadevan.adapters;

import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import br.com.simplepass.cadevan.R;

/**
 * Created by leandro on 12/3/15.
 */
public class MapInfoAdapter implements GoogleMap.InfoWindowAdapter {
    private View mContentsView;

    public MapInfoAdapter(View mContentsView) {
        this.mContentsView = mContentsView;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView titleTextView = (TextView) mContentsView.findViewById(R.id.title);
        TextView snippetTextView = (TextView) mContentsView.findViewById(R.id.snippet);

        String snippet = marker.getSnippet();

        titleTextView.setText(marker.getTitle());
        snippetTextView.setText(snippet);

        return mContentsView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
