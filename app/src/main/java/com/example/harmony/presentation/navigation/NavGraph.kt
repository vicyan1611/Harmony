package com.example.harmony.presentation.navigation

import androidx.compose.material3.Text
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
import com.example.harmony.presentation.main.config_server.ConfigServerScreen
import com.example.harmony.presentation.main.create_server.CreateServerScreen
import com.example.harmony.presentation.main.dm.DirectMessageChatScreen
import com.example.harmony.presentation.main.dm.DirectMessageListScreen
import com.example.harmony.presentation.main.home.HomeScreen
import com.example.harmony.presentation.main.join_server.JoinServerScreen
import com.example.harmony.presentation.main.profile.MyProfileScreen
import com.example.harmony.presentation.main.profile.edit.EditProfileScreen
import com.example.harmony.presentation.main.search.UserSearchScreen
import com.example.harmony.presentation.main.voice.VoiceChannelScreen

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

//        Config Server
        composable(
            route = NavRoutes.CONFIG_SERVER, // Use the route pattern with placeholder
            arguments = listOf(navArgument("serverId") { type = NavType.StringType }) // Define the argument
        ) { backStackEntry ->
            // The NavController and ViewModel handle the argument extraction internally
            // via SavedStateHandle when using hiltViewModel()
            ConfigServerScreen(
                navController = navController
                // ViewModel will be injected by Hilt and get the serverId from SavedStateHandle
            )
        }

//         Main Screens
        composable(route = NavRoutes.HOME) {
            HomeScreen(
                navController = navController,
                onNavigateToJoinServer = {
                    navController.navigate(NavRoutes.JOIN_SERVER)
                }
            )
        }

//        Create server
        composable(route = NavRoutes.CREATE_SERVER) {
            CreateServerScreen (
                mainNavController = navController
            )
        }

        composable(route = NavRoutes.SETTINGS) {
            Text("Settings Screen")
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

        composable(route = NavRoutes.JOIN_SERVER) {
            JoinServerScreen(
                navController = navController,
                onJoinSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.JOIN_SERVER) { inclusive = true }
                    }
                }
            )
        }

//        Channel chat
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
                    navController.navigate(NavRoutes.USER_SEARCH)
                }
            )
        }

        composable(
            route = NavRoutes.DM_CHAT,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) {
            DirectMessageChatScreen() // ViewModel gets args via SavedStateHandle
        }

        composable(
            route = NavRoutes.VOICE_CHANNEL,
            arguments = listOf(
                navArgument("serverId") { type = NavType.StringType },
                navArgument("channelId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            VoiceChannelScreen(navController = navController)
        }

        composable(route = NavRoutes.USER_SEARCH) {
            UserSearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDmChat = { conversationId ->
                    // Navigate to DM chat, potentially clearing search from backstack
                    navController.navigate(NavRoutes.getDmChatRoute(conversationId)) {
                        popUpTo(NavRoutes.USER_SEARCH) { inclusive = true }
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