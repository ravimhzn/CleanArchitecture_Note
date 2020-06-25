package com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Note(
    val id: String,
    val title: String,
    val body: String,
    val updated_at: String,
    val created_at: String
) : Parcelable
