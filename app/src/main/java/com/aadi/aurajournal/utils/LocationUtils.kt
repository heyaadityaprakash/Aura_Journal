package com.aadi.aurajournal.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Looper
import com.aadi.aurajournal.BuildConfig
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

// A simple data class to hold everything
data class LocationDetails(val name: String, val lat: Double, val lng: Double)


const val GEOAPIFY_API_KEY = BuildConfig.GEOAPIFY_API_KEY


@SuppressLint("MissingPermission")
// core func to get user's gps location
fun fetchCurrentLocation(
    context: Context,
    onLocationFetched: (LocationDetails) -> Unit,
    onError: (String) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val addressName = getAddressFromLocation(context, location.latitude, location.longitude)
            onLocationFetched(LocationDetails(addressName, location.latitude, location.longitude))
        } else {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {
                        fusedLocationClient.removeLocationUpdates(this)
                        val newLocation = p0.lastLocation
                        if (newLocation != null) {
                            val addressName = getAddressFromLocation(context, newLocation.latitude, newLocation.longitude)
                            onLocationFetched(LocationDetails(addressName, newLocation.latitude, newLocation.longitude))
                        } else {
                            onError("Could not determine location")
                        }
                    }
                },
                Looper.getMainLooper()
            )
        }
    }.addOnFailureListener {
        onError(it.localizedMessage ?: "Unknown error")
    }
}

//convert coordinates to string

private fun getAddressFromLocation(context: Context, lat: Double, lng: Double): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lng, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            address.featureName ?: address.locality ?: address.subAdminArea ?: "Selected Location"
        } else {
            "Selected Location"
        }
    } catch (e: Exception) {
        "Selected Location"
    }
}

/**
 * Searches for locations using GeoApify Autocomplete API
 */
suspend fun searchLocations(query: String): List<LocationDetails> = withContext(Dispatchers.IO) {
    if (query.isBlank()) return@withContext emptyList()
    try {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val urlString = "https://api.geoapify.com/v1/geocode/autocomplete?text=$encodedQuery&apiKey=$GEOAPIFY_API_KEY"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(response)
        val features = json.optJSONArray("features") ?: return@withContext emptyList()
        
        val results = mutableListOf<LocationDetails>()
        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val properties = feature.getJSONObject("properties")
            val lat = properties.optDouble("lat", 0.0)
            val lon = properties.optDouble("lon", 0.0)
            val name = properties.optString("formatted", "Unknown Location")
            results.add(LocationDetails(name, lat, lon))
        }
        results
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

/**
 * Generates a GeoApify Static Map URL for a given location
 */
fun getStaticMapUrl(lat: Double, lon: Double): String {
    return "https://maps.geoapify.com/v1/staticmap?" +
            "style=osm-bright-smooth" +
            "&width=600&height=400" +
            "&center=lonlat:$lon,$lat" +
            "&zoom=14" +
            "&marker=lonlat:$lon,$lat;color:%23ff4444;size:medium" +
            "&apiKey=$GEOAPIFY_API_KEY"
}
