package com.brodev.socialapp.android;

import android.util.Log;

import com.brodev.socialapp.entity.PlaceDetails;
import com.brodev.socialapp.entity.PlacesList;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.apache.http.client.HttpResponseException;

@SuppressWarnings("deprecation")
public class GooglePlaces {

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    // Google Places serach url's
    private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
    private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";

    private double _latitude;
    private double _longitude;
    private double _radius;

    /**
     * Searching places
     * @param latitude - latitude of place
     * @params longitude - longitude of place
     * @param radius - radius of searchable area
     * @param types - type of place to search
     * @param API_KEY - Google API key
     * @return list of places
     * */
     public PlacesList search(double latitude, double longitude, double radius, String types, String API_KEY)
            throws Exception {

        this._latitude = latitude;
        this._longitude = longitude;
        this._radius = radius;

        try {
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("location", _latitude + "," + _longitude);
            request.getUrl().put("radius", _radius); // in meters
            request.getUrl().put("sensor", "false");
            if(types != null)
                request.getUrl().put("types", types);

            PlacesList list = request.execute().parseAs(PlacesList.class);
            // Check log cat for places response status
            Log.d("Places Status", "" + list.status);
            return list;

        } catch (HttpResponseException e) {
            Log.e("Error:", e.getMessage());
            return null;
        }

    }

    /**
     * Searching single place full details
     * @param reference - reference id of place
     *                 - which you will get in search api request
     * @param API_KEY - Google API Key
     * */
     public PlaceDetails getPlaceDetails(String reference, String API_KEY) throws Exception {
        try {

            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("reference", reference);
            request.getUrl().put("sensor", "false");

            PlaceDetails place = request.execute().parseAs(PlaceDetails.class);

            return place;

        } catch (HttpResponseException e) {
            Log.e("Error in Perform Details", e.getMessage());
            throw e;
        }
    }

    /**
     * Creating http request Factory
     * */
    public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
        return transport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                HttpHeaders headers = new HttpHeaders();
                headers.setUserAgent("Social_app");
                request.setHeaders(headers);
                JsonObjectParser parser = new JsonObjectParser(new JacksonFactory());
                request.setParser(parser);
            }
        });
    }
}
