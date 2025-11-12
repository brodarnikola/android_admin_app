package hr.sil.android.smartlockers.adminapp.compose.view.ui.login


import android.content.res.Resources
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.PopUpToBuilder
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.CoroutineScope


object SignUpOnboardingDestinations {
    const val ONBOARDING_ROUTE = "onboardingRoute"
    const val SMS_VERIFICATION_CODE = "smsVerificationCode"
    const val GET_STARTED = "getStarted"
    const val SECURITY_ROUTE = "securityRoute"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun rememberSignUpAppState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    navController: NavHostController = rememberAnimatedNavController(),
    resource: Resources = resources(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) =
    remember(scaffoldState, navController, resource, coroutineScope) {
        SignUpAppState(scaffoldState, navController, resource, coroutineScope)
    }

@Stable
class SignUpAppState(
    val scaffoldState: ScaffoldState,
    val navController: NavHostController,
    private val resources: Resources,
    coroutineScope: CoroutineScope
) {

    // ----------------------------------------------------------
    // we can do async operations here if we need to for e.g. fetch something
    // ----------------------------------------------------------
    //    init {
    //        coroutineScope.launch {
    //
    //        }
    //    }

    // ----------------------------------------------------------
    // Navigation state source of truth
    // ----------------------------------------------------------

    private val currentRoute: String?
        get() = navController.currentDestination?.route

    fun upPress() {
        navController.navigateUp()
    }

    fun navigateToRoute(route: String, navBackStackEntry: State<NavBackStackEntry?>) {
        //if(navBackStackEntry.value?.lifecycleIsResumed() == true)
            if (route != currentRoute) {
                navController.navigate(route)
                    //launchSingleTop = true
                    //restoreState = true

                    // Pop up backstack to the first destination and save state. This makes going back
                    // to the start destination when pressing back in any other bottom tab.
                    // popUpTo(findStartDestination(navController.graph).id) { saveState = true }

            }
    }

    fun navigateToRouteDeletePreviousComposable(route: String) {
        if (route != currentRoute) {
            navController.navigate(route) {
                launchSingleTop = true
                popUpTo(route = route) { inclusive = true }
                // Pop up backstack to the first destination and save state. This makes going back
                // to the start destination when pressing back in any other bottom tab.
                // popUpTo(findStartDestination(navController.graph).id) { saveState = true }
            }
        }
    }

    fun goToTwoFactoryAuthenticationScreen(smsVerificationCode: String, navBackStackEntry: NavBackStackEntry) {
        if (navBackStackEntry.lifecycleIsResumed()) {
            //navController.navigate( "${SignUpOnboardingSections.TWO_FACTOR_AUTHENTICATION.route}/$smsVerificationCode" )
            // navController.navigate("${SignUpOnboardingSections.CHOOSE_MESSENGER_TYPE.route}/$titleText/$displayContinueButton")
        }
    }
}

//private tailrec fun findStartDestination(graph: NavDestination): NavDestination {
//    return if (graph is NavGraph) findStartDestination(graph.findStartDestination()) else graph
//}

/**
 * If the lifecycle is not resumed it means this NavBackStackEntry already processed a nav event.
 *
 * This is used to de-duplicate navigation events.
 */
@Suppress("UsePropertyAccessSyntax")
private fun NavBackStackEntry.lifecycleIsResumed() =
    this.lifecycle.currentState == Lifecycle.State.RESUMED
    //this.getLifecycle().currentState == Lifecycle.State.RESUMED

/**
 * A composable function that returns the [Resources]. It will be recomposed when `Configuration`
 * gets updated.
 */
@Composable
@ReadOnlyComposable
private fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}
