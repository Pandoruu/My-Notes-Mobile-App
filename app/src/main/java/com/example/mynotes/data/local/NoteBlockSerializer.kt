package com.example.mynotes.data.local

import com.example.mynotes.domain.model.NoteBlock
import com.example.mynotes.domain.model.TextAlignment
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Gson TypeAdapter để serialize/deserialize List<NoteBlock> thành JSON string.
 * Dùng field "type" làm discriminator để phân biệt các subtype.
 */
object NoteBlockSerializer {

    private val gsonAdapter: Gson by lazy {
        GsonBuilder()
            .registerTypeHierarchyAdapter(NoteBlock::class.java, NoteBlockTypeAdapter())
            .create()
    }

    fun toJson(blocks: List<NoteBlock>): String = gsonAdapter.toJson(blocks)

    fun fromJson(json: String?): List<NoteBlock> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<NoteBlock>>() {}.type
            gsonAdapter.fromJson<List<NoteBlock>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private class NoteBlockTypeAdapter : JsonSerializer<NoteBlock>, JsonDeserializer<NoteBlock> {

        override fun serialize(src: NoteBlock, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val obj = JsonObject()
            when (src) {
                is NoteBlock.TextBlock -> {
                    obj.addProperty("type", "text")
                    obj.addProperty("id", src.id)
                    obj.addProperty("text", src.text)
                    obj.addProperty("htmlText", src.htmlText)
                    obj.addProperty("alignment", src.alignment.name)
                }
                is NoteBlock.CheckboxBlock -> {
                    obj.addProperty("type", "checkbox")
                    obj.addProperty("id", src.id)
                    obj.addProperty("text", src.text)
                    obj.addProperty("isChecked", src.isChecked)
                }
                is NoteBlock.ImageBlock -> {
                    obj.addProperty("type", "image")
                    obj.addProperty("id", src.id)
                    obj.addProperty("filePath", src.filePath)
                }
                is NoteBlock.AudioBlock -> {
                    obj.addProperty("type", "audio")
                    obj.addProperty("id", src.id)
                    obj.addProperty("filePath", src.filePath)
                    obj.addProperty("durationMs", src.durationMs)
                }
            }
            return obj
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): NoteBlock {
            val obj = json.asJsonObject
            return when (obj.get("type")?.asString) {
                "text" -> NoteBlock.TextBlock(
                    id = obj.get("id")?.asString ?: java.util.UUID.randomUUID().toString(),
                    text = obj.get("text")?.asString ?: "",
                    htmlText = obj.get("htmlText")?.asString ?: "",
                    alignment = obj.get("alignment")?.asString
                        ?.let { runCatching { TextAlignment.valueOf(it) }.getOrNull() }
                        ?: TextAlignment.LEFT
                )
                "checkbox" -> NoteBlock.CheckboxBlock(
                    id = obj.get("id")?.asString ?: java.util.UUID.randomUUID().toString(),
                    text = obj.get("text")?.asString ?: "",
                    isChecked = obj.get("isChecked")?.asBoolean ?: false
                )
                "image" -> NoteBlock.ImageBlock(
                    id = obj.get("id")?.asString ?: java.util.UUID.randomUUID().toString(),
                    filePath = obj.get("filePath")?.asString ?: ""
                )
                "audio" -> NoteBlock.AudioBlock(
                    id = obj.get("id")?.asString ?: java.util.UUID.randomUUID().toString(),
                    filePath = obj.get("filePath")?.asString ?: "",
                    durationMs = obj.get("durationMs")?.asLong ?: 0L
                )
                else -> NoteBlock.TextBlock()
            }
        }
    }
}
