package com.miguel.apps.aiaialpha.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.miguel.apps.aiaialpha.data.converter.Converters
import com.miguel.apps.aiaialpha.data.dao.ChatDao
import com.miguel.apps.aiaialpha.data.model.ChatMessageConverters
import com.miguel.apps.aiaialpha.data.model.ChatMessageEntity
import com.miguel.apps.aiaialpha.data.model.GroupEntity
import com.miguel.apps.aiaialpha.data.model.ListConverters

@Database(
    entities = [GroupEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    Converters::class,
    ListConverters::class,
    ChatMessageConverters::class
)
abstract class ChatDatabase : RoomDatabase() {
    abstract val chatDao: ChatDao

    companion object {
        private const val DB_NAME = "chat_database.db"

        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getInstance(context: Context): ChatDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }

        private fun build(context: Context) =
            Room.databaseBuilder(context.applicationContext, ChatDatabase::class.java, DB_NAME)
                .build()
    }
}
