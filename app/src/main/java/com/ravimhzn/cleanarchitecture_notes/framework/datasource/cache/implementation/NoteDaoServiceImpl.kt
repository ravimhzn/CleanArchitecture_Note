package com.ravimhzn.cleanarchitecture_notes.framework.datasource.cache.implementation

import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.Note
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.util.DateUtil
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.cache.abstraction.NoteDaoService
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.cache.database.NoteDao
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.cache.database.returnOrderedQuery
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.cache.mappers.CacheMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteDaoServiceImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val noteCacheMapper: CacheMapper,
    private val dateUtil: DateUtil
) : NoteDaoService {
    override suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(noteCacheMapper.mapToEntity(note))
    }

    override suspend fun insertNotes(notes: List<Note>): LongArray {
        return noteDao.insertNotes(noteCacheMapper.noteListToEntityList(notes))
    }

    override suspend fun searchNoteById(primaryKey: String): Note? {
        return noteDao.searchNoteById(primaryKey)?.let { noteCacheMapper.mapFromEntity(it) }
    }

    override suspend fun updateNote(
        primary: String,
        newTitle: String,
        newBody: String?,
        timestamp: String?
    ): Int {
        return noteDao.updateNote(
            primaryKey = primary,
            title = newTitle,
            body = newBody,
            updated_at = dateUtil.getCurrentTimestamp()
        )
    }

    override suspend fun deleteNote(primaryKey: String): Int {
        return noteDao.deleteNote(primaryKey)
    }

    override suspend fun deleteNotes(note: List<Note>): Int {
        val ids = note.mapIndexed { index, value -> value.id }
        return noteDao.deleteNotes(ids)
    }

    override suspend fun searchNotes(): List<Note> {
        return noteCacheMapper.entityListToNoteList(
            noteDao.searchNotes()
        )
    }

    override suspend fun getAllNotes(): List<Note> {
        return noteCacheMapper.entityListToNoteList(
            noteDao.getAllNotes()
        )
    }

    override suspend fun searchNotesOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Note> {
        return noteCacheMapper.entityListToNoteList(
            noteDao.searchNotesOrderByDateDESC(
                query = query,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun searchNotesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Note> {
        return noteCacheMapper.entityListToNoteList(
            noteDao.searchNotesOrderByDateASC(
                query = query,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun searchNotesOrderByTitleDESC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Note> {
        return noteCacheMapper.entityListToNoteList(
            noteDao.searchNotesOrderByTitleDESC(
                query = query,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun searchNotesOrderByTitleASC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Note> {
        return noteCacheMapper.entityListToNoteList(
            noteDao.searchNotesOrderByTitleASC(
                query = query,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun getNumNotes(): Int {
        return noteDao.getNumNotes()
    }

    override suspend fun returnOrderedQuery(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Note> {
        return noteCacheMapper.entityListToNoteList(
            noteDao.returnOrderedQuery(
                query = query,
                page = page,
                filterAndOrder = filterAndOrder
            )
        )
    }
}