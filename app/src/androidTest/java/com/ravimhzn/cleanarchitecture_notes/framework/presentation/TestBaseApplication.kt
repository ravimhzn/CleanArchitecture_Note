package com.ravimhzn.cleanarchitecture_notes.framework.presentation

import com.ravimhzn.cleanarchitecture_notes.di.DaggerTestAppComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class TestBaseApplication : BaseApplication(){

    override fun initAppComponent() {
        appComponent = DaggerTestAppComponent
            .factory()
            .create(this)
    }

}