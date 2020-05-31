package com.ravimhzn.cleanarchitecture_notes.busniess.domain.state

interface StateEvent {

    fun errorInfo(): String

    fun eventName(): String

    fun shouldDisplayProgressBar(): Boolean
}