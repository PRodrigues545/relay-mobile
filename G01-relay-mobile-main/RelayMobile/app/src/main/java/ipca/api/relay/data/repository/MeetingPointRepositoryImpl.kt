package ipca.api.relay.data.repository

import ipca.api.relay.data.remote.api.MeetingPointApi
import ipca.api.relay.data.remote.dto.meetingpoint.MeetingPointDto
import ipca.api.relay.domain.repository.MeetingPointRepository

class MeetingPointRepositoryImpl(
    private val api: MeetingPointApi
) : MeetingPointRepository {

    override suspend fun saveMeetingPoint(
        dto: MeetingPointDto
    ): Result<Unit> {
        return try {
            val response = api.saveMeetingPoint(dto)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception("Erro ao guardar ponto: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}