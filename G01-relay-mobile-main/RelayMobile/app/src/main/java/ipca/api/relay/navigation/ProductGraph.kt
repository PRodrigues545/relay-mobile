package ipca.api.relay.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.model.PaymentMethod
import ipca.api.relay.presentation.borrow.BorrowScreen
import ipca.api.relay.presentation.borrow.BorrowViewModel
import ipca.api.relay.presentation.meetingpoint.MeetingPointConfirmationScreen
import ipca.api.relay.presentation.meetingpoint.MeetingPointDetailsScreen
import ipca.api.relay.presentation.meetingpoint.MeetingPointScreen
import ipca.api.relay.presentation.product.ProductScreen
import ipca.api.relay.presentation.purchase.PaymentMethodPickerScreen
import ipca.api.relay.presentation.purchase.PurchaseScreen
import ipca.api.relay.presentation.transaction.TransactionDetailScreen
import ipca.api.relay.presentation.reviews.ReviewsScreen
import ipca.api.relay.presentation.profile.ReviewTab
import ipca.api.relay.presentation.userprofile.UserProfileScreen
import ipca.api.relay.domain.model.TransactionType
import ipca.api.relay.presentation.create.CreateResourceScreen
import ipca.api.relay.presentation.create.CreateResourceViewModel
import ipca.api.relay.presentation.create.CreateResourceViewModelFactory
import ipca.api.relay.presentation.meetingpoint.MeetingPointUi

// Chave para o método de pagamento (savedStateHandle)
private const val PAYMENT_METHOD_KEY = "selected_payment_method"

