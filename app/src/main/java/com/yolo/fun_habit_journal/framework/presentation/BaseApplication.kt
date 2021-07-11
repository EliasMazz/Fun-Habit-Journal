package com.yolo.fun_habit_journal.framework.presentation

import android.app.Application
import com.yolo.fun_habit_journal.dependencyinjection.AppComponent
import com.yolo.fun_habit_journal.dependencyinjection.DaggerAppComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
open class BaseApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initAppComponent()
    }

    open fun initAppComponent() {
        appComponent = DaggerAppComponent.factory()
            .create(this)
    }
}
