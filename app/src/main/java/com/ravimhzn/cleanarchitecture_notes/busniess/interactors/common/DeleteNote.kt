package com.ravimhzn.cleanarchitecture_notes.busniess.interactors.common

import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.CacheResponseHandler
import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.network.abstraction.NoteNetworkDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.util.safeApiCall
import com.ravimhzn.cleanarchitecture_notes.busniess.data.util.safeCacheCall
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.model.Note
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.state.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteNote<ViewState>(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    fun deleteNote(
        stateEvent: StateEvent,
        note: Note
    ): Flow<DataState<ViewState>?> = flow {

        val cacheResult = safeCacheCall(Dispatchers.IO) {
            noteCacheDataSource.deleteNote(note.id)
        }

        val response = object : CacheResponseHandler<ViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleSuccess(resultObj: Int): DataState<ViewState> = if (resultObj > 0) {
                DataState.data(
                    response = Response(
                        message = DELETE_NOTE_SUCCESS,
                        uiComponentType = UIComponentType.None(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            } else {
                DataState.data(
                    response = Response(
                        message = DELETE_NOTE_FAILURE,
                        uiComponentType = UIComponentType.Toast(),
                        messageType = MessageType.Error()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            }
        }.getResult()
        emit(response)

        updateNetwork(response?.stateMessage?.response?.message, note)
    }

    private suspend fun updateNetwork(message: String?, note: Note) {
        if(message.equals(DELETE_NOTE_SUCCESS)){
            //delete from 'notes' node
            safeApiCall(IO){
                noteNetworkDataSource.deleteNote(note.id)
            }
            //insert into 'deletes' node {Sync notes in multiple devices process. When user logs in into another device we'll check for this node to keep it synced}
            safeApiCall(IO){
                noteNetworkDataSource.insertDeletedNote(note)
            }
        }
    }

    companion object {
        val DELETE_NOTE_SUCCESS = "Successfully delete note"
        val DELETE_NOTE_FAILURE = "Failed to delete note"
    }
}