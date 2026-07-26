package com.arabickeyboard.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * نموذج البيانات للاختصار النصي.
 * المستخدم يكتب الاختصار ويضغط مسافة فيتحول إلى الكلمة الأصلية.
 */
@Entity(tableName = "shortcuts")
public class Shortcut {

    @PrimaryKey(autoGenerate = true)
    private int id;

    /** الاختصار الذي يكتبه المستخدم (مثال: "هنف") */
    private String shortcutText;

    /** الكلمة الكاملة التي سيتوسع إليها الاختصار (مثال: "عامل إيه") */
    private String fullText;

    /** وقت الإنشاء */
    private long createdAt;

    // ================== Constructors ==================

    public Shortcut() {
        this.createdAt = System.currentTimeMillis();
    }

    public Shortcut(String shortcutText, String fullText) {
        this.shortcutText = shortcutText;
        this.fullText = fullText;
        this.createdAt = System.currentTimeMillis();
    }

    // ================== Getters & Setters ==================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortcutText() {
        return shortcutText;
    }

    public void setShortcutText(String shortcutText) {
        this.shortcutText = shortcutText;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Shortcut{id=" + id + ", shortcut='" + shortcutText + "', full='" + fullText + "'}";
    }
}
