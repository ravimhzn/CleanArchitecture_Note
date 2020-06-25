package com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.implementation

import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.Note
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.cache.abstraction.NoteDaoService
import javax.inject.Singleton

@Singleton
class NoteCacheDataSourceImpl constructor(
    private val noteDaoService: NoteDaoService
) : NoteCacheDataSource {

    override suspend fun insertNote(note: Note): Long = noteDaoService.insertNote(note)

    override suspend fun deleteNote(primaryKey: String): Int = noteDaoService.deleteNote(primaryKey)

    override suspend fun deleteNotes(note: List<Note>): Int = noteDaoService.deleteNotes(note)
    override suspend fun updateNote(
        primaryKey: String,
        newTitle: String,
        newBody: String?,
        timestamp: String?
    ): Int {
        return noteDaoService.updateNote(primaryKey, newTitle, newBody, timestamp)
    }


    override suspend fun getAllNotes(): List<Note> {
        return noteDaoService.getAllNotes()
    }

    override suspend fun searchNotes(query: String, filterAndOrder: String, page: Int): List<Note> {
        TODO("CHECK FilterAndOrder and make query")
    }

    override suspend fun searchNoteById(primaryKey: String): Note? =
        noteDaoService.searchNoteById(primaryKey)

    override suspend fun getNumNotes(): Int = noteDaoService.getNumNotes()

    override suspend fun insertNotes(notes: List<Note>): LongArray =
        noteDaoService.insertNotes(notes)
}