package ipca.api.relay.data.remote.model

import ipca.api.relay.domain.model.RemoteCategory

data class CategoryDto(
    val id: Int,
    val name: String,
    val description: String?
)

fun CategoryDto.toDomain(): RemoteCategory =
    RemoteCategory(id = id, name = name, description = description)