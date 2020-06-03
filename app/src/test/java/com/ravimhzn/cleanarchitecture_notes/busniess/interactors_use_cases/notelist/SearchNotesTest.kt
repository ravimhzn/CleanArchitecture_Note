package com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.notelist

import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.model.Note
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.model.NoteFactory
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.state.DataState
import com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.notelist.SearchNotes.Companion.SEARCH_NOTES_SUCCESS
import com.ravimhzn.cleanarchitecture_notes.di.DependencyContainer
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.cache.database.ORDER_BY_ASC_DATE_UPDATED
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notelist.state.NoteListStateEvent.SearchNotesEvent
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
Test cases:
1. blankQuery_success_confirmNotesRetrieved()
    a) query with some default search options
    b) listen for SEARCH_NOTES_SUCCESS emitted from flow
    c) confirm notes were retrieved
    d) confirm notes in cache match with notes that were retrieved
2. randomQuery_success_confirmNoResults()
    a) query with something that will yield no results
    b) listen for SEARCH_NOTES_NO_MATCHING_RESULTS emitted from flow
    c) confirm nothing was retrieved
    d) confirm there is notes in the cache
3. searchNotes_fail_confirmNoResults()
    a) force an exception to be thrown
    b) listen for CACHE_ERROR_UNKNOWN emitted from flow
    c) confirm nothing was retrieved
    d) confirm there is notes in the cache
 */

@InternalCoroutinesApi
class SearchNotesTest {

    //System under Test (SUT)
    lateinit var searchNotes: SearchNotes
    lateinit var dependencyContainer: DependencyContainer
    lateinit var noteCacheDataSource: NoteCacheDataSource
    lateinit var noteFactory: NoteFactory

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build() // It will initiate the fake DataSet
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteFactory = dependencyContainer.noteFactory
        searchNotes = SearchNotes(
            noteCacheDataSource
        )
    }

    @Test
    fun blankQuery_success_confirmNotesRetrieved() {
        runBlocking {
            val query = ""
            var results: ArrayList<Note>? = null
            searchNotes.searchNotes(
                query = query,
                filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
                page = 1,
                stateEvent = SearchNotesEvent()
            ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
                override suspend fun emit(value: DataState<NoteListViewState>?) {
                    assertEquals(value?.stateMessage?.response?.message, SEARCH_NOTES_SUCCESS)
                    value?.data?.noteList?.let { list ->
                        results = ArrayList(list)
                    }
                }
            })

            //confirm notes were retrieved
            assertTrue { results != null }

            //confirm notes in cache match with notes that are retrieved
            val notesInCache = noteCacheDataSource.searchNotes(
                query = query,
                filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
                page = 1
            )
            assertTrue { results?.containsAll(notesInCache) ?: false }
        }
    }

}