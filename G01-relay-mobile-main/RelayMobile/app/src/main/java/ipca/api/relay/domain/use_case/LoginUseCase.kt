package ipca.api.relay.domain.use_case

import ipca.api.relay.domain.model.User
import ipca.api.relay.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
){
    suspend operator fun invoke(email: String, password: String) =
        repository.login(email, password)
}