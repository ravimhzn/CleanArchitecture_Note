package com.ravimhzn.cleanarchitecture_notes.framework.datasource.network.implementation

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.Note
import com.ravimhzn.cleanarchitecture_notes.di.TestAppComponent
import com.ravimhzn.cleanarchitecture_notes.framework.BaseTest
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.NoteDataFactory
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.network.implementation.NoteFirestoreServiceImpl.Companion.NOTES_COLLECTION
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.network.implementation.NoteFirestoreServiceImpl.Companion.USER_ID
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.network.mapper.NetworkMapper
import com.ravimhzn.cleanarchitecture_notes.utils.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/*
    LEGEND:
    1. CBS = "Confirm by searching"

    Test cases:
    1. insert a single note, CBS
    2. update a random note, CBS
    3. insert a list of notes, CBS
    4. delete a single note, CBS
    5. insert a deleted note into "deletes" node, CBS
    6. insert a list of deleted notes into "deletes" node, CBS
    7. delete a 'deleted note' (note from "deletes" node). CBS

 */

@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class NoteFirestoreServiceImplTest : BaseTest() {
    //system in test
    private lateinit var noteFirestoreServiceImpl: NoteFirestoreServiceImpl

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var noteDataFactory: NoteDataFactory

    @Inject
    lateinit var networkMapper: NetworkMapper

    companion object {
        const val EMAIL = "robbiemhzn@gmail.com"
        const val PASSWORD = "password123"
    }

    override fun injectTest() {
        (application.appComponent as TestAppComponent).inject(this)
    }

    init {
        injectTest()
        signIn()
        insertTestData()
    }


    @Before
    fun before() {
        noteFirestoreServiceImpl = NoteFirestoreServiceImpl(
            firebaseAuth = FirebaseAuth.getInstance(),
            firestore = firestore,
            networkMapper = networkMapper
        )
    }

    private fun signIn() {
        runBlocking {
            firebaseAuth.signInWithEmailAndPassword(
                EMAIL,
                PASSWORD
            ).await()
        }
    }

    private fun insertTestData() {
        val entityList = networkMapper.noteListToEntityList(
            noteDataFactory.produceListOfNotes()
        )

        for (entity in entityList) {
            firestore
                .collection(NOTES_COLLECTION)
                .document(USER_ID)
                .collection(NOTES_COLLECTION)
                .document(entity.id)
                .set(entity)
        }
    }

    /**
     * NOTE: These test run alphabetically
     */
    @Test
    fun a_insertSingleNote_CBS() = runBlocking {
        val note = noteDataFactory.createSingleNote(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        )

        noteFirestoreServiceImpl.insertOrUpdateNote(note)

        val searchResult = noteFirestoreServiceImpl.searchNote(note)

        assertEquals(note, searchResult)
    }

    @Test
    fun b_queryAllNotes() = runBlocking {
        val notes = noteFirestoreServiceImpl.getAllNotes()
        printLogD("NoteFirestoreServiceImplTest", "notes: ${notes.size}")
        assertTrue {
            notes.size == 11 //Note size in our fake data is 10, however we've already inserted 1 note in our test above > a_insertSingleNote_CBS()
        }
    }

    @Test
    fun c_updateRandomSingleNote_CBS() {
        runBlocking {
            val searchResults = noteFirestoreServiceImpl.getAllNotes()

            val randomNote = searchResults.get(Random.nextInt(0, (searchResults.size-1) + 1))
            val update_title = UUID.randomUUID().toString()
            val update_body = UUID.randomUUID().toString()
            var updatedNote = noteDataFactory.createSingleNote(
                id = randomNote.id,
                title = update_title,
                body = update_body
            )
            //make the update
            noteFirestoreServiceImpl.insertOrUpdateNote(updatedNote)

            updatedNote = noteFirestoreServiceImpl.searchNote(updatedNote)!!

            assertEquals(update_title, updatedNote.title)
            assertEquals(update_body, updatedNote.body)
        }

    }

    @Test
    fun d_insertNoteList_CBS() = runBlocking {
        val list = noteDataFactory.produceListOfNotes()

        noteFirestoreServiceImpl.insertOrUpdateNotes(list)

        val searchResults = noteFirestoreServiceImpl.getAllNotes()

        assertTrue { searchResults.containsAll(list) }
    }

    @Test
    fun e_deleteSingleNote_CBS() = runBlocking {
        val noteList = noteFirestoreServiceImpl.getAllNotes()

        // choose one at random to delete
        val noteToDelete = noteList.get(Random.nextInt(0, noteList.size - 1) + 1)

        noteFirestoreServiceImpl.deleteNote(noteToDelete.id)

        // confirm it no longer exists in firestore
        val searchResults = noteFirestoreServiceImpl.getAllNotes()

        assertFalse { searchResults.contains(noteToDelete) }
    }

    @Test
    fun f_insertIntoDeletesNode_CBS() = runBlocking {
        val noteList = noteFirestoreServiceImpl.getAllNotes()

        // choose one at random to insert into "deletes" node
        val noteToDelete = noteList.get(Random.nextInt(0, noteList.size - 1) + 1)

        noteFirestoreServiceImpl.insertDeletedNote(noteToDelete)

        // confirm it is now in the "deletes" node
        val searchResults = noteFirestoreServiceImpl.getDeletedNote()

        assertTrue { searchResults.contains(noteToDelete) }
    }

    @Test
    fun g_insertListIntoDeletesNode_CBS() = runBlocking {
        val noteList = ArrayList(noteFirestoreServiceImpl.getAllNotes())

        // choose some random notes to add to "deletes" node
        val notesToDelete: ArrayList<Note> = ArrayList()

        // 1st
        var noteToDelete = noteList.get(Random.nextInt(0, noteList.size - 1) + 1)
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // 2nd
        noteToDelete = noteList.get(Random.nextInt(0, noteList.size - 1) + 1)
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // 3rd
        noteToDelete = noteList.get(Random.nextInt(0, noteList.size - 1) + 1)
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // 4th
        noteToDelete = noteList.get(Random.nextInt(0, noteList.size - 1) + 1)
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // insert into "deletes" node
        noteFirestoreServiceImpl
            .insertDeletedNotes(notesToDelete)

        // confirm the notes are in "deletes" node
        val searchResults = noteFirestoreServiceImpl.getDeletedNote()

        assertTrue { searchResults.containsAll(notesToDelete) }
    }

    @Test
    fun h_deleteDeletedNote_CBS() = runBlocking {
        val note = noteDataFactory.createSingleNote(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        )

        // insert into "deletes" node
        noteFirestoreServiceImpl.insertDeletedNote(note)

        // confirm note is in "deletes" node
        var searchResults = noteFirestoreServiceImpl.getDeletedNote()

        assertTrue { searchResults.contains(note) }

        // delete from "deletes" node
        noteFirestoreServiceImpl.deleteDeletedNote(note)

        // confirm note is deleted from "deletes" node
        searchResults = noteFirestoreServiceImpl.getDeletedNote()

        assertFalse { searchResults.contains(note) }
    }


}