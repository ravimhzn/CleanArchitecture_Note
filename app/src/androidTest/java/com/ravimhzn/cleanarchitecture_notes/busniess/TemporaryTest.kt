package com.ravimhzn.cleanarchitecture_notes.busniess

import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.firebase.firestore.FirebaseFirestore
import com.ravimhzn.cleanarchitecture_notes.di.TestAppComponent
import com.ravimhzn.cleanarchitecture_notes.framework.BaseTest
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.TestBaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class TemporaryTest: BaseTest() {



    @Inject
    lateinit var firestore: FirebaseFirestore

    init {
        (application.appComponent as TestAppComponent).inject(this)
    }

    @Test
    fun randomTest() {
        assert(::firestore.isInitialized)
    }

    override fun injectTest() {
        TODO("Not yet implemented")
    }
}