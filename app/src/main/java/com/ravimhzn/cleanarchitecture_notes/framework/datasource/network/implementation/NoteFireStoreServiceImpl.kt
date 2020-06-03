package com.ravimhzn.cleanarchitecture_notes.framework.datasource.network.implementation

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ravimhzn.cleanarchitecture_notes.busniess.domain.model.Note
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.network.abstraction.NoteFirestoreService
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.network.mapper.NetworkMapper
import com.ravimhzn.cleanarchitecture_notes.framework.datasource.network.model.NoteNetworkEntity
import com.ravimhzn.cleanarchitecture_notes.utils.cLog
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteFireStoreServiceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val networkMapper: NetworkMapper
) : NoteFirestoreService {

    companion object {
        const val NOTES_COLLECTION = "notes"
        const val USERS_COLLECTION = "users"
        const val DELETES_COLLECTION = "deletes"
        const val USER_ID = "phrimpiJrNSwTAiRei5SJUTrsk23" // hardcoded for single user
        const val EMAIL = "ravi.maharjan2018@gmail.com"
    }


    override suspend fun insertOrUpdateNote(note: Note) {
        val entity = networkMapper.mapToEntity(note)
        entity.updated_at = Timestamp.now()
        firestore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)
            .document(entity.id)
            .set(entity)
            .addOnFailureListener {
                cLog(it.message)
            }
            .await() //This is a coroutine function
    }


    override suspend fun insertOrUpdateNotes(notes: List<Note>) {
        if (notes.size > 500) {
            throw Exception("Cannot insert more than 500 notes at a time into firestore.")
        }

        val collectionRef = firestore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)

        firestore.runBatch { batch ->
            for (note in notes) {
                val entity = networkMapper.mapToEntity(note)
                entity.updated_at = Timestamp.now()
                val documentRef = collectionRef.document(note.id)
                batch.set(documentRef, entity)
            }
        }
            .addOnFailureListener {
                cLog(it.message)
            }.await()
    }

    override suspend fun deleteNote(primaryKey: String) {
        firestore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)
            .document(primaryKey)
            .delete()
            .addOnFailureListener {
                cLog(it.message)
            }
            .await()
    }

    override suspend fun insertDeletedNote(note: Note) {
        val entity = networkMapper.mapToEntity(note)
        entity.updated_at = Timestamp.now()
        firestore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)
            .document(entity.id)
            .set(entity)
            .addOnFailureListener {
                cLog(it.message)
            }
            .await() //This is a coroutine function
    }

    override suspend fun insertDeletedNotes(notes: List<Note>) {
        if (notes.size > 500) {
            throw Exception("Cannot insert more than 500 notes at a time into firestore.")
        }

        val collectionRef = firestore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)

        firestore.runBatch { batch ->
            for (note in notes) {
                val entity = networkMapper.mapToEntity(note)
                val documentRef = collectionRef.document(note.id)
                batch.set(documentRef, entity)
            }
        }
            .addOnFailureListener {
                cLog(it.message)
            }.await()
    }

    override suspend fun deleteDeletedNote(note: Note) {
        firestore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)
            .document(note.id)
            .delete()
            .addOnFailureListener {
                cLog(it.message)
            }
            .await()
    }

    override suspend fun getDeletedNote(): List<Note> {
        return networkMapper.entityListToNoteList(
            firestore
                .collection(DELETES_COLLECTION)
                .document(USER_ID)
                .collection(NOTES_COLLECTION)
                .get()
                .addOnFailureListener {
                    cLog(it.message)
                }
                .await().toObjects(NoteNetworkEntity::class.java)
        )
    }

    override suspend fun deleteAllNotes() {
        firestore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .delete()
            .addOnFailureListener {
                cLog(it.message)
            }

        firestore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .delete()
            .addOnFailureListener {
                cLog(it.message)
            }

    }

    override suspend fun searchNote(note: Note): Note? {
        return firestore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)
            .document(note.id)
            .get()
            .addOnFailureListener {
                cLog(it.message)
            }
            .await()
            .toObject(NoteNetworkEntity::class.java)?.let {
                networkMapper.mapFromEntity(it)
            }
    }

    override suspend fun getAllNotes(): List<Note> {
        return networkMapper.entityListToNoteList(
            firestore
                .collection(NOTES_COLLECTION)
                .document(USER_ID)
                .collection(NOTES_COLLECTION)
                .get()
                .addOnFailureListener {
                    cLog(it.message)
                }
                .await()
                .toObjects(NoteNetworkEntity::class.java)
        )
    }


}