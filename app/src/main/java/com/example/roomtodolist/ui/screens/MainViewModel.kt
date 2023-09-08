package com.example.roomtodolist.ui.screens

import android.util.Log
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.roomtodolist.data.Repository
import com.example.roomtodolist.data.folder.FolderTable
import com.example.roomtodolist.data.folder.folderColors
import com.example.roomtodolist.data.task.TaskTable
import com.example.roomtodolist.domain.NavigationSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    val repository: Repository,
) : ViewModel() {

    var uiState by mutableStateOf(MainUiState())
        private set

    private lateinit var navigationSystem: NavigationSystem

    lateinit var windowSizeClass: WindowSizeClass
        private set

    fun start() {
        val folders = hashMapOf<Long, FolderTable>()
        val tasks = hashMapOf<Long, TaskTable>()
        val tasksPerFolder = hashMapOf<FolderTable, MutableList<TaskTable>>()
        viewModelScope.launch(Dispatchers.IO) {
            for (folder in repository.folderDao.getFolders()) {
                folders[folder.id!!] = folder
                tasksPerFolder[folder] = mutableListOf()
                tasksPerFolder[folder]!!.addAll(repository.taskDao.getTasksFromFolder(folder.id))
            }
            for (task in repository.taskDao.getTasks()) {
                tasks[task.id!!] = task
            }
            uiState = uiState.copy(
                folders = folders,
                tasks = tasks,
                tasksPerFolder = tasksPerFolder
            )
        }
    }

    fun setNavHostController(navHostController: NavHostController) {
        navigationSystem = NavigationSystem(navHostController)
    }

    fun getNavHostController() = navigationSystem.navHostController

    fun navigateTo(destination: String) {
        navigationSystem.navigateTo(destination)
    }

    fun navigateBack() {
        navigationSystem.navigateBack()
    }

    fun setWindowSizeClass(windowSizeClass: WindowSizeClass) {
        this.windowSizeClass = windowSizeClass
    }

    fun setFolderToUpdate(folder: FolderTable) {
        uiState = uiState.copy(folderToUpdate = folder)
    }

    fun clearFolderToUpdate() {
        uiState = uiState.copy(folderToUpdate = null)
    }

    fun addFolder(folder: FolderTable) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.folderDao.addFolder(folder)
            val newFolder = folder.copy(id = id)
            updateFolderState(newFolder, Operation.ADD)
            updateTasksPerFolderKeyState(id, Operation.ADD)
        }
    }

    fun getFolderColors() = folderColors

    fun updateFolder(folder: FolderTable) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.folderDao.updateFolder(folder)
            updateFolderState(folder, Operation.CHANGE)
            updateTasksPerFolderKeyState(folder.id!!, Operation.CHANGE)
        }
    }

    fun deleteFolder(folder: FolderTable) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.folderDao.deleteFolder(folder.id!!)
            updateTasksPerFolderKeyState(folder.id, Operation.DELETE)
            updateFolderState(folder, Operation.DELETE)
        }
    }

    fun setTaskToUpdate(task: TaskTable) {
        uiState = uiState.copy(taskToUpdate = task)
    }

    fun clearTaskToUpdate() {
        uiState = uiState.copy(taskToUpdate = null)
    }

    fun addTask(task: TaskTable) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.taskDao.addTask(task)
            Log.d(TAG, "addTask: $id")
            val newTask = task.copy(id = id)
            updateTaskState(newTask, Operation.ADD)
            updateTasksPerFolderValueState(taskId = newTask.id!!, operation = Operation.ADD)
        }
    }

    fun updateTask(task: TaskTable) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.taskDao.updateTask(task)
            updateTaskState(task, Operation.CHANGE)
            updateTasksPerFolderValueState(task.id!!, Operation.CHANGE)
        }
    }

    fun deleteTask(task: TaskTable) {
        viewModelScope.launch {
            repository.taskDao.deleteTask(taskId = task.id!!)
            updateTasksPerFolderValueState(task.id, Operation.DELETE)
            updateTaskState(task, Operation.DELETE)
        }
    }

    private fun updateTaskState(task: TaskTable, operation: Operation) {
        if (task.id == null)
            throw Exception("TASK ID SHOULD NOT BE NULL")
        when (operation) {
            Operation.ADD, Operation.CHANGE -> {
                uiState.tasks[task.id] = task
            }
            Operation.DELETE -> {
                uiState.tasks.remove(task.id)
            }
        }
    }

    private fun updateFolderState(folder: FolderTable, operation: Operation) {
        if (folder.id == null)
            throw Exception("FOLDER ID SHOULD NOT BE NULL")
        when (operation) {
            Operation.ADD, Operation.CHANGE -> {
                uiState.folders[folder.id] = folder
            }
            Operation.DELETE -> {
                uiState.folders.remove(folder.id)
            }
        }
    }

    private fun updateTasksPerFolderKeyState(folderId: Long, operation: Operation) {
        val folder = uiState.folders[folderId] ?: throw Exception("FOLDER $folderId DO NOT EXISTS")
        when (operation) {
            Operation.ADD -> {
                uiState.tasksPerFolder[folder] = mutableListOf()
            }
            Operation.CHANGE -> {
                for (eachFolder in uiState.tasksPerFolder.keys) {
                    if (eachFolder.id == folderId) {
                        val list = uiState.tasksPerFolder[eachFolder]!!
                        uiState.tasksPerFolder.remove(eachFolder)
                        uiState.tasksPerFolder[folder] = list
                        return
                    }
                }
            }
            Operation.DELETE -> {
                uiState.tasksPerFolder.remove(folder)
            }
        }
    }

    private fun updateTasksPerFolderValueState(taskId: Long, operation: Operation) {
        val task = uiState.tasks[taskId] ?: throw Exception("TASK $taskId DO NOT EXISTS")
        val folder = uiState.folders[task.folder] ?: throw Exception("FOLDER ${task.folder} DO NOT EXISTS")
        when (operation) {
            Operation.ADD -> {
                uiState.tasksPerFolder[folder]!!.add(task)
            }
            Operation.CHANGE -> {
                val list = uiState.tasksPerFolder[folder]!!
                for (index in list.indices) {
                    if (list[index].id == task.id) {
                        if (list[index].folder == task.folder)
                            uiState.tasksPerFolder[folder]!![index] = task
                        else
                            uiState.tasksPerFolder[folder]!!.remove(list[index])

                        return
                    }
                }
                uiState.tasksPerFolder[folder]!!.add(task)
            }
            Operation.DELETE -> {
                uiState.tasksPerFolder[folder]!!.remove(task)
            }
        }
    }

    enum class Operation {
        ADD,
        CHANGE,
        DELETE
    }

}