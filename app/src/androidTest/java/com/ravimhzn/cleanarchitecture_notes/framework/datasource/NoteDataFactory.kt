package com.ravimhzn.cleanarchitecture_notes.framework.datasource

import android.app.Application
import android.content.res.AssetManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.Note
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.NoteFactory
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NoteDataFactory @Inject constructor(
    private val application: Application,
    private val noteFactory: NoteFactory
) {
    private fun readJSONFromAsset(fileName: String): String? {
        var json: String? = null
        json = try {
            val inputStream: InputStream = (application.assets as AssetManager)
                .open(fileName)
            inputStream.bufferedReader().use { it.readText() } //Alternative way to read json files
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return json
    }

    fun produceListOfNotes(): List<Note> {
        val notes: List<Note> = Gson().fromJson(
            readJSONFromAsset("notelist.json"),
            object : TypeToken<List<Note>>() {}.type
        )
        return notes
    }

    fun produceEmptyListOfNotes(): List<Note> {
        return ArrayList()
    }

    fun createSingleNote(
        id: String? = null,
        title: String,
        body: String? = null
    ) = noteFactory.createSingleNote(id, title, body)

    fun createNoteList(numNotes: Int) = noteFactory.createNoteList(numNotes)
}