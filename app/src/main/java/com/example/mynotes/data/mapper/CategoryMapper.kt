package com.example.mynotes.data.mapper

import com.example.mynotes.data.local.entity.Category as CategoryEntity
import com.example.mynotes.domain.model.Category

fun CategoryEntity.toDomain(): Category =
    Category(id = id, userId = userId, name = name)

fun Category.toEntity(): CategoryEntity =
    CategoryEntity(id = id, userId = userId, name = name)

