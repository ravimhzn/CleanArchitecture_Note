package com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.common

import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.CacheResponseHandler
import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.network.abstraction.NoteNetworkDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.util.safeApiCall
import com.ravimhzn.cleanarchitecture_notes.busniess.data.util.safeCacheCall
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.Note
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.state.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteNote<ViewState>(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    fun deleteNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<ViewState>?> =
        flow {
            val cacheResult = safeCacheCall(IO) {
                noteCacheDataSource.deleteNote(note.id)
            }

            val response = object : CacheResponseHandler<ViewState, Int>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
                override fun handleSuccess(resultObj: Int): DataState<ViewState> {
                    return if (resultObj > 0) {
                        DataState.data(
                            response = Response(
                                message = DELETE_NOTE_SUCCESS,
                                uiComponentType = UIComponentType.Toast(),
                                messageType = MessageType.Success()
                            ),
                            stateEvent = stateEvent,
                            data = null
                        )
                    } else {
                        DataState.data(
                            response = Response(
                                message = DELETE_NOTE_FAILURE,
                                uiComponentType = UIComponentType.Toast(),
                                messageType = MessageType.Error()
                            ),
                            stateEvent = stateEvent,
                            data = null
                        )
                    }
                }
            }.getResult()

            emit(response)

            updateNetwork(response?.stateMessage?.response?.message, note)
        }

    private suspend fun updateNetwork(message: String?, note: Note) {
        if (message.equals(DELETE_NOTE_SUCCESS)) {
            safeApiCall(IO) {
                noteNetworkDataSource.deleteNote(note.id)
            }

            //insert into 'deletes' node {Sync notes in multiple devices process.
            // When user logs in into another device we'll check for this node to keep it synced}
            safeApiCall(IO) {
                noteNetworkDataSource.insertDeletedNote(note)
            }
        }
    }


    companion object {
        val DELETE_NOTE_SUCCESS = "Successfully delete note"
        val DELETE_NOTE_FAILURE = "Failed to delete note"
    }
}