package ipca.api.relay.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import ipca.api.relay.domain.model.TransactionType
import ipca.api.relay.presentation.history.HistoryScreen
import ipca.api.relay.presentation.profile.*
import ipca.api.relay.presentation.reviews.ReviewsScreen
import ipca.api.relay.presentation.reviews.pending.PendingReviewsScreen
import ipca.api.relay.presentation.reviews.write.WriteReviewScreen
import ipca.api.relay.presentation.transaction.TransactionDetailScreen

// savedStateHandle key written by WriteReviewScreen on successful submission;
// observed by TransactionDetailScreen to flip its CTA to "✓ Avaliado".
private const val REVIEW_SUBMITTED_KEY = "review_submitted"

fun NavGraphBuilder.profileGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    navigation<ProfileGraph>(startDestination = Profile) {

        composable<Profile> { entry ->
            val parentEntry = remember(entry) {
                navController.getBackStackEntry(ProfileGraph)
            }
            val profileViewModel: ProfileViewModel = viewModel(parentEntry)

            ProfileScreen(navController, profileViewModel)
        }

        composable<EditProfile> { entry ->
            val parentEntry = remember(entry) {
                navController.getBackStackEntry(ProfileGraph)
            }
            val profileViewModel: ProfileViewModel = viewModel(parentEntry)

            EditProfileScreen(navController, profileViewModel)
        }

        composable<Settings> { entry ->
            val parentEntry = remember(entry) {
                navController.getBackStackEntry(ProfileGraph)
            }
            val profileViewModel: ProfileViewModel = viewModel(parentEntry)

            SettingsScreen(navController, profileViewModel, onLogout)
        }

        composable<ChangeUsername> { entry ->
            val parentEntry = remember(entry) {
                navController.getBackStackEntry(ProfileGraph)
            }
            val profileViewModel: ProfileViewModel = viewModel(parentEntry)

            ChangeUsernameScreen(navController, profileViewModel)
        }

        composable<History> {
            HistoryScreen(
                onTransactionClick = { id, type ->
                    navController.navigate(TransactionDetail(id, type.name))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<TransactionDetail> { backStack ->
            val args = backStack.toRoute<TransactionDetail>()

            // Observe the "review submitted" signal written by WriteReview
            // when it pops back here. The screen uses this to flip its
            // Avaliar CTA to "✓ Avaliado".
            val savedState = backStack.savedStateHandle
            val reviewed by savedState
                .getStateFlow(REVIEW_SUBMITTED_KEY, false)
                .collectAsStateWithLifecycle()

            TransactionDetailScreen(
                id = args.id,
                type = TransactionType.valueOf(args.type),
                onBack = { navController.popBackStack() },
                reviewedInSession = reviewed,
                onWriteReview = { reviewedName, reviewedPicture ->
                    navController.navigate(
                        WriteReview(
                            transactionId = args.id,
                            transactionType = args.type,
                            reviewedName = reviewedName,
                            reviewedPicture = reviewedPicture,
                        )
                    )
                }
            )
        }

        composable<SellerReviews> { entry ->
            val parentEntry = remember(entry) { navController.getBackStackEntry(ProfileGraph) }
            val profileViewModel: ProfileViewModel = viewModel(parentEntry)

            ReviewsScreen(
                userId = profileViewModel.userId,
                onBack = { navController.popBackStack() },
                selectedTab = ReviewTab.SELLER
            )
        }

        composable<ClientReviews> { entry ->
            val parentEntry = remember(entry) { navController.getBackStackEntry(ProfileGraph) }
            val profileViewModel: ProfileViewModel = viewModel(parentEntry)

            ReviewsScreen(
                userId = profileViewModel.userId,
                onBack = { navController.popBackStack() },
                selectedTab = ReviewTab.CLIENT
            )
        }

        // --- Pending reviews list + write-review form ---
        // PendingReviews picks an item then hands off to WriteReview using the
        // pending row's fields. WriteReview is also reachable directly from
        // TransactionDetail above; both entry points use the same composable.
        composable<PendingReviews> {
            PendingReviewsScreen(
                onBack = { navController.popBackStack() },
                onPick = { pending ->
                    navController.navigate(
                        WriteReview(
                            transactionId = pending.transactionId,
                            transactionType = pending.transactionType,
                            reviewedName = pending.reviewedUsername ?: "Utilizador",
                            reviewedPicture = pending.reviewedPicture,
                        )
                    )
                }
            )
        }

        composable<WriteReview> { backStack ->
            val args = backStack.toRoute<WriteReview>()
            WriteReviewScreen(
                transactionId = args.transactionId,
                transactionType = args.transactionType,
                reviewedName = args.reviewedName,
                reviewedPicture = args.reviewedPicture,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    // Signal back to whoever opened this screen. TransactionDetail
                    // flips its Avaliar CTA to "✓ Avaliado"; PendingReviews
                    // reloads on resume independently and doesn't read this key,
                    // so setting it for that caller is harmless.
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(REVIEW_SUBMITTED_KEY, true)
                    navController.popBackStack()
                }
            )
        }
    }
}