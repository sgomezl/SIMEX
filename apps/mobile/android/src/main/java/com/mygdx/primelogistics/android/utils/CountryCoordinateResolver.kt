package com.mygdx.primelogistics.android.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.maps.model.LatLng
import java.text.Normalizer
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

enum class MaritimeZone {
    EUROPE_ATLANTIC,
    MEDITERRANEAN,
    CARIBBEAN,
    NORTH_AMERICA_ATLANTIC,
    GULF_OF_MEXICO,
    SOUTH_AMERICA_ATLANTIC,
    WEST_COAST_AMERICA,
    EAST_ASIA,
    SOUTH_EAST_ASIA,
    SOUTH_ASIA,
    MIDDLE_EAST,
    AFRICA_ATLANTIC,
    UNKNOWN
}

data class MaritimeLocation(
    val displayName: String,
    val latLng: LatLng,
    val zone: MaritimeZone
)

object CountryCoordinateResolver {
    private val fallbackLocations = mapOf(
        "argentina" to MaritimeLocation("Buenos Aires", LatLng(-34.6037, -58.3816), MaritimeZone.SOUTH_AMERICA_ATLANTIC),
        "belgica" to MaritimeLocation("Antwerp", LatLng(51.2194, 4.4025), MaritimeZone.EUROPE_ATLANTIC),
        "belgium" to MaritimeLocation("Antwerp", LatLng(51.2194, 4.4025), MaritimeZone.EUROPE_ATLANTIC),
        "bolivia" to MaritimeLocation("Arica corridor", LatLng(-18.4783, -70.3126), MaritimeZone.WEST_COAST_AMERICA),
        "brasil" to MaritimeLocation("Santos", LatLng(-23.9608, -46.3336), MaritimeZone.SOUTH_AMERICA_ATLANTIC),
        "brazil" to MaritimeLocation("Santos", LatLng(-23.9608, -46.3336), MaritimeZone.SOUTH_AMERICA_ATLANTIC),
        "canada" to MaritimeLocation("Halifax", LatLng(44.6488, -63.5752), MaritimeZone.NORTH_AMERICA_ATLANTIC),
        "chile" to MaritimeLocation("Valparaiso", LatLng(-33.0472, -71.6127), MaritimeZone.WEST_COAST_AMERICA),
        "china" to MaritimeLocation("Shanghai", LatLng(31.2304, 121.4737), MaritimeZone.EAST_ASIA),
        "colombia" to MaritimeLocation("Cartagena", LatLng(10.3910, -75.4794), MaritimeZone.CARIBBEAN),
        "corea del sur" to MaritimeLocation("Busan", LatLng(35.1796, 129.0756), MaritimeZone.EAST_ASIA),
        "costa rica" to MaritimeLocation("Limon", LatLng(9.9907, -83.0360), MaritimeZone.CARIBBEAN),
        "cuba" to MaritimeLocation("Havana", LatLng(23.1136, -82.3666), MaritimeZone.CARIBBEAN),
        "dominican republic" to MaritimeLocation("Caucedo", LatLng(18.4286, -69.6689), MaritimeZone.CARIBBEAN),
        "rep dominicana" to MaritimeLocation("Caucedo", LatLng(18.4286, -69.6689), MaritimeZone.CARIBBEAN),
        "republica dominicana" to MaritimeLocation("Caucedo", LatLng(18.4286, -69.6689), MaritimeZone.CARIBBEAN),
        "republica dominicana " to MaritimeLocation("Caucedo", LatLng(18.4286, -69.6689), MaritimeZone.CARIBBEAN),
        "republica dominicana rep" to MaritimeLocation("Caucedo", LatLng(18.4286, -69.6689), MaritimeZone.CARIBBEAN),
        "ecuador" to MaritimeLocation("Guayaquil", LatLng(-2.1709, -79.9224), MaritimeZone.WEST_COAST_AMERICA),
        "egipto" to MaritimeLocation("Port Said", LatLng(31.2653, 32.3019), MaritimeZone.MEDITERRANEAN),
        "egypt" to MaritimeLocation("Port Said", LatLng(31.2653, 32.3019), MaritimeZone.MEDITERRANEAN),
        "el salvador" to MaritimeLocation("Acajutla", LatLng(13.5928, -89.8276), MaritimeZone.WEST_COAST_AMERICA),
        "espana" to MaritimeLocation("Algeciras", LatLng(36.1408, -5.4562), MaritimeZone.MEDITERRANEAN),
        "spain" to MaritimeLocation("Algeciras", LatLng(36.1408, -5.4562), MaritimeZone.MEDITERRANEAN),
        "espana mainland" to MaritimeLocation("Algeciras", LatLng(36.1408, -5.4562), MaritimeZone.MEDITERRANEAN),
        "estados unidos" to MaritimeLocation("New York", LatLng(40.7128, -74.0060), MaritimeZone.NORTH_AMERICA_ATLANTIC),
        "ee uu" to MaritimeLocation("New York", LatLng(40.7128, -74.0060), MaritimeZone.NORTH_AMERICA_ATLANTIC),
        "usa" to MaritimeLocation("New York", LatLng(40.7128, -74.0060), MaritimeZone.NORTH_AMERICA_ATLANTIC),
        "france" to MaritimeLocation("Le Havre", LatLng(49.4944, 0.1079), MaritimeZone.EUROPE_ATLANTIC),
        "francia" to MaritimeLocation("Le Havre", LatLng(49.4944, 0.1079), MaritimeZone.EUROPE_ATLANTIC),
        "germany" to MaritimeLocation("Hamburg", LatLng(53.5511, 9.9937), MaritimeZone.EUROPE_ATLANTIC),
        "grecia" to MaritimeLocation("Piraeus", LatLng(37.9420, 23.6465), MaritimeZone.MEDITERRANEAN),
        "greece" to MaritimeLocation("Piraeus", LatLng(37.9420, 23.6465), MaritimeZone.MEDITERRANEAN),
        "guatemala" to MaritimeLocation("Puerto Barrios", LatLng(15.7278, -88.5944), MaritimeZone.CARIBBEAN),
        "honduras" to MaritimeLocation("Puerto Cortes", LatLng(15.8256, -87.9494), MaritimeZone.CARIBBEAN),
        "india" to MaritimeLocation("Mumbai", LatLng(19.0760, 72.8777), MaritimeZone.SOUTH_ASIA),
        "italia" to MaritimeLocation("Genoa", LatLng(44.4056, 8.9463), MaritimeZone.MEDITERRANEAN),
        "italy" to MaritimeLocation("Genoa", LatLng(44.4056, 8.9463), MaritimeZone.MEDITERRANEAN),
        "japan" to MaritimeLocation("Tokyo", LatLng(35.6762, 139.6503), MaritimeZone.EAST_ASIA),
        "japon" to MaritimeLocation("Tokyo", LatLng(35.6762, 139.6503), MaritimeZone.EAST_ASIA),
        "mexico" to MaritimeLocation("Veracruz", LatLng(19.1738, -96.1342), MaritimeZone.GULF_OF_MEXICO),
        "marruecos" to MaritimeLocation("Tangier Med", LatLng(35.8888, -5.5033), MaritimeZone.MEDITERRANEAN),
        "morocco" to MaritimeLocation("Tangier Med", LatLng(35.8888, -5.5033), MaritimeZone.MEDITERRANEAN),
        "netherlands" to MaritimeLocation("Rotterdam", LatLng(51.9244, 4.4777), MaritimeZone.EUROPE_ATLANTIC),
        "nicaragua" to MaritimeLocation("Corinto", LatLng(12.4825, -87.1741), MaritimeZone.WEST_COAST_AMERICA),
        "paises bajos" to MaritimeLocation("Rotterdam", LatLng(51.9244, 4.4777), MaritimeZone.EUROPE_ATLANTIC),
        "panama" to MaritimeLocation("Colon", LatLng(9.3545, -79.9000), MaritimeZone.CARIBBEAN),
        "paraguay" to MaritimeLocation("Buenos Aires corridor", LatLng(-34.6037, -58.3816), MaritimeZone.SOUTH_AMERICA_ATLANTIC),
        "peru" to MaritimeLocation("Callao", LatLng(-12.0464, -77.0428), MaritimeZone.WEST_COAST_AMERICA),
        "portugal" to MaritimeLocation("Lisbon", LatLng(38.7223, -9.1393), MaritimeZone.EUROPE_ATLANTIC),
        "puerto rico" to MaritimeLocation("San Juan", LatLng(18.4655, -66.1057), MaritimeZone.CARIBBEAN),
        "reino unido" to MaritimeLocation("Felixstowe", LatLng(51.9634, 1.3511), MaritimeZone.EUROPE_ATLANTIC),
        "republica dominicana" to MaritimeLocation("Caucedo", LatLng(18.4286, -69.6689), MaritimeZone.CARIBBEAN),
        "singapur" to MaritimeLocation("Singapore", LatLng(1.2903, 103.8519), MaritimeZone.SOUTH_EAST_ASIA),
        "singapore" to MaritimeLocation("Singapore", LatLng(1.2903, 103.8519), MaritimeZone.SOUTH_EAST_ASIA),
        "south korea" to MaritimeLocation("Busan", LatLng(35.1796, 129.0756), MaritimeZone.EAST_ASIA),
        "tailandia" to MaritimeLocation("Laem Chabang", LatLng(13.0827, 100.8830), MaritimeZone.SOUTH_EAST_ASIA),
        "thailand" to MaritimeLocation("Laem Chabang", LatLng(13.0827, 100.8830), MaritimeZone.SOUTH_EAST_ASIA),
        "turkey" to MaritimeLocation("Istanbul", LatLng(41.0082, 28.9784), MaritimeZone.MEDITERRANEAN),
        "turquia" to MaritimeLocation("Istanbul", LatLng(41.0082, 28.9784), MaritimeZone.MEDITERRANEAN),
        "united kingdom" to MaritimeLocation("Felixstowe", LatLng(51.9634, 1.3511), MaritimeZone.EUROPE_ATLANTIC),
        "united states" to MaritimeLocation("New York", LatLng(40.7128, -74.0060), MaritimeZone.NORTH_AMERICA_ATLANTIC),
        "uruguay" to MaritimeLocation("Montevideo", LatLng(-34.9011, -56.1645), MaritimeZone.SOUTH_AMERICA_ATLANTIC),
        "venezuela" to MaritimeLocation("La Guaira", LatLng(10.5990, -66.9340), MaritimeZone.CARIBBEAN),
        "vietnam" to MaritimeLocation("Ho Chi Minh City", LatLng(10.8231, 106.6297), MaritimeZone.SOUTH_EAST_ASIA)
    )

