package com.ravimhzn.cleanarchitecture_notes.busniess.interactors.notelist

import com.ravimhzn.cleanarchitecture_notes.busniess.interactors.notelist.data.cache.FORCE_GENERAL_FAILURE
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors.notelist.data.cache.FORCE_NEW_NOTE_EXCEPTION
import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.CacheErrors
import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.network.abstraction.NoteNetworkDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.state.DataState
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.model.NoteFactory
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors.notelist.InsertNewNote.Companion.INSERT_NOTE_FAILED
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors.notelist.InsertNewNote.Companion.INSERT_NOTE_SUCCESS
import com.ravimhzn.cleanarchitecture_notes.di.DependencyContainer
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notelist.state.NoteListStateEvent
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*


/*
Test cases:
1. insertNote_success_confirmNetworkAndCacheUpdated()
    a) insert a new note
    b) listen for INSERT_NOTE_SUCCESS emission from flow
    c) confirm cache was updated with new note
    d) confirm network was updated with new note
2. insertNote_fail_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force a failure (return -1 from db operation)
    c) listen for INSERT_NOTE_FAILED emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force an exception
    c) listen for CACHE_ERROR_UNKNOWN emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
 */

@InternalCoroutinesApi
class InsertNewNoteTest {
    // system in test
    private val insertNewNoteSUT: InsertNewNote

    // dependencies
    private val dependencyContainer: DependencyContainer
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource
    private val noteFactory: NoteFactory

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        insertNewNoteSUT = InsertNewNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource,
            noteFactory = noteFactory
        )
    }


    @Test
    fun insertNote_success_confirmNetworkAndCacheUpdated() {
        runBlocking {//Block the current thread the test is running

            val newNote = noteFactory.createSingleNote(
                id = null,
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString()
            )

            insertNewNoteSUT.insertNewNote(
                id = newNote.id,
                title = newNote.title,
                body = newNote.body,
                stateEvent = NoteListStateEvent.InsertNewNoteEvent(newNote.title, newNote.body)
            ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
                override suspend fun emit(value: DataState<NoteListViewState>?) {
                    println(value?.stateMessage?.response?.message)
                    assertEquals(
                        value?.stateMessage?.response?.message,
                        INSERT_NOTE_SUCCESS
                    )
                }
            })

            //confirm cache was updated
            val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
            assertTrue { cacheNoteThatWasInserted == newNote }

            //confirm network was updated
            val confirmNetworkWasUpdatedWithNewNote = noteNetworkDataSource.searchNote(newNote)
            assertTrue { confirmNetworkWasUpdatedWithNewNote == newNote }
        }
    }

    @Test
    fun insertNote_fail_confirmNetworkAndCacheUnchanged() {
        runBlocking {
            val newNote = noteFactory.createSingleNote(
                id = FORCE_GENERAL_FAILURE,
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString()
            )

            insertNewNoteSUT.insertNewNote(
                id = newNote.id,
                title = newNote.title,
                body = newNote.body,
                stateEvent = NoteListStateEvent.InsertNewNoteEvent(newNote.title, newNote.body)
            ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
                override suspend fun emit(value: DataState<NoteListViewState>?) {
                    assertEquals(
                        value?.stateMessage?.response?.message,
                        INSERT_NOTE_FAILED
                    )
                }
            })

            //confirm not cache was updated
            val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
            // assertTrue { cacheNoteThatWasInserted == null }
            assertNull(cacheNoteThatWasInserted)

            //confirm network was not updated
            val confirmNetworkWasUpdatedWithNewNote = noteNetworkDataSource.searchNote(newNote)
            assertTrue { confirmNetworkWasUpdatedWithNewNote == null }
        }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() {
        runBlocking {
            val newNote = noteFactory.createSingleNote(
                id = FORCE_NEW_NOTE_EXCEPTION,
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString()
            )

            insertNewNoteSUT.insertNewNote(
                id = newNote.id,
                title = newNote.title,
                body = newNote.body,
                stateEvent = NoteListStateEvent.InsertNewNoteEvent(newNote.title, newNote.body)
            ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
                override suspend fun emit(value: DataState<NoteListViewState>?) {
                    println("hi test ${value?.stateMessage?.response?.message}")
                    assert(
                        value?.stateMessage?.response?.message?.contains(CacheErrors.CACHE_ERROR_UNKNOWN)
                            ?: false
                    )
                }
            })

            //confirm cache was updated
            val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
            // assertTrue { cacheNoteThatWasInserted == null }
            assertNull(cacheNoteThatWasInserted)

            //confirm network was updated
            val confirmNetworkWasUpdatedWithNewNote = noteNetworkDataSource.searchNote(newNote)
            assertTrue { confirmNetworkWasUpdatedWithNewNote == null }
        }
    }
}