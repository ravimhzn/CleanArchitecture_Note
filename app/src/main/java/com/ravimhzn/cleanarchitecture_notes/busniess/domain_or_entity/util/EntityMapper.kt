package com.ravimhzn.cleanarchitecture_notes.busniess.domain_or_entity.util

interface EntityMapper<Entity, DomainModel> {
    fun mapFromEntity(entity: Entity): DomainModel

    fun mapToEntity(domainModel: DomainModel): Entity
}