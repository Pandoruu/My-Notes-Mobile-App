package com.example.mynotes.data.local

import androidx.room.TypeConverter
import com.example.mynotes.domain.model.NoteBlock
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromBlocksJson(value: String?): List<NoteBlock> {
        return NoteBlockSerializer.fromJson(value)
    }

    @TypeConverter
    fun blocksToJson(blocks: List<NoteBlock>): String? {
        return NoteBlockSerializer.toJson(blocks)
    }
}