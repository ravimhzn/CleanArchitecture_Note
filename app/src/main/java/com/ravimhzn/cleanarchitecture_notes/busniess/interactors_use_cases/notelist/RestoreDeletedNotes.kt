package com.ravimhzn.cleanarchitecture_notes.busniess.interactors_use_cases.notelist

import com.ravimhzn.cleanarchitecture_notes.busniess.data.cache.abstraction.NoteCacheDataSource
import com.ravimhzn.cleanarchitecture_notes.busniess.data.network.abstraction.NoteNetworkDataSource

class RestoreDeletedNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
){}