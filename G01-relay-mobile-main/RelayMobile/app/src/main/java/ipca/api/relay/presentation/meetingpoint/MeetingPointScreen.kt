package ipca.api.relay.presentation.meetingpoint

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import ipca.api.relay.data.local.SocketManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt

/**
 * Suspend-wrapper around a Play-services Task so we can use it inside
 * collectLatest. Avoids pulling in kotlinx-coroutines-play-services as a new
 * dependency just for one call site.
 */
private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    if (isComplete) {
        val e = exception
        if (e != null) cont.resumeWithException(e)
        else @Suppress("UNCHECKED_CAST") cont.resume(result as T)
        return@suspendCancellableCoroutine
    }
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
}

private val RelayPurple = Color(0xFF6666FF)
private val RelayDark = Color(0xFF2A2A5E)
private val RelayLight = Color(0xFFF0F0FF)

// Place types relevant for a student marketplace meetup.
private val STUDENT_PLACE_TYPES = listOf(
    "university",
    "cafe",
    "library",
    "train_station",
    "shopping_mall"
)

// Fields the autocomplete result Place lookup needs to populate.
private val PLACE_FETCH_FIELDS = listOf(
    Place.Field.LOCATION,
    Place.Field.DISPLAY_NAME,
    Place.Field.FORMATTED_ADDRESS
)

// Search debounce — typing pauses for this long before a Places API call.
// Places autocomplete is metered and bandwidth-heavy; debouncing turns a
// 10-character word from 10 calls into 1.
private const val SEARCH_DEBOUNCE_MS = 250L
private const val SEARCH_MIN_LEN = 2

// Fixed view of Barcelos campus area for the default camera position.
private val BARCELOS_CENTER = LatLng(41.5317, -8.6184)
private const val DEFAULT_ZOOM = 14f
private const val FOCUSED_ZOOM = 16f

data class MeetingPointUi(
    val id: Int,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Computes the great-circle distance between two coordinates in meters,
 * using Android's built-in Location.distanceBetween (Vincenty / Haversine hybrid).
 */
private fun distanceMeters(from: LatLng, to: LatLng): Float {
    val out = FloatArray(1)
    Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, out)
    return out[0]
}

private fun formatDistance(meters: Float): String {
    return if (meters < 1000f) "${meters.roundToInt()} m"
    else "%.1f km".format(meters / 1000f)
}

