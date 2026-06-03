package com.example.clearstackprototype1

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    private var database: ClearStackDatabase? = null

    fun getDatabase(
        context: Context
    ): ClearStackDatabase{
        return database ?: synchronized(this){
            val instance = Room.databaseBuilder(
                context.applicationContext,
                ClearStackDatabase::class.java,
                "clearstack_database"
            ).fallbackToDestructiveMigration().build()
            database = instance
            instance
        }
    }
}