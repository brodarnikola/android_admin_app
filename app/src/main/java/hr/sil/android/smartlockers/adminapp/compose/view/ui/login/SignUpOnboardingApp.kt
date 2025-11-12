package hr.sil.android.smartlockers.adminapp.compose.view.ui.login

import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.compose.view.ui.components.AppScaffold
import hr.sil.android.smartlockers.adminapp.compose.view.ui.theme.AppTheme


@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SignUpOnboardingApp(
    signUpOnboardingViewModel: SignUpOnboardingViewModel
) {
    //val selectedTheme = signUpOnboardingViewModel.selectedTheme.collectAsState(initial = null)

    val appState = rememberSignUpAppState()
    val navBackStackEntry =
        appState.navController.currentBackStackEntryAsState() // navController.currentBackStackEntryAsState()


    AppTheme {
        AppScaffold(scaffoldState = appState.scaffoldState, modifier = Modifier.semantics {
            testTagsAsResourceId = true
        }) { innerPaddingModifier ->
            val modifier = Modifier
            // Box required because there is no background in transition moment when changing screens
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                val routeFirstScreen = SignUpOnboardingSections.LOGIN_SCREEN.route

                NavigationStack(routeFirstScreen, modifier)
//                NavHost(
//                    navController = appState.navController,
//                    startDestination = routeFirstScreen,
//                    modifier = Modifier.padding(innerPaddingModifier)
//                ) {
//                    navGraph(
//                        navBackStackEntry = navBackStackEntry,
//                        modifier = modifier,
//                        nextScreen = appState::navigateToRoute,
//                        goToFirstOnboardingScreen =  { route ->
//                            //appState.navigateToAnimatedCreditCard(route = route)
//                        },
//                        goToSecondOnboardingScreen =  { route ->
//                            //appState.navigateToMovieDetails(route = route, movieId = movieId)
//                        },
//                        navigateUp = {
//                            appState.upPress()
//                        }
//                    )
//                }
            }
        }
    }
}

@Composable
fun NavigationStack(routeFirstScreen: String, modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = routeFirstScreen) {
        composable(
            SignUpOnboardingSections.LOGIN_SCREEN.route,
        ) {
            LoginScreen(
                modifier = modifier,
                viewModel = viewModel(),
                navigateUp = {
                    navController.popBackStack()
                },
                nextScreen = { route ->
                    if (route != navController.currentDestination?.route) {
                        navController.navigate(route)
                    }
                }
            )
        }

    }
}

object NavArguments {
    const val EMAIL = "emailAddress"
}


enum class SignUpOnboardingSections(
    @StringRes val title: Int,
    val icon: ImageVector,
    val route: String
) {
    LOGIN_SCREEN(R.string.login_submit_title, Icons.Outlined.Search, "splash/firstOnboarding"),
    SECOND_ONBOARDING_SCREEN(R.string.logout_again, Icons.Outlined.Search, "splash/secondOnboarding"),
}