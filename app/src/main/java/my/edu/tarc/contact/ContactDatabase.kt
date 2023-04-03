package my.edu.tarc.contact

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Contact::class/*, More Entities*/), version = 1/*Must be changed every update*/, exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao

    companion object {
        //Singleton prevents multiple instances of database opening at the same time
        @Volatile
        private var INSTANCE: ContactDatabase? = null

        fun getDatabase(context: Context): ContactDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContactDatabase::class.java,
                    "contact_db"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}