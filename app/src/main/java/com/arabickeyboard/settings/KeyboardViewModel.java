package com.arabickeyboard.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.arabickeyboard.data.model.AutoTypingEntry;
import com.arabickeyboard.data.model.Shortcut;
import com.arabickeyboard.data.repository.KeyboardRepository;

import java.util.List;

/**
 * ViewModel مشترك لجميع fragments في شاشة الإعدادات.
 * يستخدم Repository للوصول إلى البيانات بدون الاعتماد على Context مباشرةً.
 */
public class KeyboardViewModel extends AndroidViewModel {

    private final KeyboardRepository repository;

    /** LiveData: قائمة الاختصارات */
    public final LiveData<List<Shortcut>> shortcuts;

    /** LiveData: قائمة الكتابة التلقائية */
    public final LiveData<List<AutoTypingEntry>> autoTypingEntries;

    // ================== Constructor ==================

    public KeyboardViewModel(@NonNull Application application) {
        super(application);
        repository = new KeyboardRepository(application);
        shortcuts = repository.getAllShortcuts();
        autoTypingEntries = repository.getAllAutoTypingEntries();
    }

    // ================== Shortcut Operations ==================

    public void addShortcut(String shortcutText, String fullText) {
        Shortcut shortcut = new Shortcut(shortcutText.trim(), fullText.trim());
        repository.insertShortcut(shortcut);
    }

    public void updateShortcut(Shortcut shortcut, String newShortcutText, String newFullText) {
        shortcut.setShortcutText(newShortcutText.trim());
        shortcut.setFullText(newFullText.trim());
        repository.updateShortcut(shortcut);
    }

    public void deleteShortcut(Shortcut shortcut) {
        repository.deleteShortcut(shortcut);
    }

    // ================== Auto Typing Operations ==================

    public void addAutoTypingEntry(String triggerShortcut, String fullText, int speedMs) {
        AutoTypingEntry entry = new AutoTypingEntry(
                triggerShortcut.trim(),
                fullText.trim(),
                speedMs
        );
        repository.insertAutoTypingEntry(entry);
    }

    public void updateAutoTypingEntry(AutoTypingEntry entry,
                                       String newTrigger,
                                       String newText,
                                       int newSpeedMs) {
        entry.setTriggerShortcut(newTrigger.trim());
        entry.setFullText(newText.trim());
        entry.setTypingSpeedMs(newSpeedMs);
        repository.updateAutoTypingEntry(entry);
    }

    public void deleteAutoTypingEntry(AutoTypingEntry entry) {
        repository.deleteAutoTypingEntry(entry);
    }
}
