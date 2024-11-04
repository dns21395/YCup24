package com.example.ycup24.di

import android.content.Context
import com.example.ycup24.ui.ScreenViewModel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component
@Singleton
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun viewModel(): ScreenViewModel
}