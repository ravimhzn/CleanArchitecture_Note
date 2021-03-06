package com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model

import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.util.DateUtil
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteFactory @Inject constructor(private val dateUtil: DateUtil) {

    fun createSingleNote(id: String? = null, title: String, body: String? = null): Note {
        return Note(
            id = id ?: UUID.randomUUID().toString(),
            title = title,
            body = body ?: "",
            created_at = dateUtil.getCurrentTimestamp(),
            updated_at = dateUtil.getCurrentTimestamp()
        )
    }

    fun createNoteList(numNotes: Int): List<Note> {
        val list: ArrayList<Note> = ArrayList()
        for (i in 0 until numNotes) { // exclusive on upper bound
            list.add(
                createSingleNote(
                    id = UUID.randomUUID().toString(),
                    title = UUID.randomUUID().toString(),
                    body = UUID.randomUUID().toString()
                )
            )
        }
        return list
    }
}