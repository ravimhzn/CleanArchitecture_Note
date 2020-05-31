package com.ravimhzn.cleanarchitecture_notes.di

import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.network.abstraction.NoteNetworkDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.model.Note
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.model.NoteDataFactory
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.model.NoteFactory
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.util.DateUtil
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors.notelist.data.cache.FakeNoteCacheDataSourceImpl
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors.notelist.data.network.FakeNoteNetworkDataSourceImpl
import com.ravimhzn.cleanarchitecture_notes.utils.isUnitTest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class DependencyContainer {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
    val dateUtil = DateUtil(dateFormat)
    lateinit var noteNetworkDataSource: NoteNetworkDataSource
    lateinit var noteCacheDataSource: NoteCacheDataSource
    lateinit var noteFactory: NoteFactory
    lateinit var noteDateFactory: NoteDataFactory
    private var notesData: HashMap<String, Note> = HashMap() // To avoid null-pointer

    init {
        isUnitTest = true // for Logger.kt
    }

    fun build() {
        this.javaClass.classLoader?.let { classLoader ->
            noteDateFactory = NoteDataFactory(classLoader)

            //fake data set
            notesData = noteDateFactory.produceHashMapOfNotes(
                noteDateFactory.produceListOfNotes()
            )
        }

        noteFactory =
            NoteFactory(
                dateUtil
            )
        noteNetworkDataSource = FakeNoteNetworkDataSourceImpl(
            notesData = notesData,
            deletedNotesData = HashMap()
        )
        noteCacheDataSource = FakeNoteCacheDataSourceImpl(
            notesData = notesData,
            dateUtil = dateUtil
        )
    }

}