@OptIn(FlowPreview::class)
@Composable
fun MeetingPointScreen(
    onBackClick: () -> Unit,
    onOpenDetails: (MeetingPointUi) -> Unit,
    onPicked: ((MeetingPointUi) -> Unit)? = null,
) {
    val context = LocalContext.current

    val placesClient = remember { Places.createClient(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // A single autocomplete session token across keystrokes — Google bills
    // by session, not by request, so reusing the token until a place is
    // picked keeps the cost predictable.
    val sessionToken = remember { AutocompleteSessionToken.newInstance() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPoint by remember { mutableStateOf<MeetingPointUi?>(null) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    // Real meeting points around Barcelos (fixed coordinates).
    val meetingPoints = remember {
        listOf(
            MeetingPointUi(1, "Firefighter Statue", "Av. da Liberdade 79, 4750-312 Barcelos", 41.5329, -8.6151),
            MeetingPointUi(2, "IPCA — Escola Superior de Tecnologia", "Campus do IPCA, Lugar do Aldão", 41.5362, -8.6271),
            MeetingPointUi(3, "Largo da Porta Nova", "Largo da Porta Nova, 4750-329 Barcelos", 41.5310, -8.6178),
            MeetingPointUi(4, "Câmara Municipal de Barcelos", "Largo Dom António Barroso 211, 4750-323 Barcelos", 41.5316, -8.6176),
            MeetingPointUi(5, "Estação Ferroviária de Barcelos", "Largo da Estação, 4750-260 Barcelos", 41.5292, -8.6228),
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(BARCELOS_CENTER, DEFAULT_ZOOM)
    }

    val mapProperties = remember(hasLocationPermission) {
        MapProperties(isMyLocationEnabled = hasLocationPermission)
    }
    val mapUiSettings = remember {
        MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false)
    }

    fun moveToUserLocation() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                userLocation = latLng
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (granted) moveToUserLocation()
    }

    // Auto-request permission on first composition; if already granted, fetch immediately.
    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            moveToUserLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Subscribe to socket updates lifecycle-aware. Crucially, we now detach
    // the listener when the screen leaves composition — otherwise the
    // SocketManager handler would keep firing for a Composable scope that
    // no longer exists, holding a memory reference to it.
    DisposableEffect(Unit) {
        SocketManager.onMeetingPointUpdated { lat, lng ->
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(LatLng(lat, lng), FOCUSED_ZOOM)
        }
        onDispose {
            SocketManager.offMeetingPointUpdated()
        }
    }

    // Debounced search pipeline. searchQuery (the TextField's backing state)
    // updates synchronously for snappy typing, but the Places call only fires
    // once typing settles. Pre-debounce, every keystroke produced one billed
    // Places request.
    val searchFlow = remember { MutableStateFlow("") }
    LaunchedEffect(searchFlow) {
        searchFlow
            .debounce(SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged()
            // collectLatest cancels an in-flight prediction call if a newer
            // keystroke arrives, so stale responses can't clobber fresher ones.
            .collectLatest { query ->
                if (query.length < SEARCH_MIN_LEN) {
                    predictions = emptyList()
                    return@collectLatest
                }

                val builder = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .setTypesFilter(STUDENT_PLACE_TYPES)
                    .setCountries("PT")
                    .setSessionToken(sessionToken)

                // Bias to the user's area when known — much better relevance.
                userLocation?.let { loc ->
                    val delta = 0.2 // ~20 km box around the user
                    builder.setLocationBias(
                        RectangularBounds.newInstance(
                            LatLng(loc.latitude - delta, loc.longitude - delta),
                            LatLng(loc.latitude + delta, loc.longitude + delta)
                        )
                    )
                }

                val result = runCatching { placesClient.findAutocompletePredictions(builder.build()).awaitResult() }
                predictions = result.getOrNull()?.autocompletePredictions ?: emptyList()
            }
    }

    // Sort meeting points by distance from the user (if known).
    val sortedPoints = remember(userLocation, meetingPoints) {
        val user = userLocation
        if (user == null) {
            meetingPoints.map { it to null as Float? }
        } else {
            meetingPoints
                .map { it to distanceMeters(user, LatLng(it.latitude, it.longitude)) }
                .sortedBy { it.second ?: Float.MAX_VALUE }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Spacer(modifier = Modifier.height(20.dp))

        // Top bar.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Meeting Point", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search with type-filtered, debounced autocomplete.
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    // Feed the debounced flow; predictions are cleared in the
                    // debounce collector when the query is too short.
                    searchFlow.value = query
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                placeholder = { Text("Search a café, library, station…") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = RelayDark,
                    unfocusedBorderColor = RelayDark,
                    cursorColor = RelayDark,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray
                )
            )

            predictions.take(5).forEach { prediction ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable {
                        val request = FetchPlaceRequest.builder(prediction.placeId, PLACE_FETCH_FIELDS)
                            // Reuse the same session token as the autocomplete
                            // calls so Google groups them into one billable
                            // session ending with this fetch.
                            .setSessionToken(sessionToken)
                            .build()
                        placesClient.fetchPlace(request).addOnSuccessListener { response ->
                            val place = response.place
                            place.location?.let { latLng ->
                                cameraPositionState.position =
                                    CameraPosition.fromLatLngZoom(latLng, FOCUSED_ZOOM)
                                selectedPoint = MeetingPointUi(
                                    id = -1,
                                    name = place.displayName ?: "Location",
                                    address = place.formattedAddress ?: "",
                                    latitude = latLng.latitude,
                                    longitude = latLng.longitude
                                )
                                searchQuery = place.displayName ?: ""
                                // Stop the debounce loop from re-fetching for
                                // the (now fully-resolved) query string.
                                searchFlow.value = ""
                                predictions = emptyList()
                            }
                        }
                    },
                    tonalElevation = 2.dp
                ) {
                    Text(
                        prediction.getFullText(null).toString(),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = RelayDark
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Map") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("List") })
        }

        if (selectedTab == 0) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = mapUiSettings
                ) {
                    meetingPoints.forEach { point ->
                        // rememberMarkerState gives each marker a stable
                        // MarkerState across recompositions. The previous code
                        // built a fresh MarkerState every recomposition, which
                        // forced the maps SDK to redraw markers and broke any
                        // open info-windows mid-interaction.
                        val markerState = rememberMarkerState(
                            key = "mp-${point.id}",
                            position = LatLng(point.latitude, point.longitude)
                        )
                        Marker(
                            state = markerState,
                            title = point.name,
                            snippet = point.address,
                            onClick = {
                                selectedPoint = point
                                SocketManager.emitMeetingPoint(point.latitude, point.longitude)
                                false
                            }
                        )
                    }

                    // Custom (search-picked) point — show a distinct marker.
                    selectedPoint?.takeIf { it.id == -1 }?.let { custom ->
                        val customMarkerState = rememberMarkerState(
                            key = "mp-custom",
                            position = LatLng(custom.latitude, custom.longitude)
                        )
                        Marker(
                            state = customMarkerState,
                            title = custom.name,
                            snippet = custom.address
                        )
                    }
                }

                FloatingActionButton(
                    onClick = {
                        if (hasLocationPermission) moveToUserLocation()
                        else locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    containerColor = Color.White,
                    contentColor = RelayPurple
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    androidx.compose.animation.AnimatedVisibility(visible = selectedPoint != null) {
                        selectedPoint?.let { point ->
                            val distance = userLocation?.let {
                                distanceMeters(it, LatLng(point.latitude, point.longitude))
                            }
                            SelectedPointCard(
                                point = point,
                                distanceText = distance?.let(::formatDistance),
                                onClick = { onOpenDetails(point) }
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (userLocation == null) {
                    item {
                        Text(
                            "Tap the location button on the map to sort by distance.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                items(sortedPoints, key = { it.first.id }) { (point, distance) ->
                    MeetingPointItem(
                        title = point.name,
                        address = point.address,
                        distanceText = distance?.let(::formatDistance),
                        isNearest = distance != null && sortedPoints.firstOrNull()?.first?.id == point.id,
                        onClick = {
                            selectedPoint = point
                            SocketManager.emitMeetingPoint(point.latitude, point.longitude)
                            cameraPositionState.position =
                                CameraPosition.fromLatLngZoom(LatLng(point.latitude, point.longitude), FOCUSED_ZOOM)
                            selectedTab = 0
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                selectedPoint?.let { point ->
                    if (onPicked != null) {
                        onPicked(point)
                    } else {
                        // Standalone mode — open details. The previous version
                        // called a now-removed `data.remote.saveMeetingPoint`
                        // stub that pointed at a placeholder localhost URL and
                        // had no requestId; that call always failed silently.
                        // The real save happens in the details/confirmation
                        // flow with the full transaction context.
                        // TODO: wire MeetingPointRepository.saveMeetingPoint in
                        //       MeetingPointConfirmationScreen once requestId
                        //       is available there.
                        onOpenDetails(point)
                    }
                }
            },
            enabled = selectedPoint != null,
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RelayPurple,
                disabledContainerColor = Color(0xFFB8B8FF),
                contentColor = Color.White,
                disabledContentColor = Color.White
            )
        ) {
            Text(
                text = if (selectedPoint == null) "Select a location" else "Select this meeting place",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SelectedPointCard(
    point: MeetingPointUi,
    distanceText: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(modifier = modifier.fillMaxWidth(), color = Color.White, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(point.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    distanceText?.let {
                        Spacer(Modifier.width(8.dp))
                        DistanceChip(it)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(point.address, fontSize = 13.sp, color = Color.Gray)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Black)
        }
    }
}

@Composable
fun MeetingPointItem(
    title: String,
    address: String,
    distanceText: String?,
    isNearest: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Place,
                contentDescription = null,
                tint = if (isNearest) RelayPurple else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            if (isNearest) {
                Box(
                    modifier = Modifier
                        .background(RelayPurple, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Nearest", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(8.dp))
            }
            distanceText?.let { DistanceChip(it) }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(address, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(start = 26.dp))
    }
    HorizontalDivider()
}

@Composable
private fun DistanceChip(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, RelayLight, RoundedCornerShape(50))
            .background(RelayLight, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = RelayDark, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}