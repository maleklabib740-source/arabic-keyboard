package com.arabickeyboard.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * نموذج البيانات لعملية الكتابة التلقائية.
 * المستخدم يكتب الاختصار ويضغط مسافة فيبدأ الكيبورد بكتابة النص حرفاً حرفاً.
 * بعد انتهاء الكتابة لا يُضغط زر الإرسال أبداً.
 */
@Entity(tableName = "auto_typing_entries")
public class AutoTypingEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    /** الاختصار الذي يُشغِّل الكتابة التلقائية (مثال: "123") */
    private String triggerShortcut;

    /** النص الكامل الذي سيُكتب تلقائياً */
    private String fullText;

    /**
     * سرعة الكتابة بالمللي ثانية بين كل حرف.
     * القيمة الصغيرة = كتابة سريعة، القيمة الكبيرة = كتابة بطيئة.
     * النطاق: 10ms (أسرع ما يمكن) إلى 200ms (بطيء)
     */
    private int typingSpeedMs;

    /** وقت الإنشاء */
    private long createdAt;

    // ================== Constructors ==================

    public AutoTypingEntry() {
        this.typingSpeedMs = 50; // 50ms افتراضياً
        this.createdAt = System.currentTimeMillis();
    }

    public AutoTypingEntry(String triggerShortcut, String fullText, int typingSpeedMs) {
        this.triggerShortcut = triggerShortcut;
        this.fullText = fullText;
        this.typingSpeedMs = typingSpeedMs;
        this.createdAt = System.currentTimeMillis();
    }

    // ================== Getters & Setters ==================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTriggerShortcut() {
        return triggerShortcut;
    }

    public void setTriggerShortcut(String triggerShortcut) {
        this.triggerShortcut = triggerShortcut;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public int getTypingSpeedMs() {
        return typingSpeedMs;
    }

    public void setTypingSpeedMs(int typingSpeedMs) {
        this.typingSpeedMs = typingSpeedMs;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * يُحوِّل قيمة الـ SeekBar إلى نص وصفي للسرعة.
     */
    public String getSpeedDescription() {
        if (typingSpeedMs <= 30) return "سريع جداً";
        if (typingSpeedMs <= 70) return "سريع";
        if (typingSpeedMs <= 120) return "متوسط";
        if (typingSpeedMs <= 160) return "بطيء";
        return "بطيء جداً";
    }

    @Override
    public String toString() {
        return "AutoTypingEntry{id=" + id
                + ", trigger='" + triggerShortcut + "'"
                + ", speed=" + typingSpeedMs + "ms}";
    }
}
