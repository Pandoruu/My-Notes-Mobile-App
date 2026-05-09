package com.example.mynotes.domain.model

import java.util.Date

data class Note(
    val id: Int = 0,
    val userId: Int,
    val categoryId: Int? = null,
    val title: String,
    val detail: String? = null,
    val titlePlain: String? = null,
    val detailPlain: String? = null,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val isTrashed: Boolean = false,
    val trashedAt: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

