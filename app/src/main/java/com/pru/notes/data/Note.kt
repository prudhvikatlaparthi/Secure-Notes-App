package com.pru.notes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.*

@Entity(tableName = "note_table")
data class Note(

    @ColumnInfo(name = "_notes_description")
    var notesDescription: String,

    @ColumnInfo(name = "_notes_time_date")
    var modifiedDate: Date,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "_notes_image")
    var noteImage: ByteArray?,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "_notes_hand_written")
    var notePaint: ByteArray?
) {

    //does it matter if these are private or not?
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}