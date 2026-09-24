package ipca.api.relay.domain.repository

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken

interface PlacesRepository {
    suspend fun getAutocompletePredictions(
        query: String,
        token: AutocompleteSessionToken
    ): List<AutocompletePrediction>
}