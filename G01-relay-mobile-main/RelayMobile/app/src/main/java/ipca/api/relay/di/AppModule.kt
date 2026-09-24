package ipca.api.relay.di

import android.content.Context
import com.google.android.libraries.places.api.Places
import ipca.api.relay.BuildConfig
import ipca.api.relay.data.local.AppDatabase
import ipca.api.relay.data.local.ProfileLocalStore
import ipca.api.relay.data.local.SocketManager
import ipca.api.relay.data.local.TokenManager
import ipca.api.relay.data.local.dao.ResourceDao
import ipca.api.relay.data.local.dao.UserDao
import ipca.api.relay.data.remote.api.* // Agrupado para clareza (Auth, Resource, User, Request)
import ipca.api.relay.data.repository.* // Agrupado para clareza (Auth, Resource, User, Places, Request)
import ipca.api.relay.domain.repository.* // Agrupado para clareza (Auth, Resource, User, Places, Request)
import kotlinx.coroutines.channels.Channel
import retrofit2.Retrofit
import ipca.api.relay.data.remote.api.MeetingPointApi
import ipca.api.relay.data.repository.MeetingPointRepositoryImpl
import ipca.api.relay.domain.repository.MeetingPointRepository

object AppModule {
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofit: Retrofit
    private lateinit var database: AppDatabase

    lateinit var profileLocalStore: ProfileLocalStore
        private set

    // DAOs exposed publicly so repository impls can take them in their
    // constructors without reaching into AppDatabase themselves. Keeps
    // the Room wiring isolated to this file.
    lateinit var resourceDao: ResourceDao
        private set
    lateinit var userDao: UserDao
        private set

    lateinit var authApi: AuthApi
        private set
    lateinit var authRepository: AuthRepository
        private set

    lateinit var resourceApi: ResourceApi
        private set
    lateinit var resourceRepository: ResourceRepository
        private set

    lateinit var requestApi: RequestApi
        private set
    lateinit var requestRepository: RequestRepository
        private set

    lateinit var userApi: UserApi
        private set
    lateinit var userRepository: UserRepository
        private set

    lateinit var messagesApi: MessagesApi
        private set
    lateinit var messagesRepository: MessagesRepository
        private set

    lateinit var notificationsApi: NotificationsApi
        private set
    lateinit var notificationRepository: NotificationRepository
        private set

    lateinit var operationApi: OperationApi
        private set
    lateinit var operationRepository: OperationRepository
        private set

    lateinit var reviewApi: ReviewApi
        private set
    lateinit var reviewRepository: ReviewRepository
        private set

    lateinit var placesRepository: PlacesRepository
        private set

    lateinit var meetingPointApi: MeetingPointApi
        private set

    lateinit var meetingPointRepository: MeetingPointRepository
        private set


    val sessionExpiredEvent = Channel<Unit>(Channel.CONFLATED)

    fun init(context: Context, onSessionExpired: () -> Unit) {
        tokenManager = TokenManager(context)
        profileLocalStore = ProfileLocalStore(context)

        database = AppDatabase.get(context)
        resourceDao = database.resourceDao()
        userDao = database.userDao()

        val client = RetrofitInstance.buildClient(
            tokenManager = tokenManager,
            onSessionExpired = {
                sessionExpiredEvent.trySend(Unit)
            }
        )
        retrofit = RetrofitInstance.buildRetrofit(client)

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(
                context,
                BuildConfig.PLACES_API_KEY
            )
        }

        placesRepository = PlacesRepositoryImpl(
            Places.createClient(context)
        )

        // --- APIs ---
        authApi = retrofit.create(AuthApi::class.java)
        resourceApi = retrofit.create(ResourceApi::class.java)
        userApi = retrofit.create(UserApi::class.java)
        requestApi = retrofit.create(RequestApi::class.java)
        messagesApi = retrofit.create(MessagesApi::class.java)
        notificationsApi = retrofit.create(NotificationsApi::class.java)
        operationApi = retrofit.create(OperationApi::class.java)
        reviewApi = retrofit.create(ReviewApi::class.java)
        meetingPointApi = retrofit.create(MeetingPointApi::class.java)

        // --- Repositories ---
        authRepository = AuthRepositoryImpl(authApi, tokenManager)
        resourceRepository = ResourceRepositoryImpl(resourceApi, context, resourceDao)
        userRepository = UserRepositoryImpl(userApi, context, userDao)
        requestRepository = RequestRepositoryImpl(requestApi)
        messagesRepository = MessagesRepositoryImpl(messagesApi)
        notificationRepository = NotificationRepositoryImpl(notificationsApi)
        operationRepository = OperationRepositoryImpl(operationApi, tokenManager)
        reviewRepository = ReviewRepositoryImpl(reviewApi)
        meetingPointRepository = MeetingPointRepositoryImpl(meetingPointApi)
    }

    fun connectSocket() {
        val token = tokenManager.getAccessToken() ?: return
        val socketUrl = BuildConfig.MESSAGING_SOCKET_URL
        SocketManager.connect(socketUrl, token)
    }

    fun disconnectSocket() {
        SocketManager.disconnect()
    }

    fun getTokenManager() = tokenManager

    fun getDatabase() = database
}
