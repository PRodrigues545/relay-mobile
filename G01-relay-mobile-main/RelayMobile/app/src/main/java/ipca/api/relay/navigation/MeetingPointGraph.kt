package ipca.api.relay.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ipca.api.relay.presentation.meetingpoint.MeetingPointConfirmationScreen
import ipca.api.relay.presentation.meetingpoint.MeetingPointDetailsScreen
import ipca.api.relay.presentation.meetingpoint.MeetingPointScreen
import ipca.api.relay.presentation.meetingpoint.MeetingPointUi

const val MEETING_POINT_PICK_ID_KEY = "meeting_point_pick_id"
const val MEETING_POINT_PICK_NAME_KEY = "meeting_point_pick_name"

fun NavGraphBuilder.meetingPointGraph(
    navController: NavHostController
) {
    composable<MeetingPointSelection> {
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
            onPicked = { point ->
                val prev = navController.previousBackStackEntry
                prev?.savedStateHandle?.set(MEETING_POINT_PICK_ID_KEY, point.id)
                prev?.savedStateHandle?.set(MEETING_POINT_PICK_NAME_KEY, point.name)
                navController.popBackStack()
            }
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

    composable<MeetingPointConfirmation> { backStackEntry ->
        val route = backStackEntry.toRoute<MeetingPointConfirmation>()
        MeetingPointConfirmationScreen(
            pointName = route.pointName,
            selectedTime = route.selectedTime,
            onFinish = {
                navController.popBackStack(
                    route = MeetingPointSelection::class,
                    inclusive = true
                )
            }
        )
    }
}