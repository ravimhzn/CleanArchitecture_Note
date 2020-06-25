package com.ravimhzn.cleanarchitecture_notes.di

import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.implementation.NoteCacheDataSourceImpl
import com.ravimhzn.cleanarchitecture_notes.busniess.data.network.abstraction.NoteNetworkDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.network.implementation.NoteNetworkDataSourceImpl
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.NoteFactory
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.util.DateUtil
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.common.DeleteNote
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.notedetail.NoteDetailInteractors
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.notelist.*
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.cache.abstraction.NoteDaoService
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.cache.database.NoteDao
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.cache.database.NoteDatabase
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.cache.implementation.NoteDaoServiceImpl
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.cache.mappers.CacheMapper
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.network.abstraction.NoteFirestoreService
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.network.implementation.NoteFirestoreServiceImpl
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.network.mapper.NetworkMapper
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object AppModule {


    // https://developer.android.com/reference/java/text/SimpleDateFormat.html?hl=pt-br
    @JvmStatic
    @Singleton
    @Provides
    fun provideDateFormat(): SimpleDateFormat {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("UTC-7") // match firestore
        return sdf
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideDateUtil(dateFormat: SimpleDateFormat): DateUtil {
        return DateUtil(
            dateFormat
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPrefsEditor(
        sharedPreferences: SharedPreferences
    ): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteFactory(dateUtil: DateUtil): NoteFactory {
        return NoteFactory(
            dateUtil
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDAO(noteDatabase: NoteDatabase): NoteDao {
        return noteDatabase.noteDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteCacheMapper(): CacheMapper {
        return CacheMapper()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteNetworkMapper(dateUtil: DateUtil): NetworkMapper {
        return NetworkMapper(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDaoService(
        noteDao: NoteDao,
        noteEntityMapper: CacheMapper,
        dateUtil: DateUtil
    ): NoteDaoService {
        return NoteDaoServiceImpl(noteDao, noteEntityMapper, dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteCacheDataSource(
        noteDaoService: NoteDaoService
    ): NoteCacheDataSource {
        return NoteCacheDataSourceImpl(noteDaoService)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestoreService(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        networkMapper: NetworkMapper
    ): NoteFirestoreService {
        return NoteFirestoreServiceImpl(
            firebaseAuth,
            firebaseFirestore,
            networkMapper
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteNetworkDataSource(
        firestoreService: NoteFirestoreServiceImpl
    ): NoteNetworkDataSource {
        return NoteNetworkDataSourceImpl(
            firestoreService
        )
    }

//    @JvmStatic
//    @Singleton
//    @Provides
//    fun provideSyncNotes(
//        noteCacheDataSource: NoteCacheDataSource,
//        noteNetworkDataSource: NoteNetworkDataSource
//    ): SyncNotes {
//        return SyncNotes(
//            noteCacheDataSource,
//            noteNetworkDataSource
//        )
//    }

//    @JvmStatic
//    @Singleton
//    @Provides
//    fun provideSyncDeletedNotes(
//        noteCacheDataSource: NoteCacheDataSource,
//        noteNetworkDataSource: NoteNetworkDataSource
//    ): SyncDeletedNotes{
//        return SyncDeletedNotes(
//            noteCacheDataSource,
//            noteNetworkDataSource
//        )
//    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDetailInteractors(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource
    ): NoteDetailInteractors {
        return NoteDetailInteractors(
            DeleteNote(noteCacheDataSource, noteNetworkDataSource)
        )
    }
//
    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteListInteractors(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource,
        noteFactory: NoteFactory
    ): NoteListInteractors {
        return NoteListInteractors(
            InsertNewNote(noteCacheDataSource, noteNetworkDataSource, noteFactory),
            DeleteNote(noteCacheDataSource, noteNetworkDataSource),
            SearchNotes(noteCacheDataSource),
            GetNumNotes(noteCacheDataSource),
            RestoreDeletedNotes(noteCacheDataSource, noteNetworkDataSource),
            DeleteMultipleNotes(noteCacheDataSource, noteNetworkDataSource)
        )
    }



}









