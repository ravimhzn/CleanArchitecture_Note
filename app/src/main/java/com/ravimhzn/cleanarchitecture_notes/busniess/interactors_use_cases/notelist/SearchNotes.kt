package com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.notelist

import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.CacheResponseHandler
import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.util.safeCacheCall
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.Note
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.state.*
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchNotes(
    private val noteCacheDataSource: NoteCacheDataSource
) {

    fun searchNotes(
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        var updatedPage = page

        if (page <= 0) {
            updatedPage = 1
        }

        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.searchNotes(
                query = query,
                filterAndOrder = filterAndOrder,
                page = updatedPage
            )
        }

        val response = object : CacheResponseHandler<NoteListViewState, List<Note>>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleSuccess(resultObj: List<Note>): DataState<NoteListViewState> {
                var message: String? = SEARCH_NOTES_SUCCESS
                var uiComponentType: UIComponentType = UIComponentType.None()
                if (resultObj.isEmpty()) {
                    message = SEARCH_NOTES_NO_MATCHIN_RESULTS
                    uiComponentType = UIComponentType.Toast()
                }
                return DataState.data(
                    response = Response(
                        message = message,
                        uiComponentType = uiComponentType,
                        messageType = MessageType.Success()
                    ),
                    data = NoteListViewState(
                        noteList = ArrayList(resultObj)
                    ),
                    stateEvent = stateEvent
                )
            }
        }.getResult()

        emit(response)
    }


    companion object {
        val SEARCH_NOTES_SUCCESS = "Successfully retrieved list of notes."
        val SEARCH_NOTES_NO_MATCHIN_RESULTS = "There are no notes that match that query."
        val SEARCH_NOTES_FAILED = "Failed to retrieve the list of notes"
    }
}