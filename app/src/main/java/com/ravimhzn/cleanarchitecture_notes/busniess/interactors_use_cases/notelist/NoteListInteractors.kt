package com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.notelist

import com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.common.DeleteNote
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notelist.state.NoteListViewState

class NoteListInteractors(
    val insertNewNote: InsertNewNote,
    val deleteNote: DeleteNote<NoteListViewState>,
    val searchNotes: SearchNotes,
    val getNumNotes: GetNumNotes,
    val restoreDeletedNotes: RestoreDeletedNotes,
    val deleteMultipleNotes: DeleteMultipleNotes
)