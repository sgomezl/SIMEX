package com.mygdx.primelogistics.android.utils

import com.google.android.gms.maps.model.LatLng

object MaritimeRoutePlanner {
    private val gibraltar = LatLng(35.95, -5.61)
    private val iberianAtlantic = LatLng(35.8, -9.8)
    private val canaryApproach = LatLng(28.0, -15.5)
    private val atlanticNorth = LatLng(34.5, -38.0)
    private val atlanticTropical = LatLng(21.0, -43.0)
    private val atlanticSouth = LatLng(-8.0, -28.0)
    private val caribbeanApproach = LatLng(18.8, -61.5)
    private val suez = LatLng(30.70, 32.35)
    private val redSea = LatLng(20.0, 38.5)
    private val arabianSea = LatLng(14.0, 58.0)
    private val malacca = LatLng(2.5, 102.5)
    private val southChinaSea = LatLng(12.0, 114.0)
    private val eastChinaSea = LatLng(28.0, 124.0)
    private val panamaCaribbean = LatLng(9.40, -79.90)
    private val panamaPacific = LatLng(8.95, -79.55)

    fun buildRoute(origin: MaritimeLocation, destination: MaritimeLocation): List<LatLng> {
        val route = mutableListOf(origin.latLng)

        when {
            crossesPanama(origin.zone, destination.zone) -> appendPanamaCorridor(route, origin.zone)
            crossesSuez(origin.zone, destination.zone) -> appendSuezCorridor(route, origin.zone, destination.zone)
            crossesAtlantic(origin.zone, destination.zone) -> appendAtlanticCorridor(route, origin, destination)
            crossesMediterraneanToAtlantic(origin.zone, destination.zone) -> route.add(gibraltar)
        }

        route.add(destination.latLng)
        return collapseDuplicates(route)
    }

    private fun appendPanamaCorridor(route: MutableList<LatLng>, originZone: MaritimeZone) {
        if (isPacific(originZone)) {
            route.add(panamaPacific)
            route.add(panamaCaribbean)
        } else {
            route.add(panamaCaribbean)
            route.add(panamaPacific)
        }
    }

    private fun appendSuezCorridor(
        route: MutableList<LatLng>,
        originZone: MaritimeZone,
        destinationZone: MaritimeZone
    ) {
        val travelingFromEast = isEastOfSuez(originZone)

        if (travelingFromEast) {
            if (originZone == MaritimeZone.EAST_ASIA) {
                route.add(eastChinaSea)
                route.add(southChinaSea)
            } else if (originZone == MaritimeZone.SOUTH_EAST_ASIA) {
                route.add(southChinaSea)
            }

            if (originZone == MaritimeZone.EAST_ASIA || originZone == MaritimeZone.SOUTH_EAST_ASIA) {
                route.add(malacca)
            }

            route.add(arabianSea)
            route.add(redSea)
            route.add(suez)

            if (isAtlantic(destinationZone)) {
                route.add(gibraltar)
                route.add(selectAtlanticWaypoint(route.first(), route.last()))
            }
        } else {
            if (isAtlantic(originZone)) {
                route.add(selectAtlanticWaypoint(route.first(), route.last()))
                route.add(gibraltar)
            }

            route.add(suez)
            route.add(redSea)
            route.add(arabianSea)

            if (destinationZone == MaritimeZone.EAST_ASIA || destinationZone == MaritimeZone.SOUTH_EAST_ASIA) {
                route.add(malacca)
            }

            if (destinationZone == MaritimeZone.SOUTH_EAST_ASIA) {
                route.add(southChinaSea)
            } else if (destinationZone == MaritimeZone.EAST_ASIA) {
                route.add(southChinaSea)
                route.add(eastChinaSea)
            }
        }
    }

