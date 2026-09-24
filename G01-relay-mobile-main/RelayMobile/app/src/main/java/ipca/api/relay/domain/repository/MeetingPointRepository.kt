package ipca.api.relay.domain.repository

import ipca.api.relay.data.remote.dto.meetingpoint.MeetingPointDto

interface MeetingPointRepository {

    suspend fun saveMeetingPoint(
        dto: MeetingPointDto
    ): Result<Unit>
}