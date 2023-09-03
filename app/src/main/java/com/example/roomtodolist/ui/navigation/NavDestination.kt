package com.example.roomtodolist.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import com.example.roomtodolist.R
import com.example.roomtodolist.ui.screens.addfolder.AddFolderScreen
import com.example.roomtodolist.ui.screens.addfolder.AddFolderViewModel
import com.example.roomtodolist.ui.screens.addtask.AddTaskScreen
import com.example.roomtodolist.ui.screens.addtask.AddTaskViewModel
import com.example.roomtodolist.ui.screens.calendar.CalendarScreen
import com.example.roomtodolist.ui.screens.home.HomeScreen
import com.example.roomtodolist.ui.screens.home.HomeViewModel
import com.example.roomtodolist.ui.screens.tasks.TasksScreen

interface ScreenRoute

enum class RootRoutes {
    HOME_ROOT,
    TASK_ROOT,
    CALENDAR_ROOT,
    ADD_TASK_ROOT,
}

enum class MainRoutes : ScreenRoute {
    HOME,
    TASKS,
    CALENDAR,
    ADD_TASK,
}

enum class NestedRoutes : ScreenRoute {
    ADD_FOLDER
}

sealed class Routes(
    val route: RootRoutes,
    val mainDestination: MainDestinations,
    val nestedDestination: MutableList<NestedDestinations>? = null
) {
    object Home : Routes(
        route = RootRoutes.HOME_ROOT,
        mainDestination = MainDestinations.Home,
        nestedDestination = mutableListOf(NestedDestinations.AddFolder)
    )
    object Task : Routes(
        route = RootRoutes.TASK_ROOT,
        mainDestination = MainDestinations.Task,
        nestedDestination = null
    )
    object Calendar : Routes(
        route = RootRoutes.CALENDAR_ROOT,
        mainDestination = MainDestinations.Calendar,
        nestedDestination = null
    )
    object AddTask : Routes(
        route = RootRoutes.ADD_TASK_ROOT,
        mainDestination = MainDestinations.AddTask,
        nestedDestination = mutableListOf(NestedDestinations.AddFolder)
    )
}


sealed class MainDestinations(
    val route: MainRoutes,
    val title: String,
    val icon: NavIcon,
    var screen: @Composable (viewModel: ViewModel?) -> Unit
) {
    object Home : MainDestinations(
        route = MainRoutes.HOME,
        title = "Home",
        icon = NavIcon(R.drawable.filled_home_icon, R.drawable.outlined_home_icon),
        screen = { viewModel ->
            HomeScreen(viewModel as HomeViewModel)
        }
    )
    object Task : MainDestinations(
        route = MainRoutes.TASKS,
        title = "Tasks",
        icon = NavIcon(R.drawable.filled_task_icon, R.drawable.outlined_task_icon),
        screen = {
            TasksScreen()
        }
    )
    object Calendar : MainDestinations(
        route = MainRoutes.CALENDAR,
        title = "Calendar",
        icon = NavIcon(R.drawable.filled_calendar_icon, R.drawable.outlined_calendar_icon),
        screen = {
            CalendarScreen()
        }
    )
    object AddTask : MainDestinations(
        route = MainRoutes.ADD_TASK,
        title = "AddTask",
        icon = NavIcon(R.drawable.filled_add_icon, R.drawable.outlined_add_icon),
        screen = {
            AddTaskScreen(it as AddTaskViewModel)
        }
    )
}

sealed class NestedDestinations(
    val route: NestedRoutes,
    var screen: @Composable (viewModel: ViewModel?) -> Unit
) {
    object AddFolder : NestedDestinations(
        route = NestedRoutes.ADD_FOLDER,
        screen = { viewModel -> AddFolderScreen(viewModel as AddFolderViewModel) }
    )
}

data class NavIcon(
    val selected: Int,
    val unselected: Int
)