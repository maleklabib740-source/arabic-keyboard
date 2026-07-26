package com.arabickeyboard.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.arabickeyboard.data.model.AutoTypingEntry;
import com.arabickeyboard.data.model.Shortcut;

/**
 * قاعدة البيانات الرئيسية للتطبيق (Room Database).
 * تحتوي على جدولين: shortcuts و auto_typing_entries.
 *
 * استخدام نمط Singleton لضمان وجود نسخة واحدة فقط من قاعدة البيانات.
 */
@Database(
        entities = {Shortcut.class, AutoTypingEntry.class},
        version = 1,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "arabic_keyboard.db";
    private static volatile AppDatabase INSTANCE;

    // ================== Abstract DAOs ==================

    public abstract ShortcutDao shortcutDao();

    public abstract AutoTypingDao autoTypingDao();

    // ================== Singleton ==================

    /**
     * الحصول على النسخة الوحيدة من قاعدة البيانات (Thread-safe).
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration() // عند الترقية نعيد البناء
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
