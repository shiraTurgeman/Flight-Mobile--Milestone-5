package com.example.flightmobileapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date
import java.time.LocalDateTime

@Entity(tableName = "URL_data_table3")

class Url_Entity {

    @PrimaryKey(autoGenerate = true)
    var url_id:Int=0
    @ColumnInfo (name="URL_NAME")
    var url_name:String=""

    @ColumnInfo (name="URL_Date")
    var URL_Date: Long =  0
}