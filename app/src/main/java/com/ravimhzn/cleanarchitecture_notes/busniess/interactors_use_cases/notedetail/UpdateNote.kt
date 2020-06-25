package com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.notedetail

import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.CacheResponseHandler
import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.network.abstraction.NoteNetworkDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.util.safeApiCall
import com.ravimhzn.cleanarchitecture_notes.busniess.data.util.safeCacheCall
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.Note
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.state.*
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notedetail.state.NoteDetailViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    fun updateNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<NoteDetailViewState>?> = flow {
        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.updateNote(
                primaryKey = note.id,
                newTitle = note.title,
                newBody = note.body,
                timestamp = ""
            )
        }

        val response = object : CacheResponseHandler<NoteDetailViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleSuccess(resultObj: Int): DataState<NoteDetailViewState> {
                return if (resultObj > 0) {
                    DataState.data(
                        response = Response(
                            message = UPDATE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        stateEvent = stateEvent,
                        data = null
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = UPDATE_NOTE_FAILED,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Error()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()

        emit(response)

        updateNetwork(response?.stateMessage?.response?.message, note)
    }

    private suspend fun updateNetwork(
        message: String?,
        note: Note
    ) {
        if (message.equals(UPDATE_NOTE_SUCCESS)) {
            safeApiCall(IO) {
                noteNetworkDataSource.insertOrUpdateNote(note)
            }
        }
    }

    companion object {
        val UPDATE_NOTE_SUCCESS = "Successfully updated note."
        val UPDATE_NOTE_FAILED = "Failed to update note."
        val UPDATE_NOTE_FAILED_PK = "Update failed. Note is missing primary key."

    }


}

