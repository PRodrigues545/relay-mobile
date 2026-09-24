package ipca.api.relay.domain.repository

import android.net.Uri
import ipca.api.relay.domain.model.RemoteCategory
import ipca.api.relay.domain.model.Resource

interface ResourceRepository {

    suspend fun getResources(type: String? = null, category: String? = null): List<Resource>
    suspend fun getResource(id: Int): Resource?

    /**
     * Reads a single resource from the local Room cache without touching the
     * network. Returns null if nothing's cached for [id].
     *
     * Used by ProductScreen to paint an immediate first frame (correct title,
     * price, image, and crucially the right bottom bar) while a fresh fetch
     * runs in the background. Cache reads are sub-frame (5–15ms typical),
     * which is fast enough to feel like no load at all.
     */
    suspend fun getCachedResource(id: Int): Resource?

    suspend fun addResource(
        title: String,
        description: String,
        type: String,
        condition: String?,
        price: Double?,
        categoryId: Int,
        borrowDuration: String?,
        zones: String?,
        imageUri: Uri?
    ): Resource

    suspend fun updateResource(
        id: Int,
        title: String,
        description: String,
        type: String,
        condition: String?,
        price: Double?,
        categoryId: Int,
        borrowDuration: String?,
        zones: String?,
        imageUri: Uri?
    ): Resource

    suspend fun deleteResource(id: Int)
    suspend fun getResourcesByOwner(ownerId: Int): List<Resource>

    suspend fun getCategories(): Result<List<RemoteCategory>>
}