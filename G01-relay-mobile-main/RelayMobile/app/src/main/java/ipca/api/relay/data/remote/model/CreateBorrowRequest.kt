package ipca.api.relay.data.remote.model

import com.google.gson.annotations.SerializedName

data class CreateBorrowRequest(
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String
)