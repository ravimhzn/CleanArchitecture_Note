package com.ravimhzn.cleanarchitecture_notes.framework.presentation.notedetail.state

import android.os.Parcelable
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model.Note
import com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NoteDetailViewState(

    var note: Note? = null,

    var isUpdatePending: Boolean? = null

) : Parcelable, ViewState