    private fun appendAtlanticCorridor(
        route: MutableList<LatLng>,
        origin: MaritimeLocation,
        destination: MaritimeLocation
    ) {
        val originZone = origin.zone
        val destinationZone = destination.zone
        val travelingWest = origin.latLng.longitude > destination.latLng.longitude

        if (travelingWest) {
            if (originZone == MaritimeZone.MEDITERRANEAN) {
                route.add(gibraltar)
            } else if (originZone == MaritimeZone.EUROPE_ATLANTIC) {
                route.add(iberianAtlantic)
            }

            if (originZone == MaritimeZone.MEDITERRANEAN || originZone == MaritimeZone.EUROPE_ATLANTIC) {
                route.add(canaryApproach)
            }

            route.add(selectAtlanticWaypoint(origin.latLng, destination.latLng))

            if (destinationZone == MaritimeZone.CARIBBEAN || destinationZone == MaritimeZone.GULF_OF_MEXICO) {
                route.add(caribbeanApproach)
            }
        } else {
            if (originZone == MaritimeZone.CARIBBEAN || originZone == MaritimeZone.GULF_OF_MEXICO) {
                route.add(caribbeanApproach)
            }

            route.add(selectAtlanticWaypoint(origin.latLng, destination.latLng))

            if (destinationZone == MaritimeZone.MEDITERRANEAN || destinationZone == MaritimeZone.EUROPE_ATLANTIC) {
                route.add(canaryApproach)
            }

            if (destinationZone == MaritimeZone.MEDITERRANEAN) {
                route.add(gibraltar)
            } else if (destinationZone == MaritimeZone.EUROPE_ATLANTIC) {
                route.add(iberianAtlantic)
            }
        }
    }

    private fun crossesPanama(originZone: MaritimeZone, destinationZone: MaritimeZone): Boolean {
        return (isPacific(originZone) && isAtlantic(destinationZone)) ||
            (isAtlantic(originZone) && isPacific(destinationZone))
    }

    private fun crossesSuez(originZone: MaritimeZone, destinationZone: MaritimeZone): Boolean {
        return (isEastOfSuez(originZone) && isWestOfSuez(destinationZone)) ||
            (isWestOfSuez(originZone) && isEastOfSuez(destinationZone))
    }

    private fun crossesAtlantic(originZone: MaritimeZone, destinationZone: MaritimeZone): Boolean {
        return isAtlantic(originZone) && isAtlantic(destinationZone) && originZone != destinationZone
    }

    private fun crossesMediterraneanToAtlantic(originZone: MaritimeZone, destinationZone: MaritimeZone): Boolean {
        return (originZone == MaritimeZone.MEDITERRANEAN && destinationZone == MaritimeZone.EUROPE_ATLANTIC) ||
            (originZone == MaritimeZone.EUROPE_ATLANTIC && destinationZone == MaritimeZone.MEDITERRANEAN)
    }

    private fun isPacific(zone: MaritimeZone): Boolean {
        return zone == MaritimeZone.WEST_COAST_AMERICA ||
            zone == MaritimeZone.EAST_ASIA ||
            zone == MaritimeZone.SOUTH_EAST_ASIA
    }

    private fun isAtlantic(zone: MaritimeZone): Boolean {
        return zone == MaritimeZone.EUROPE_ATLANTIC ||
            zone == MaritimeZone.MEDITERRANEAN ||
            zone == MaritimeZone.CARIBBEAN ||
            zone == MaritimeZone.NORTH_AMERICA_ATLANTIC ||
            zone == MaritimeZone.GULF_OF_MEXICO ||
            zone == MaritimeZone.SOUTH_AMERICA_ATLANTIC ||
            zone == MaritimeZone.AFRICA_ATLANTIC
    }

    private fun isEastOfSuez(zone: MaritimeZone): Boolean {
        return zone == MaritimeZone.EAST_ASIA ||
            zone == MaritimeZone.SOUTH_EAST_ASIA ||
            zone == MaritimeZone.SOUTH_ASIA ||
            zone == MaritimeZone.MIDDLE_EAST
    }

    private fun isWestOfSuez(zone: MaritimeZone): Boolean {
        return isAtlantic(zone) || zone == MaritimeZone.MEDITERRANEAN
    }

    private fun selectAtlanticWaypoint(origin: LatLng, destination: LatLng): LatLng {
        val averageLatitude = (origin.latitude + destination.latitude) / 2.0
        return when {
            averageLatitude > 32.0 -> atlanticNorth
            averageLatitude > 5.0 -> atlanticTropical
            else -> atlanticSouth
        }
    }

    private fun collapseDuplicates(points: List<LatLng>): List<LatLng> {
        return points.fold(mutableListOf()) { acc, point ->
            if (acc.isEmpty() || !isSamePoint(acc.last(), point)) {
                acc.add(point)
            }
            acc
        }
    }

    private fun isSamePoint(a: LatLng, b: LatLng): Boolean {
        val latDiff = kotlin.math.abs(a.latitude - b.latitude)
        val lngDiff = kotlin.math.abs(a.longitude - b.longitude)
        return latDiff < 0.01 && lngDiff < 0.01
    }
}
