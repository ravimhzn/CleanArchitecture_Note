package com.ravimhzn.cleanarchitecture_notes.di

import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.NoteFactory
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.NoteDataFactory
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.cache.database.NoteDatabase
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.TestBaseApplication
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object TestModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDb(app: TestBaseApplication): NoteDatabase {
        return Room
            .inMemoryDatabaseBuilder(app, NoteDatabase::class.java)
            .fallbackToDestructiveMigration()
            .build()
    }


    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseFirestoreSettings(): FirebaseFirestoreSettings {
        return FirebaseFirestoreSettings.Builder()
            .setHost("10.0.2.2:8080")
            .setSslEnabled(false)
            .setPersistenceEnabled(false)
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseFirestore(settings: FirebaseFirestoreSettings): FirebaseFirestore {
        val fireStore = FirebaseFirestore.getInstance()
        fireStore.firestoreSettings = settings
        return fireStore
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDataFactory(
        application: TestBaseApplication,
        noteFactory: NoteFactory
    ): NoteDataFactory = NoteDataFactory(application, noteFactory)

}