package com.example.mynotes.data.mapper

import com.example.mynotes.data.local.entity.Note as NoteEntity
import com.example.mynotes.domain.model.Note

fun NoteEntity.toDomain(): Note =
    Note(
        id = id,
        userId = userId,
        categoryId = categoryId,
        title = title,
        detail = detail,
        titlePlain = titlePlain,
        detailPlain = detailPlain,
        isFavorite = isFavorite,
        isPinned = isPinned,
        isTrashed = isTrashed,
        trashedAt = trashedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun Note.toEntity(): NoteEntity =
    NoteEntity(
        id = id,
        userId = userId,
        categoryId = categoryId,
        title = title,
        detail = detail,
        titlePlain = titlePlain,
        detailPlain = detailPlain,
        isFavorite = isFavorite,
        isPinned = isPinned,
        isTrashed = isTrashed,
        trashedAt = trashedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

