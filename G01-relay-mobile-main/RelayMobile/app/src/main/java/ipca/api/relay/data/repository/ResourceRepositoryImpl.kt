package ipca.api.relay.data.repository

import android.content.Context
import android.net.Uri
import ipca.api.relay.data.local.dao.ResourceDao
import ipca.api.relay.data.local.entity.ResourceEntity
import ipca.api.relay.data.remote.api.ResourceApi
import ipca.api.relay.data.remote.model.CreateResourceRequest
import ipca.api.relay.data.remote.model.toDomain
import ipca.api.relay.domain.model.RemoteCategory
import ipca.api.relay.domain.model.Resource
import ipca.api.relay.domain.repository.ResourceRepository
import retrofit2.HttpException
import java.io.IOException

class ResourceRepositoryImpl(
    private val api: ResourceApi,
    private val context: Context,
    private val cache: ResourceDao,
) : ResourceRepository {

    override suspend fun getResources(type: String?, category: String?): List<Resource> {
        return try {
            val remote = api.getResources(type, category).map { it.toDomain() }
            if (type == null && category == null) {
                val now = System.currentTimeMillis()
                cache.replaceAll(remote.map { it.toEntity(now) })
            }
            remote
        } catch (e: Exception) {
            if (type == null && category == null) {
                cache.getAll().map { it.toDomain() }.ifEmpty { throw e }
            } else {
                throw e
            }
        }
    }

    override suspend fun getResource(id: Int): Resource? {
        return try {
            val dto = api.getResourceById(id).toDomain()
            // Warm the cache so subsequent visits to this product (and the
            // cache-first path in getCachedResource) get the latest values.
            cache.upsertAll(listOf(dto.toEntity(System.currentTimeMillis())))
            dto
        } catch (e: Exception) {
            cache.getById(id)?.toDomain()
        }
    }

    override suspend fun getCachedResource(id: Int): Resource? {
        // Pure local read. Returns null if the user hasn't seen this resource
        // before in this install (e.g. opened via deep-link or push notification
        // before scrolling Home). Callers should follow up with [getResource]
        // to fetch fresh data.
        return cache.getById(id)?.toDomain()
    }

    override suspend fun addResource(
        title: String,
        description: String,
        type: String,
        condition: String?,
        price: Double?,
        categoryId: Int,
        borrowDuration: String?,
        zones: String?,
        imageUri: Uri?
    ): Resource {
        val request = CreateResourceRequest(
            title = title,
            description = description,
            type = type,
            condition = condition,
            price = price,
            categoryId = categoryId,
            borrowDuration = borrowDuration,
            zones = zones
        )
        val created = api.createResource(request)

        // If the caller supplied an image, attach it in a second hop. Failure
        // here doesn't undo the resource — same behavior as before.
        val finalDto = imageUri?.let { uri ->
            uriToMultipart(context, uri)?.let { part -> api.uploadImage(created.id, part) }
        } ?: created

        val domain = finalDto.toDomain()
        cache.upsertAll(listOf(domain.toEntity(System.currentTimeMillis())))
        return domain
    }

    override suspend fun updateResource(
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
    ): Resource {
        val request = CreateResourceRequest(
            title = title,
            description = description,
            type = type,
            condition = condition,
            price = price,
            categoryId = categoryId,
            borrowDuration = borrowDuration,
            zones = zones
        )
        val updated = api.updateResource(id, request)

        // Same two-hop pattern as create: text fields land first, image follows.
        val finalDto = imageUri?.let { uri ->
            uriToMultipart(context, uri)?.let { part -> api.uploadImage(id, part) }
        } ?: updated

        val domain = finalDto.toDomain()
        cache.upsertAll(listOf(domain.toEntity(System.currentTimeMillis())))
        return domain
    }

    override suspend fun deleteResource(id: Int) {
        api.deleteResource(id)
        cache.deleteById(id)
    }

    override suspend fun getResourcesByOwner(ownerId: Int): List<Resource> {
        return api.filterResources(ownerId = ownerId).map { it.toDomain() }
    }

    override suspend fun getCategories(): Result<List<RemoteCategory>> {
        return try {
            Result.success(
                api.getCategories().map { RemoteCategory(id = it.id, name = it.name, description = it.description) }
            )
        } catch (e: HttpException) {
            Result.failure(AuthException.ServerError(e.code()))
        } catch (e: IOException) {
            Result.failure(AuthException.NetworkError(e))
        }
    }
}

// ── Mappers (private to this file) ──────────────────────────────────────────
private fun Resource.toEntity(now: Long) = ResourceEntity(
    id = id,
    title = title,
    description = description,
    price = price,
    category = category,
    categoryId = categoryId,
    imageUrl = imageUrl,
    isAvailable = isAvailable,
    type = type,
    condition = condition,
    borrowDuration = borrowDuration,
    zones = zones,
    ownerId = ownerId,
    ownerName = ownerName,
    ownerUsername = ownerUsername,
    ownerReputation = ownerReputation,
    ownerPicture = ownerPicture,
    cachedAt = now,
)

private fun ResourceEntity.toDomain() = Resource(
    id = id,
    title = title,
    description = description,
    price = price,
    category = category,
    categoryId = categoryId,
    imageUrl = imageUrl,
    isAvailable = isAvailable,
    type = type,
    condition = condition,
    borrowDuration = borrowDuration,
    zones = zones,
    ownerId = ownerId,
    ownerName = ownerName,
    ownerUsername = ownerUsername,
    ownerReputation = ownerReputation,
    ownerPicture = ownerPicture,
)