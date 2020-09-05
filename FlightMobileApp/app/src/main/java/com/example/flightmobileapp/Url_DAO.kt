package com.example.flightmobileapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface Url_DAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveUrl(url:Url_Entity)

    @Query("UPDATE URL_data_table3 SET URL_Date = :date WHERE URL_NAME= :url")
    fun updateUrl(url:String, date:Long)

    @Query("SELECT Distinct URL_NAME FROM URL_data_table3 order by URL_Date desc Limit 5")
    fun getRecentUrl(): List<String>

    @Query("DELETE FROM URL_data_table3")
    fun deleteAll()

    @Query("select URL_NAME from URL_data_table3 order by URL_Date desc Limit 1")
    fun getLastURL():String
}