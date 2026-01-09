package com.romreviewertools.noteitup.data.import.joplin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JoplinNote(
    val id: String,
    val title: String,
    val body: String,
    @SerialName("created_time") val createdTime: Long,
    @SerialName("updated_time") val updatedTime: Long,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("is_todo") val isTodo: Int = 0,
    @SerialName("todo_completed") val todoCompleted: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("source_url") val sourceUrl: String? = null,
    val type_: Int = 1
)

@Serializable
data class JoplinNotebook(
    val id: String,
    val title: String,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("created_time") val createdTime: Long,
    @SerialName("updated_time") val updatedTime: Long? = null,
    val type_: Int = 2
)

@Serializable
data class JoplinTag(
    val id: String,
    val title: String,
    @SerialName("created_time") val createdTime: Long? = null,
    val type_: Int = 5
)

@Serializable
data class JoplinNoteTag(
    val id: String,
    @SerialName("note_id") val noteId: String,
    @SerialName("tag_id") val tagId: String,
    val type_: Int = 6
)

@Serializable
data class JoplinResource(
    val id: String,
    val title: String,
    val filename: String? = null,
    val mime: String? = null,
    @SerialName("created_time") val createdTime: Long,
    val type_: Int = 4
)