fun NavGraphBuilder.productGraph(navController: NavHostController) {
    navigation<ProductGraph>(startDestination = Product(id = 0)) {

        // ── Detalhe do produto ──────────────────────────────────────────────
        composable<Product> { backStack ->
            val args = backStack.toRoute<Product>()

            ProductScreen(
                id = args.id,
                // ownerIdHint comes from the route. Callers that already know
                // who owns the resource (Home, Profile, UserProfile) pass it
                // so the bottom bar paints correctly on the first frame. Deep
                // links can omit it; the screen falls back to its cache path.
                ownerIdHint = args.ownerId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate(EditResource(args.id)) },
                onPurchaseClick = { resourceId ->
                    navController.navigate(Purchase(resourceId))
                },
                onBorrowClick = { resourceId ->
                    navController.navigate(Borrow(resourceId))
                },
                // Seller block tap → public profile route registered below.
                onSellerClick = { sellerId ->
                    navController.navigate(UserProfile(id = sellerId))
                },
                onNavigateToConversation = { convId, title, avatar, username, displayName ->
                    navController.navigate(
                        Conversation(
                            id = convId,
                            title = title,
                            otherUserAvatar = avatar,
                            otherUserUsername = username,
                            otherUserDisplayName = displayName,
                        )
                    )
                }
            )
        }

        // ── Editar recurso ──────────────────────────────────────────────────
        composable<EditResource> { backStack ->
            val args = backStack.toRoute<EditResource>()
            val viewModel: CreateResourceViewModel = viewModel(
                factory = CreateResourceViewModelFactory(AppModule.resourceRepository)
            )
            CreateResourceScreen(
                navController = navController,
                viewModel = viewModel,
                editingId = args.id
            )
        }

        // ── Perfil público de outro utilizador ──────────────────────────────
        composable<UserProfile> { backStack ->
            val args = backStack.toRoute<UserProfile>()
            UserProfileScreen(
                userId = args.id,
                onBack = { navController.popBackStack() },
                onResourceClick = { resourceId ->
                    // Every resource shown on a user-profile page is owned by
                    // the user being viewed (args.id). Threading it through
                    // as ownerId lets ProductScreen render the correct bar
                    // immediately without waiting for the network.
                    navController.navigate(Product(id = resourceId, ownerId = args.id))
                },
                onReviewsClick = {
                    navController.navigate(UserReviews(userId = args.id))
                }
            )
        }

        // ── Avaliações públicas de outro utilizador ─────────────────────────
        composable<UserReviews> { backStack ->
            val args = backStack.toRoute<UserReviews>()
            ReviewsScreen(
                userId = args.userId,
                onBack = { navController.popBackStack() },
                selectedTab = ReviewTab.SELLER
            )
        }

        // ── Fluxo de COMPRA ─────────────────────────────────────────────────
        composable<Purchase> { backStack ->
            val args = backStack.toRoute<Purchase>()

            val savedState = backStack.savedStateHandle
            val meetingPointId = savedState.get<Int>(MEETING_POINT_PICK_ID_KEY)
            val meetingPointName = savedState.get<String>(MEETING_POINT_PICK_NAME_KEY)

            val paymentMethodName = savedState.get<String>(PAYMENT_METHOD_KEY)
            val paymentMethod = paymentMethodName?.let {
                runCatching { PaymentMethod.valueOf(it) }.getOrNull()
            }

            val viewModel: ipca.api.relay.presentation.purchase.PurchaseViewModel =
                viewModel(factory = ipca.api.relay.presentation.purchase.PurchaseViewModel.factory(args.resourceId))

            if (meetingPointId != null && meetingPointName != null) {
                viewModel.onMeetingPointSelected(meetingPointId, meetingPointName)
            }
            if (paymentMethod != null) {
                viewModel.onPaymentMethodSelected(paymentMethod)
            }

            PurchaseScreen(
                resourceId = args.resourceId,
                onBack = { navController.popBackStack() },
                onPickMeetingPoint = {
                    navController.navigate(MeetingPointSelection(productId = args.resourceId))
                },
                onPickPaymentMethod = {
                    navController.navigate(PaymentMethodPicker)
                },
                onFinished = { purchaseId ->
                    navController.navigate(
                        TransactionDetail(
                            id = purchaseId,
                            type = TransactionType.PURCHASE.name
                        )
                    ) {
                        popUpTo<Purchase> { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        // ── Picker de método de pagamento ───────────────────────────────────
        composable<PaymentMethodPicker> { backStack ->
            val initialName = navController
                .previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>(PAYMENT_METHOD_KEY)
            val initial = initialName?.let {
                runCatching { PaymentMethod.valueOf(it) }.getOrNull()
            }

            PaymentMethodPickerScreen(
                initial = initial,
                onBack = { navController.popBackStack() },
                onConfirm = { method ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(PAYMENT_METHOD_KEY, method.name)
                    navController.popBackStack()
                }
            )
        }

        // ── Fluxo de EMPRÉSTIMO ─────────────────────────────────────────────
        composable<Borrow> { backStack ->
            val args = backStack.toRoute<Borrow>()

            val savedState = backStack.savedStateHandle
            val meetingPointId = savedState.get<Int>(MEETING_POINT_PICK_ID_KEY)
            val meetingPointName = savedState.get<String>(MEETING_POINT_PICK_NAME_KEY)

            val viewModel: BorrowViewModel =
                viewModel(factory = BorrowViewModel.factory(args.resourceId))

            if (meetingPointId != null && meetingPointName != null) {
                viewModel.onMeetingPointSelected(meetingPointId, meetingPointName)
            }

            BorrowScreen(
                resourceId = args.resourceId,
                onBack = { navController.popBackStack() },
                onPickMeetingPoint = {
                    navController.navigate(MeetingPointSelection(productId = args.resourceId))
                },
                onFinished = { borrowId ->
                    navController.navigate(
                        TransactionDetail(
                            id = borrowId,
                            type = TransactionType.BORROW.name
                        )
                    ) {
                        popUpTo<Borrow> { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable<MeetingPointSelection> { backStack ->
            val args = backStack.toRoute<MeetingPointSelection>()

            MeetingPointScreen(
                onBackClick = { navController.popBackStack() },
                onOpenDetails = { point ->
                    navController.navigate(
                        MeetingPointDetails(
                            id = point.id,
                            name = point.name,
                            address = point.address,
                            latitude = point.latitude,
                            longitude = point.longitude
                        )
                    )
                },
                onPicked = null
            )
        }

        composable<MeetingPointDetails> { backStack ->
            val args = backStack.toRoute<MeetingPointDetails>()

            MeetingPointDetailsScreen(
                point = MeetingPointUi(
                    id = args.id,
                    name = args.name,
                    address = args.address,
                    latitude = args.latitude,
                    longitude = args.longitude
                ),
                onBackClick = { navController.popBackStack() },
                onConfirm = { point, selectedTime ->
                    navController.navigate(
                        MeetingPointConfirmation(
                            id = point.id,
                            pointName = point.name,
                            address = point.address,
                            latitude = point.latitude,
                            longitude = point.longitude,
                            selectedTime = selectedTime
                        )
                    )
                }
            )
        }

        composable<MeetingPointConfirmation> { backStack ->
            val args = backStack.toRoute<MeetingPointConfirmation>()

            MeetingPointConfirmationScreen(
                pointName = args.pointName,
                selectedTime = args.selectedTime,
                onFinish = {
                    val targetEntry = runCatching { navController.getBackStackEntry(Purchase::class) }.getOrNull()
                        ?: runCatching { navController.getBackStackEntry(Borrow::class) }.getOrNull()

                    targetEntry?.savedStateHandle?.apply {
                        set(MEETING_POINT_PICK_ID_KEY, args.id)
                        set(MEETING_POINT_PICK_NAME_KEY, "${args.pointName} — ${args.selectedTime}")
                        set("meeting_point_pick_lat", args.latitude)
                        set("meeting_point_pick_lng", args.longitude)
                        set("meeting_point_pick_address", args.address)
                    }

                    navController.popBackStack(
                        route = MeetingPointSelection::class,
                        inclusive = true
                    )
                }
            )
        }
    }
}