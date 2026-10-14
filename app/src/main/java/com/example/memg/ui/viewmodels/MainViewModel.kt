package com.example.memg.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.memg.ui.navigation.NavRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _backStack = MutableStateFlow<List<NavRoute>>(listOf(NavRoute.Splash))
    val backStack: StateFlow<List<NavRoute>> = _backStack.asStateFlow()

    fun navigateTo(route: NavRoute) {
        _backStack.value = _backStack.value + route
    }

    fun pop() {
        if (_backStack.value.size > 1) {
            _backStack.value = _backStack.value.dropLast(1)
        }
    }

    fun replaceAll(route: NavRoute) {
        _backStack.value = listOf(route)
    }
}
