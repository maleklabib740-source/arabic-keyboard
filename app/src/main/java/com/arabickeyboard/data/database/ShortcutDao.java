package com.arabickeyboard.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.arabickeyboard.data.model.Shortcut;

import java.util.List;

/**
 * DAO لعمليات قاعدة البيانات الخاصة بالاختصارات النصية.
 */
@Dao
public interface ShortcutDao {

    /** إدراج اختصار جديد */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Shortcut shortcut);

    /** تحديث اختصار موجود */
    @Update
    void update(Shortcut shortcut);

    /** حذف اختصار */
    @Delete
    void delete(Shortcut shortcut);

    /** جلب جميع الاختصارات مرتبة حسب وقت الإنشاء تنازلياً */
    @Query("SELECT * FROM shortcuts ORDER BY createdAt DESC")
    LiveData<List<Shortcut>> getAllShortcuts();

    /**
     * جلب جميع الاختصارات بشكل متزامن (تُستخدم من خيط الخدمة للبحث السريع).
     * لا تستخدم هذا من الـ Main Thread.
     */
    @Query("SELECT * FROM shortcuts")
    List<Shortcut> getAllShortcutsSync();

    /**
     * البحث عن اختصار محدد بنصه (للتحقق من التكرار).
     * غير حساسة لحالة الأحرف.
     */
    @Query("SELECT * FROM shortcuts WHERE shortcutText = :shortcutText LIMIT 1")
    Shortcut findByShortcut(String shortcutText);

    /** حذف جميع الاختصارات (للاختبار) */
    @Query("DELETE FROM shortcuts")
    void deleteAll();

    /** عدد الاختصارات */
    @Query("SELECT COUNT(*) FROM shortcuts")
    int getCount();
}
