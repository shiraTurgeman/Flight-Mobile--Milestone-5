package com.example.flightmobileapp

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database (entities = [(Url_Entity::class)],version = 4)

abstract class AppDataBase : RoomDatabase(){
    abstract fun urlDAO():Url_DAO



    companion object{
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Since we didn't alter the table, there's nothing else to do here.
            }
        }
        @Volatile
        private var INSTANCE : AppDataBase? = null
        fun getInstance(context: Context):AppDataBase{
            synchronized(this){
                var instance = INSTANCE
                if(instance==null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDataBase::class.java,
                        "url_data_database3"
                    ).addMigrations(MIGRATION_3_4).build()
                }
                return instance
            }
        }

    }
}