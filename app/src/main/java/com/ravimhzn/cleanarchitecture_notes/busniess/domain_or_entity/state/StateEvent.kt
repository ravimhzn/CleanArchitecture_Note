package com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.state

interface StateEvent {

    fun errorInfo(): String

    fun eventName(): String

    fun shouldDisplayProgressBar(): Boolean
}