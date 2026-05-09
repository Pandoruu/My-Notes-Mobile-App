package com.example.mynotes.domain.model

data class User(
    val id: Int = 0,
    val username: String,
    val password: String,
    val fullName: String = "",
    val email: String = "",
    val phone: String = ""
)
