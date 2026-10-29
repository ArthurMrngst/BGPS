package com.example.bgps.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

// Bản đồ OpenStreetMap (osmdroid) dùng chung cho màn chi tiết hành trình và
// vị trí giao dịch — miễn phí hoàn toàn, không cần API key/thẻ thanh toán
// như Google Maps.
@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    center: GeoPoint,
    zoom: Double = 16.0,
    routePoints: List<GeoPoint> = emptyList(),
    markers: List<Pair<GeoPoint, String>> = emptyList()
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            if (routePoints.size >= 2) {
                mapView.overlays.add(Polyline(mapView).apply { setPoints(routePoints) })
            }
            markers.forEach { (point, label) ->
                mapView.overlays.add(
                    Marker(mapView).apply {
                        position = point
                        title = label
                    }
                )
            }
            mapView.controller.setZoom(zoom)
            mapView.controller.setCenter(center)
            mapView.invalidate()
        }
    )
}
