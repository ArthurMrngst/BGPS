package com.example.bgps.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

// Bản đồ OpenStreetMap (osmdroid) dùng chung cho màn chi tiết hành trình và
// vị trí giao dịch — miễn phí hoàn toàn, không cần API key/thẻ thanh toán
// như Google Maps.
//
// Đã thử lần lượt và đều bị chặn — không phải lỗi cấu hình phía mình:
//   - TileSourceFactory.MAPNIK (tile.openstreetmap.org): 403 "Access
//     blocked" theo chính sách tile usage của OSM (osm.wiki/Blocked).
//   - CARTO (basemaps.cartocdn.com): giờ bắt buộc API key
//     (carto.com/basemaps/apikey).
//   - Wikimedia Maps (maps.wikimedia.org): 403 "restricted to Wikimedia
//     and affiliated sites only".
// Dùng mirror OpenStreetMap France thay thế — đã test trực tiếp từ máy
// (curl trả 200), miễn phí, không cần API key, style render giống OSM gốc
// (đường, toà nhà, nhãn địa điểm đầy đủ, không phải bản đồ địa hình).
private val OSM_FRANCE = XYTileSource(
    "OsmFrance",
    0, 20, 256, ".png",
    arrayOf(
        "https://a.tile.openstreetmap.fr/osmfr/",
        "https://b.tile.openstreetmap.fr/osmfr/",
        "https://c.tile.openstreetmap.fr/osmfr/"
    ),
    "© OpenStreetMap France contributors"
)

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    center: GeoPoint,
    zoom: Double = 16.0,
    routePoints: List<GeoPoint> = emptyList(),
    markers: List<Pair<GeoPoint, String>> = emptyList(),
    // true = tự dời camera theo center mỗi lần cập nhật (màn bản đồ trực
    // tiếp, cần đuổi theo vị trí di chuyển). Mặc định false: chỉ đặt vị trí
    // ban đầu lúc tạo map — nếu set lại mỗi lần recompose thì sẽ liên tục đè
    // mất thao tác zoom/pan bằng tay của người dùng (lỗi thật đã gặp ở màn
    // sửa giao dịch: cảm giác "không zoom được", "điều khiển không tốt").
    followCamera: Boolean = false
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(OSM_FRANCE)
                setMultiTouchControls(true)
                controller.setZoom(zoom)
                controller.setCenter(center)
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
            if (followCamera) {
                mapView.controller.setZoom(zoom)
                mapView.controller.setCenter(center)
            }
            mapView.invalidate()
        }
    )
}
