package com.example.mynotes.data.mapper

import com.example.mynotes.data.local.entity.User as UserEntity
import com.example.mynotes.domain.model.User

fun UserEntity.toDomain(): User =
    User(
        id = id,
        username = username,
        password = password,
        fullName = fullName,
        email = email,
        phone = phone
    )

fun User.toEntity(): UserEntity =
    UserEntity(
        id = id,
        username = username,
        password = password,
        fullName = fullName,
        email = email,
        phone = phone
    )
