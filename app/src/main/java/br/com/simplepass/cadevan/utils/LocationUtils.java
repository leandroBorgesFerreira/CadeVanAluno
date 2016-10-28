package br.com.simplepass.cadevan.utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Essa classe serve para retornar a localização atual. Ela é usada para criar uma geofence no
 * lugar em que a pessoa está com o smartphone. Assim, essa classe tem o método getLastLocation
 * para seja retornado o lugar atual e então a geofence seja criada.
 */
public class LocationUtils {
    public static LatLng getDefaultLatLng(){ return new LatLng(-19.673067, -43.9391129); }

    public static int getDefaultZoom(){
        return 12;
    }
}
