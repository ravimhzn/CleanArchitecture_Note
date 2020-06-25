package com.ravimhzn.cleanarchitecture_notes.framework.presentation

import android.app.Application
import com.ravimhzn.cleanarchitecture_notes.di.AppComponent
import com.ravimhzn.cleanarchitecture_notes.di.DaggerAppComponent
import kotlinx.coroutines.*

@FlowPreview
@ExperimentalCoroutinesApi
open class BaseApplication : Application(){

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initAppComponent()
    }

    open fun initAppComponent(){
        appComponent = DaggerAppComponent
            .factory()
            .create(this)
    }
}