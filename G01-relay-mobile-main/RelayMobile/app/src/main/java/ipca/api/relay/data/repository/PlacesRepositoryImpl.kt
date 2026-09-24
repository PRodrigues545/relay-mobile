package ipca.api.relay.data.repository

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import ipca.api.relay.domain.repository.PlacesRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PlacesRepositoryImpl(
    val client: PlacesClient
) : PlacesRepository {

    override suspend fun getAutocompletePredictions(
        query: String,
        token: AutocompleteSessionToken
    ): List<AutocompletePrediction> = suspendCancellableCoroutine { cont ->
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .build()

        client.findAutocompletePredictions(request)
            .addOnSuccessListener { cont.resume(it.autocompletePredictions) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }
}