package com.example.mynotes

import android.app.Application
import com.example.mynotes.di.AppContainer

class MyNotesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}

