package com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.notedetail

import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.CacheErrors
import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.FORCE_UPDATE_NOTE_EXCEPTION
import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.network.abstraction.NoteNetworkDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.NoteFactory
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.state.DataState
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.notedetail.UpdateNote.Companion.UPDATE_NOTE_FAILED
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.notedetail.UpdateNote.Companion.UPDATE_NOTE_SUCCESS
import com.ravimhzn.cleanarchitecture_notes.di.DependencyContainer
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notedetail.state.NoteDetailStateEvent.UpdateNoteEvent
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notedetail.state.NoteDetailViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@InternalCoroutinesApi
class UpdateNoteTest {

    // system in test
    private val updateNoteSUT: UpdateNote

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
        updateNoteSUT = UpdateNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun updateNote_success_confirmNetworkAndCacheUpdated() = runBlocking {
        //select a random note from the cache
        val randomNote = noteCacheDataSource.searchNotes(
            query = "",
            filterAndOrder = "",
            page = 1
        )[0]
        //update that note
        val updatedNote = noteFactory.createSingleNote(
            id = randomNote.id,
            title = "Nepalese Mountain Range",
            body = "It has 8 tallest mountains in the world that's among top 10"
        )
        updateNoteSUT.updateNote(
            note = updatedNote,
            stateEvent = UpdateNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteDetailViewState>?> {
            override suspend fun emit(value: DataState<NoteDetailViewState>?) {
                //confirm UPDATE_NOTE_SUCCESS msg is emitted from flow
                println(value?.stateMessage?.response?.message)
                assertEquals(value?.stateMessage?.response?.message, UPDATE_NOTE_SUCCESS)
            }
        })

        //confirm note is updated in cache
        val isUpdatedInCache = noteCacheDataSource.searchNoteById(updatedNote.id)
        assertTrue { isUpdatedInCache == updatedNote }

        //confirm note is updated in network
        val isUpdateInNetwork = noteNetworkDataSource.searchNote(updatedNote)
        assertTrue { isUpdateInNetwork == updatedNote }
    }


    @Test
    fun updateNote_fail_confirmNetworkAndCacheUnchanged() = runBlocking {
        //attempt to update a note, fail since does not exist
        val updatedNote = noteFactory.createSingleNote(
            id = "99",
            title = "Nepalese Mountain Range",
            body = "It has 8 tallest mountains in the world that's among top 10"
        )
        updateNoteSUT.updateNote(
            note = updatedNote,
            stateEvent = UpdateNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteDetailViewState>?> {
            override suspend fun emit(value: DataState<NoteDetailViewState>?) {
                //check for failure message from flow emission
                assertEquals(value?.stateMessage?.response?.message, UPDATE_NOTE_FAILED)
            }
        })

        //confirm note is not updated in cache
        val isUpdatedInCache = noteCacheDataSource.searchNoteById(updatedNote.id)
        assertTrue { isUpdatedInCache == null }

        //confirm note is not updated in network
        val isUpdateInNetwork = noteNetworkDataSource.searchNote(updatedNote)
        assertTrue { isUpdateInNetwork == null }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {
        //attempt to update a note, force an exception to throw
        val updatedNote = noteFactory.createSingleNote(
            id = FORCE_UPDATE_NOTE_EXCEPTION,
            title = "Nepalese Mountain Range",
            body = "It has 8 tallest mountains in the world that's among top 10"
        )
        updateNoteSUT.updateNote(
            note = updatedNote,
            stateEvent = UpdateNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteDetailViewState>?> {
            override suspend fun emit(value: DataState<NoteDetailViewState>?) {
                //check for failure message from flow emission
                println(value?.stateMessage?.response?.message)
                assert(
                    value?.stateMessage?.response?.message?.contains(CacheErrors.CACHE_ERROR_UNKNOWN)
                        ?: false
                )
            }
        })

        //confirm note is updated in cache
        val isUpdatedInCache = noteCacheDataSource.searchNoteById(updatedNote.id)
        assertTrue { isUpdatedInCache == null }

        //confirm note is updated in network
        val isUpdateInNetwork = noteNetworkDataSource.searchNote(updatedNote)
        assertTrue { isUpdateInNetwork == null }
    }
}