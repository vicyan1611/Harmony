package com.example.harmony.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.harmony.presentation.auth.login.LoginScreen
import com.example.harmony.presentation.auth.register.RegisterScreen
import com.example.harmony.presentation.auth.splash.SplashScreen
import com.example.harmony.presentation.main.chat.ChatScreen
import com.example.harmony.presentation.main.dm.DirectMessageChatScreen
import com.example.harmony.presentation.main.dm.DirectMessageListScreen
import com.example.harmony.presentation.main.home.HomeScreen

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
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
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
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToChat = { serverId, channelId ->
                    navController.navigate(NavRoutes.getChatRoute(serverId, channelId))
                },
                onNavigateToDmList = {
                    navController.navigate(NavRoutes.DM_LIST)
                },
            )
        }

        composable(
            route = NavRoutes.CHAT, arguments = listOf(
                navArgument("serverId") { type = NavType.StringType },
                navArgument("channelId") { type = NavType.StringType }
            )
        ) {
            ChatScreen()
        }

        composable(route = NavRoutes.DM_LIST) {
            DirectMessageListScreen(
                onNavigateToDmChat = { conversationId ->
                    navController.navigate(NavRoutes.getDmChatRoute(conversationId))
                },
                onNavigateToUserSearch = {
                    navController.navigate(NavRoutes.USER_SEARCH) // Navigate to user search
                }
            )
        }

        composable(
            route = NavRoutes.DM_CHAT, arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType }
            )
        ) {
            DirectMessageChatScreen()
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