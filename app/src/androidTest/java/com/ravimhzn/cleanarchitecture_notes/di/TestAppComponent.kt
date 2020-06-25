package com.ravimhzn.cleanarchitecture_notes.di

import com.ravimhzn.cleanarchitecture_notes.busniess.TemporaryTest
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.network.implementation.NoteFirestoreServiceImplTest
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.TestBaseApplication
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton


@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
@Component(
    modules = [
        AppModule::class,
        TestModule::class
    ]
)

interface TestAppComponent : AppComponent {
    @Component.Factory
    interface Factory {

        fun create(@BindsInstance app: TestBaseApplication): TestAppComponent
    }

    fun inject(temporaryTest: TemporaryTest)
    fun inject(noteFirestoreServiceImplTest: NoteFirestoreServiceImplTest)
}