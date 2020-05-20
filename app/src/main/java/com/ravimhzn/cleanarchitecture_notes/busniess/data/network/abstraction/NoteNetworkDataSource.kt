package com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction

import com.ravimhzn.cleanarchitecture_notes.busniess.domain.model.Note

interface NoteNetworkDataSource {
    suspend fun insertOrUpdatNote(note: Note)
    suspend fun deleteNote(primaryKey: String)
    suspend fun insertDeletedNote(note: Note)
    suspend fun insertDeletedNotes(notes: List<Note>)
    suspend fun deleteDeletedNote(note: Note)
    suspend fun getDeletedNote(): List<Note>
    suspend fun deleteAllNotes()
    suspend fun searchNote(note: Note): Note?
    suspend fun getAllNotes(): List<Note>
    suspend fun insertOrUpdateNotes(notes: List<Note>)
}