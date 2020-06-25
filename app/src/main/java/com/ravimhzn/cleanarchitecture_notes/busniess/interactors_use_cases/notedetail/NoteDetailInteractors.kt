package com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.notedetail

import com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.common.DeleteNote
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.notedetail.state.NoteDetailViewState

class NoteDetailInteractors(
    val deleteNote: DeleteNote<NoteDetailViewState>,
    val updateNote: UpdateNote? = null
) {}