    suspend fun resolveLocation(context: Context, countryName: String): MaritimeLocation? {
        if (countryName.isBlank()) {
            return null
        }

        fallbackLocations[normalize(countryName)]?.let { return it }
        return geocode(context, countryName)
    }

    suspend fun resolve(context: Context, countryName: String): LatLng? {
        return resolveLocation(context, countryName)?.latLng
    }

    private suspend fun geocode(context: Context, countryName: String): MaritimeLocation? {
        if (!Geocoder.isPresent()) {
            return null
        }

        val latLng = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                Geocoder(context, Locale.getDefault()).getFromLocationName(
                    countryName,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            continuation.resume(addresses.firstOrNull()?.toLatLng())
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resume(null)
                        }
                    }
                )
            }
        } else {
            @Suppress("DEPRECATION")
            Geocoder(context, Locale.getDefault())
                .getFromLocationName(countryName, 1)
                ?.firstOrNull()
                ?.toLatLng()
        }

        return latLng?.let {
            MaritimeLocation(countryName, it, MaritimeZone.UNKNOWN)
        }
    }

    fun normalizeCountryName(value: String): String {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase(Locale.ROOT)
            .replace("[^a-z\\s]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun Address.toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }

    private fun normalize(value: String): String {
        return normalizeCountryName(value)
    }
}
