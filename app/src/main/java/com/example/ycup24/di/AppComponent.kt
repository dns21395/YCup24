package com.example.ycup24.di

import com.example.ycup24.ui.ScreenViewModel
import dagger.Component
import javax.inject.Singleton

@Component
@Singleton
interface AppComponent {

    fun viewModel(): ScreenViewModel
}