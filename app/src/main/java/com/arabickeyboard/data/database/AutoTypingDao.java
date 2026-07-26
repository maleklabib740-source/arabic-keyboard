package com.arabickeyboard.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.arabickeyboard.data.model.AutoTypingEntry;

import java.util.List;

/**
 * DAO لعمليات قاعدة البيانات الخاصة بإدخالات الكتابة التلقائية.
 */
@Dao
public interface AutoTypingDao {

    /** إدراج إدخال جديد للكتابة التلقائية */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AutoTypingEntry entry);

    /** تحديث إدخال موجود */
    @Update
    void update(AutoTypingEntry entry);

    /** حذف إدخال */
    @Delete
    void delete(AutoTypingEntry entry);

    /** جلب جميع الإدخالات مرتبة حسب وقت الإنشاء تنازلياً */
    @Query("SELECT * FROM auto_typing_entries ORDER BY createdAt DESC")
    LiveData<List<AutoTypingEntry>> getAllEntries();

    /**
     * جلب جميع الإدخالات بشكل متزامن (للبحث السريع من خيط الخدمة).
     * لا تستخدم هذا من الـ Main Thread.
     */
    @Query("SELECT * FROM auto_typing_entries")
    List<AutoTypingEntry> getAllEntriesSync();

    /**
     * البحث عن إدخال بالاختصار المُشغِّل.
     */
    @Query("SELECT * FROM auto_typing_entries WHERE triggerShortcut = :trigger LIMIT 1")
    AutoTypingEntry findByTrigger(String trigger);

    /** حذف جميع الإدخالات */
    @Query("DELETE FROM auto_typing_entries")
    void deleteAll();

    /** عدد الإدخالات */
    @Query("SELECT COUNT(*) FROM auto_typing_entries")
    int getCount();
}
