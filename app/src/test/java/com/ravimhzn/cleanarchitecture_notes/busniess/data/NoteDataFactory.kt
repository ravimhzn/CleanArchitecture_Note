package com.ravimhzn.cleanarchitecture_notes.busniess.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.Note

class NoteDataFactory(
    private val testClassLoader: ClassLoader
) {
    fun produceListOfNotes(): List<Note> {
        val notes: List<Note> = Gson().fromJson(
            getNotesFromFile("notelist.json"),
            object : TypeToken<List<Note>>() {}.type
        )
        return notes
    }

    fun produceHashMapOfNotes(noteList: List<Note>): HashMap<String, Note> {
        val map = HashMap<String, Note>()
        for (note in noteList) {
            map.put(note.id, note)
        }
        return map
    }

    fun produceEmptyListOfNotes(): List<Note> {
        return ArrayList()
    }

    fun getNotesFromFile(fileName: String): String {
        return testClassLoader.getResource(fileName).readText() //easy way to read local json files
    }
}