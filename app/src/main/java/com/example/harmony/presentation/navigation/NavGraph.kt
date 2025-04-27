package com.example.harmony.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.harmony.domain.model.AppLanguage
import com.example.harmony.presentation.auth.login.LoginScreen
import com.example.harmony.presentation.auth.register.RegisterScreen
import com.example.harmony.presentation.auth.splash.SplashScreen
import com.example.harmony.presentation.main.home.HomeScreen
import com.example.harmony.presentation.main.profile.MyProfileScreen
import com.example.harmony.presentation.main.profile.edit.EditProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(route = NavRoutes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(route = NavRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.REGISTER)
                },
                onNavigateToHome = {
//                    navController.navigate(NavRoutes.HOME) {
//                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
//                    }
                    navController.navigate(NavRoutes.MY_PROFILE) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = NavRoutes.MY_PROFILE) {
            MyProfileScreen(
                onNavigateToEditProfile = { navController.navigate(NavRoutes.EDIT_PROFILE) },
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        // Clear backstack up to home or splash depending on flow
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Composable for editing profile
        composable(route = NavRoutes.EDIT_PROFILE) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Existing ProfileScreen (might be used for settings now)
        composable(route = NavRoutes.PROFILE) {
            // Decide how/if you still use this screen.
            // Maybe rename route to SETTINGS?
            // ProfileScreen(navController = navController)
        }

        composable(route = NavRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.REGISTER) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

//         Main Screens
        composable(route = NavRoutes.HOME) {
            HomeScreen(
//                onNavigateToServerDetail = { serverId ->
//                    navController.navigate(NavRoutes.getServerDetailRoute(serverId))
//                },
//                onNavigateToProfile = {
//                    navController.navigate(NavRoutes.PROFILE)
//                }
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.HOME) {inclusive = true}
                        launchSingleTop = true
                    }
                }
            )
        }
//
//        composable(
//            route = NavRoutes.SERVER_DETAIL,
//            arguments = listOf(
//                navArgument("serverId") {
//                    type = NavType.StringType
//                }
//            )
//        ) { backStackEntry ->
//            val serverId = backStackEntry.arguments?.getString("serverId") ?: ""
//            // Will implement ServerDetailScreen later
//        }
//
//        composable(
//            route = NavRoutes.CHANNEL_DETAIL,
//            arguments = listOf(
//                navArgument("channelId") {
//                    type = NavType.StringType
//                }
//            )
//        ) { backStackEntry ->
//            val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
//            // Will implement ChannelDetailScreen later
//        }
//
//        composable(route = NavRoutes.PROFILE) {
//            ProfileScreen(
//                onNavigateBack = {
//                    navController.navigateUp()
//                }
//            )
//        }
    }
}