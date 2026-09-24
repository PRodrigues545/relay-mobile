package ipca.api.relay.data.remote.model

import com.google.gson.annotations.SerializedName

data class CreateResourceRequest(
    val title: String,
    val description: String,
    val price: Double?,
    val type: String,
    val condition: String?,

    // Alterado de "category_id" para "categoryId" para bater com o teu Controller.js
    @SerializedName("categoryId")
    val categoryId: Int,

    @SerializedName("borrowDuration")
    val borrowDuration: String?,

    val zones: String?
)