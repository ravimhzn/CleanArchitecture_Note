package com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.common

import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.FORCE_DELETE_NOTE_EXCEPTION
import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.network.abstraction.NoteNetworkDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.Note
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.NoteFactory
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.state.DataState
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.common.DeleteNote.Companion.DELETE_NOTE_FAILURE
import com.ravimhzn.cleanarchitecture_notes.di.DependencyContainer
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notedetail.state.NoteDetailStateEvent
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notelist.state.NoteListStateEvent.DeleteNoteEvent
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

/*
Test cases:
1. deleteNote_success_confirmNetworkUpdated()
    a) delete a note
    b) check for success message from flow emission
    c) confirm note was deleted from "notes" node in network
    d) confirm note was added to "deletes" node in network
2. deleteNote_fail_confirmNetworkUnchanged()
    a) attempt to delete a note, fail since does not exist
    b) check for failure message from flow emission
    c) confirm network was not changed
3. throwException_checkGenericError_confirmNetworkUnchanged()
    a) attempt to delete a note, force an exception to throw
    b) check for failure message from flow emission
    c) confirm network was not changed
 */

@InternalCoroutinesApi
class DeleteNoteTest {
    // system in test
    private val deleteNoteSUT: DeleteNote<NoteListViewState>

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
        deleteNoteSUT = DeleteNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun deleteNote_success_confirmNetworkUpdated() = runBlocking {
        //delete a note
        val randomNoteToDelete = noteCacheDataSource.searchNotes(
            query = "",
            page = 1,
            filterAndOrder = ""
        )[0]
        deleteNoteSUT.deleteNote(
            note = randomNoteToDelete,
            stateEvent = DeleteNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    DeleteNote.DELETE_NOTE_SUCCESS
                )
            }
        })

        //confirm note was deleted from "notes" node in network
        val wasNoteDeleted = !noteNetworkDataSource.getAllNotes().contains(randomNoteToDelete)
        assertTrue(wasNoteDeleted)

        val wasDeletedNotesInserted =
            noteNetworkDataSource.getDeletedNotes().contains(randomNoteToDelete)
        assertTrue(wasDeletedNotesInserted)
    }

    /**
     * deleteNote_fail_confirmNetworkUnchanged()
    a) attempt to delete a note, fail since does not exist
    b) check for failure message from flow emission
    c) confirm network was not changed
     */

    @Test
    fun deleteNote_fail_confirmNetworkUnchanged() = runBlocking {
        //attempt to delete a note, fail since does not exist
        val deleteNote = noteFactory.createSingleNote(
            id = "99",
            title = "Nepalese Mountain Range",
            body = "It has 8 tallest mountains in the world that's among top 10"
        )
        deleteNoteSUT.deleteNote(
            note = deleteNote,
            stateEvent = NoteDetailStateEvent.UpdateNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                //check for failure message from flow emission
                assertEquals(value?.stateMessage?.response?.message, DELETE_NOTE_FAILURE)
            }
        })

        //confirm network was not changed
        val notesInCache = noteCacheDataSource.getNumNotes()
        val notesInNetwork = noteNetworkDataSource.getAllNotes()
        assertTrue { notesInCache == notesInNetwork.size }

        //Confirm note was not deleted from network
        val wasDeletedNotesNotInserted =
            !noteNetworkDataSource.getAllNotes().contains(deleteNote)
        assertTrue(wasDeletedNotesNotInserted)
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkUnchanged() = runBlocking {
        /**
         * throwException_checkGenericError_confirmNetworkUnchanged()
        a) attempt to delete a note, force an exception to throw
        b) check for failure message from flow emission
        c) confirm network was not changed
         */
        val noteToDelete = Note(
            id = FORCE_DELETE_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString()
        )

        deleteNoteSUT.deleteNote(
            stateEvent = DeleteNoteEvent(noteToDelete),
            note = noteToDelete
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                //check for failure message from flow emission
                assert(
                    value?.stateMessage?.response?.message?.contains(CACHE_ERROR_UNKNOWN) ?: false
                )
            }
        })

        //confirm network was not changed
        val notesInCache = noteCacheDataSource.getNumNotes()
        val notesInNetwork = noteNetworkDataSource.getAllNotes()
        assertTrue { notesInCache == notesInNetwork.size }

        //Confirm note was not deleted from network
        val wasDeletedNotesNotInserted =
            !noteNetworkDataSource.getAllNotes().contains(noteToDelete)
        assertTrue(wasDeletedNotesNotInserted)

    }
}