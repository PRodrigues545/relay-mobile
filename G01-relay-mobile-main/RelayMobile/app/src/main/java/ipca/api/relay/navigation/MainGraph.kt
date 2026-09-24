package ipca.api.relay.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

fun NavGraphBuilder.mainTabsGraph(navController: NavHostController, onLogout: () -> Unit) {

    homeGraph(navController)
    requestsGraph(navController)
    createGraph(navController)
    messagesGraph(navController)
    profileGraph(navController, onLogout)

    productGraph(navController)

    // meetingPointGraph foi removido daqui — está dentro de productGraph
    // para que o TimePicker e o fluxo completo funcionem no contexto de compra/empréstimo
}