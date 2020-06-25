package com.ravimhzn.cleanarchitecture_notes.framework

import androidx.test.core.app.ApplicationProvider
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.TestBaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
abstract class BaseTest {

    val application: TestBaseApplication =
        ApplicationProvider.getApplicationContext() as TestBaseApplication

    abstract fun injectTest()
}