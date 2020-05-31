package com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction

import com.ravimhzn.cleanarchitecture_notes.busniess.domain.model.Note

interface NoteCacheDataSource {

    suspend fun insertNote(note: Note): Long
    suspend fun deleteNote(primaryKey: String): Int
    suspend fun deleteNotes(note: List<Note>): Int
    suspend fun updateNote(primaryKey: String, newTitle: String, newBody: String?): Int
    suspend fun searchNotes(query: String, filterAndOrder: String, page: Int): List<Note>
    suspend fun searchNoteById(primaryKey: String): Note?
    suspend fun getNumNotes(): Int
    suspend fun insertNotes(notes: List<Note>): LongArray

}