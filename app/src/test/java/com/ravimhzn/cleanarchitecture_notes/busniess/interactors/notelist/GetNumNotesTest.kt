package com.ravimhzn.cleanarchitecture_notes.busniess.interactors.notelist

import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.network.abstraction.NoteNetworkDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.model.NoteFactory
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.state.DataState
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors.notelist.GetNumNotes.Companion.GET_NUM_NOTES_SUCCESS
import com.ravimhzn.cleanarchitecture_notes.di.DependencyContainer
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notelist.state.NoteListStateEvent
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * 1> getNumNotes_success_information()
 *  a> Get the number of notes in cache
 *  b> listen for GET_NUM_NOTE_SUCCESS from flow emission
 *  c> Compare with the number of notes in the fake data
 */

@InternalCoroutinesApi
class GetNumNotesTest {

    // system in test
    private val getNumNotes: GetNumNotes

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
        getNumNotes = GetNumNotes(
            noteCacheDataSource = noteCacheDataSource
        )
    }

    @Test
    fun getNumNotes_success_information() {
        runBlocking {
            var results: Int = 0
            //Get the number of notes in cache
            getNumNotes.getNumNotes(stateEvent = NoteListStateEvent.GetNumNotesInCacheEvent())
                //listen for GET_NUM_NOTE_SUCCESS from flow emission
                .collect(object : FlowCollector<DataState<NoteListViewState>?> {
                    override suspend fun emit(value: DataState<NoteListViewState>?) {
                        assertEquals(value?.stateMessage?.response?.message, GET_NUM_NOTES_SUCCESS)
                        value?.data?.numNotesInCache?.let {
                            results = it
                        }
                    }
                })

            //Compare with the number of notes in the fake data
            assertEquals(results, noteCacheDataSource.getNumNotes())

        }
    }

}