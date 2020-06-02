package com.ravimhzn.cleanarchitecture_notes.busniess.interactors.common

import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.network.abstraction.NoteNetworkDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.model.Note
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.model.NoteFactory
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.state.DataState
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors.common.DeleteNote.Companion.DELETE_NOTE_FAILURE
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors.common.DeleteNote.Companion.DELETE_NOTE_SUCCESS
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors.notelist.data.cache.FORCE_DELETE_NOTE_EXCEPTION
import com.ravimhzn.cleanarchitecture_notes.di.DependencyContainer
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notelist.state.NoteListStateEvent
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
    private val deleteNote: DeleteNote<NoteListViewState>

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
        deleteNote = DeleteNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun deleteNote_success_confirmNetworkUpdated() {
        runBlocking {

            val noteToDelete = noteCacheDataSource.searchNotes(
                query = "",
                filterAndOrder = "",
                page = 1
            ).get(0)
            //attempt to delete a note, fail since does not exist
            deleteNote.deleteNote(
                stateEvent = NoteListStateEvent.DeleteNoteEvent(),
                note = noteToDelete
            ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
                override suspend fun emit(value: DataState<NoteListViewState>?) {
                    // check for success message from flow emission
                    assertEquals(
                        value?.stateMessage?.response?.message,
                        DELETE_NOTE_SUCCESS
                    )
                }
            })
            val wasNoteDeleted = !noteNetworkDataSource.getAllNotes().contains(noteToDelete)
            assertTrue(wasNoteDeleted)

            val wasDeletedNotesInserted =
                noteNetworkDataSource.getDeletedNotes().contains(noteToDelete)
            assertTrue(wasDeletedNotesInserted)
        }
    }

    @Test
    fun deleteNote_fail_confirmNetworkUnchanged() {

        runBlocking {
            val noteToDelete = Note(
                id = UUID.randomUUID().toString(),
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString(),
                created_at = UUID.randomUUID().toString(),
                updated_at = UUID.randomUUID().toString()
            )
            deleteNote.deleteNote(
                stateEvent = NoteListStateEvent.DeleteNoteEvent(noteToDelete),
                note = noteToDelete
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
                !noteNetworkDataSource.getAllNotes().contains(noteToDelete)
            assertTrue(wasDeletedNotesNotInserted)

        }
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

        deleteNote.deleteNote(
            stateEvent = NoteListStateEvent.DeleteNoteEvent(noteToDelete),
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