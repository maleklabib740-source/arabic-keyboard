package com.arabickeyboard.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.arabickeyboard.data.database.AppDatabase;
import com.arabickeyboard.data.database.AutoTypingDao;
import com.arabickeyboard.data.database.ShortcutDao;
import com.arabickeyboard.data.model.AutoTypingEntry;
import com.arabickeyboard.data.model.Shortcut;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository: يفصل طبقة البيانات عن طبقة الـ ViewModel وخدمة الكيبورد.
 * جميع العمليات التي تعدّل البيانات تُنفَّذ في خيط خلفي (background thread).
 */
public class KeyboardRepository {

    private final ShortcutDao shortcutDao;
    private final AutoTypingDao autoTypingDao;

    /** LiveData: قوائم مباشرة يراقبها الـ UI */
    private final LiveData<List<Shortcut>> allShortcuts;
    private final LiveData<List<AutoTypingEntry>> allAutoTypingEntries;

    /** خيط خلفي للعمليات على قاعدة البيانات */
    private final ExecutorService executor;

    // ================== Constructor ==================

    public KeyboardRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        shortcutDao = db.shortcutDao();
        autoTypingDao = db.autoTypingDao();
        allShortcuts = shortcutDao.getAllShortcuts();
        allAutoTypingEntries = autoTypingDao.getAllEntries();
        executor = Executors.newSingleThreadExecutor();
    }

    // ================== Shortcut Operations ==================

    /** إضافة اختصار جديد */
    public void insertShortcut(Shortcut shortcut) {
        executor.execute(() -> shortcutDao.insert(shortcut));
    }

    /** تحديث اختصار */
    public void updateShortcut(Shortcut shortcut) {
        executor.execute(() -> shortcutDao.update(shortcut));
    }

    /** حذف اختصار */
    public void deleteShortcut(Shortcut shortcut) {
        executor.execute(() -> shortcutDao.delete(shortcut));
    }

    /** LiveData: قائمة الاختصارات */
    public LiveData<List<Shortcut>> getAllShortcuts() {
        return allShortcuts;
    }

    /**
     * جلب جميع الاختصارات بشكل متزامن (يُستخدَم من خيط الخدمة لبحث الاختصارات).
     * يجب استدعاؤه من خيط خلفي وليس Main Thread.
     */
    public List<Shortcut> getAllShortcutsSync() {
        return shortcutDao.getAllShortcutsSync();
    }

    // ================== Auto Typing Operations ==================

    /** إضافة إدخال كتابة تلقائية */
    public void insertAutoTypingEntry(AutoTypingEntry entry) {
        executor.execute(() -> autoTypingDao.insert(entry));
    }

    /** تحديث إدخال كتابة تلقائية */
    public void updateAutoTypingEntry(AutoTypingEntry entry) {
        executor.execute(() -> autoTypingDao.update(entry));
    }

    /** حذف إدخال كتابة تلقائية */
    public void deleteAutoTypingEntry(AutoTypingEntry entry) {
        executor.execute(() -> autoTypingDao.delete(entry));
    }

    /** LiveData: قائمة الكتابة التلقائية */
    public LiveData<List<AutoTypingEntry>> getAllAutoTypingEntries() {
        return allAutoTypingEntries;
    }

    /**
     * جلب جميع إدخالات الكتابة التلقائية بشكل متزامن.
     * يجب استدعاؤه من خيط خلفي.
     */
    public List<AutoTypingEntry> getAllAutoTypingEntriesSync() {
        return autoTypingDao.getAllEntriesSync();
    }

    // ================== Utility ==================

    /**
     * البحث السريع في الاختصارات - يُستخدَم من خيط الخدمة.
     * يبحث عن الكلمة المكتوبة في قائمة الاختصارات، ويُرجع الكلمة الموسَّعة إن وُجدت.
     *
     * @param typedWord الكلمة التي كتبها المستخدم
     * @param shortcuts قائمة الاختصارات المُخزَّنة محلياً (لتجنب استدعاء DB كل مرة)
     * @return الكلمة الكاملة إن وُجد اختصار، أو null
     */
    public static String findShortcutExpansion(String typedWord, List<Shortcut> shortcuts) {
        if (typedWord == null || typedWord.isEmpty() || shortcuts == null) return null;
        for (Shortcut shortcut : shortcuts) {
            if (typedWord.equals(shortcut.getShortcutText())) {
                return shortcut.getFullText();
            }
        }
        return null;
    }

    /**
     * البحث السريع في إدخالات الكتابة التلقائية.
     *
     * @param typedWord الكلمة المكتوبة
     * @param entries قائمة الإدخالات المُخزَّنة محلياً
     * @return AutoTypingEntry إن وُجد تطابق، أو null
     */
    public static AutoTypingEntry findAutoTypingEntry(String typedWord, List<AutoTypingEntry> entries) {
        if (typedWord == null || typedWord.isEmpty() || entries == null) return null;
        for (AutoTypingEntry entry : entries) {
            if (typedWord.equals(entry.getTriggerShortcut())) {
                return entry;
            }
        }
        return null;
    }
}
