package com.arabickeyboard.keyboard;

import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.arabickeyboard.R;
import com.arabickeyboard.data.database.AppDatabase;
import com.arabickeyboard.data.model.AutoTypingEntry;
import com.arabickeyboard.data.model.Shortcut;
import com.arabickeyboard.data.repository.KeyboardRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * الخدمة الرئيسية للوحة المفاتيح - تمتد من InputMethodService.
 *
 * المميزات المدمجة:
 * 1. لوحة مفاتيح عربية وإنجليزية ورموز
 * 2. اختصارات النصوص: عند كتابة اختصار + مسافة يُستبدل بالكلمة الكاملة
 * 3. استبدال المسافة: يمكن استبدال مفتاح المسافة برمز مخصص
 * 4. الكتابة التلقائية: عند كتابة اختصار + مسافة يكتب النص حرفاً حرفاً
 */
public class ArabicKeyboardService extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    // ================== Constants ==================

    /** كود مخصص للتبديل بين العربية والإنجليزية */
    private static final int KEY_SWITCH_LANGUAGE = -10;

    /** كود مخصص للتبديل إلى الرموز الثانية */
    private static final int KEY_SWITCH_SYMBOLS2 = -11;

    /** SharedPreferences اسم الملف */
    public static final String PREFS_NAME = "keyboard_prefs";
    public static final String PREF_SPACE_REPLACEMENT_ENABLED = "space_replacement_enabled";
    public static final String PREF_SPACE_REPLACEMENT_CHAR = "space_replacement_char";

    // ================== Keyboard Modes ==================

    private static final int MODE_ARABIC   = 0;
    private static final int MODE_ENGLISH  = 1;
    private static final int MODE_SYMBOLS  = 2;
    private static final int MODE_SYMBOLS2 = 3;

    // ================== Fields ==================

    private KeyboardView keyboardView;
    private Keyboard arabicKeyboard;
    private Keyboard englishKeyboard;
    private Keyboard symbolsKeyboard;
    private Keyboard symbols2Keyboard;

    /** الوضع الحالي للوحة المفاتيح */
    private int currentMode = MODE_ARABIC;

    /** هل Shift مفعَّل؟ */
    private boolean isShifted = false;

    /** بفر الكلمة الحالية التي يكتبها المستخدم (لفحص الاختصارات) */
    private final StringBuilder currentWordBuffer = new StringBuilder();

    /** Handler للكتابة التلقائية */
    private final Handler autoTypingHandler = new Handler(Looper.getMainLooper());

    /** هل الكتابة التلقائية جارية الآن؟ */
    private boolean isAutoTyping = false;

    /** مؤشر الحرف الحالي أثناء الكتابة التلقائية */
    private int autoTypingIndex = 0;

    /** النص الجاري كتابته تلقائياً */
    private String autoTypingText = null;

    /** سرعة الكتابة التلقائية الحالية */
    private int autoTypingSpeedMs = 50;

    /** Executor للبحث في قاعدة البيانات من خيط خلفي */
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    /** نسخ مؤقتة من البيانات محلياً لتسريع الأداء */
    private List<Shortcut> cachedShortcuts = null;
    private List<AutoTypingEntry> cachedAutoTypingEntries = null;

    /** SharedPreferences للإعدادات */
    private SharedPreferences prefs;

    // ================== Lifecycle ==================

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // تحميل البيانات من قاعدة البيانات في الخلفية
        loadDataInBackground();
    }

    @Override
    public View onCreateInputView() {
        // تضخيم (inflate) الـ Layout الخاص بلوحة المفاتيح
        View keyboardLayout = getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboardView = keyboardLayout.findViewById(R.id.keyboard_view);

        // إنشاء لوحات المفاتيح
        arabicKeyboard   = new Keyboard(this, R.xml.keyboard_arabic);
        englishKeyboard  = new Keyboard(this, R.xml.keyboard_english);
        symbolsKeyboard  = new Keyboard(this, R.xml.keyboard_symbols);
        symbols2Keyboard = new Keyboard(this, R.xml.keyboard_symbols2);

        // تعيين لوحة المفاتيح الافتراضية
        keyboardView.setKeyboard(arabicKeyboard);
        keyboardView.setOnKeyboardActionListener(this);
        keyboardView.setPreviewEnabled(true);

        return keyboardLayout;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        // مسح البفر عند بدء الإدخال في حقل جديد
        currentWordBuffer.setLength(0);

        // إيقاف أي كتابة تلقائية جارية
        stopAutoTyping();

        // إعادة تحميل البيانات لضمان التحديث
        loadDataInBackground();
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        stopAutoTyping();
        currentWordBuffer.setLength(0);
    }

    // ================== Data Loading ==================

    /**
     * تحميل الاختصارات وإدخالات الكتابة التلقائية من قاعدة البيانات في خيط خلفي.
     * يُخزَّن محلياً لتسريع عملية البحث أثناء الكتابة.
     */
    private void loadDataInBackground() {
        backgroundExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            cachedShortcuts = db.shortcutDao().getAllShortcutsSync();
            cachedAutoTypingEntries = db.autoTypingDao().getAllEntriesSync();
        });
    }

    // ================== Key Press Handling ==================

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        // إذا كانت الكتابة التلقائية جارية، نتجاهل أي ضغطات مفاتيح
        if (isAutoTyping) {
            return;
        }

        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        switch (primaryCode) {

            case Keyboard.KEYCODE_DELETE:
                handleDelete(ic);
                break;

            case Keyboard.KEYCODE_SHIFT:
                handleShift();
                break;

            case Keyboard.KEYCODE_DONE:
                handleEnter(ic);
                break;

            case Keyboard.KEYCODE_MODE_CHANGE:
                handleModeChange();
                break;

            case KEY_SWITCH_LANGUAGE:
                handleLanguageSwitch();
                break;

            case KEY_SWITCH_SYMBOLS2:
                handleSymbols2Switch();
                break;

            case 32: // مسافة
                handleSpace(ic);
                break;

            default:
                // حرف عادي
                handleCharacter(ic, primaryCode);
                break;
        }
    }

    // ================== Character Input ==================

    /**
     * معالجة إدخال حرف عادي.
     */
    private void handleCharacter(InputConnection ic, int primaryCode) {
        // إذا كان Shift مفعَّلاً وكان حرفاً إنجليزياً، حوِّله إلى كبير
        char character = (char) primaryCode;
        if (isShifted && currentMode == MODE_ENGLISH && Character.isLetter(character)) {
            character = Character.toUpperCase(character);
            // إيقاف Shift بعد حرف واحد
            isShifted = false;
            keyboardView.setShifted(false);
        }

        // إدخال الحرف في حقل النص
        ic.commitText(String.valueOf(character), 1);

        // إضافة الحرف إلى بفر الكلمة الحالية
        currentWordBuffer.append(character);
    }

    // ================== Space Handling ==================

    /**
     * معالجة ضغط مفتاح المسافة.
     * الترتيب:
     * 1. فحص اختصارات الكتابة التلقائية (Auto Typing) أولاً
     * 2. ثم فحص اختصارات النصوص (Text Shortcuts)
     * 3. ثم فحص استبدال المسافة (Space Replacement)
     * 4. وإلا إدخال مسافة عادية
     */
    private void handleSpace(InputConnection ic) {
        String typedWord = currentWordBuffer.toString();

        // 1. فحص الكتابة التلقائية
        AutoTypingEntry autoTypingEntry = KeyboardRepository.findAutoTypingEntry(
                typedWord, cachedAutoTypingEntries);

        if (autoTypingEntry != null) {
            // حذف الاختصار المكتوب
            deleteCurrentWord(ic, typedWord.length());
            currentWordBuffer.setLength(0);

            // بدء الكتابة التلقائية
            startAutoTyping(ic, autoTypingEntry.getFullText(), autoTypingEntry.getTypingSpeedMs());
            return;
        }

        // 2. فحص اختصارات النصوص
        String expansion = KeyboardRepository.findShortcutExpansion(
                typedWord, cachedShortcuts);

        if (expansion != null) {
            // حذف الاختصار وإدراج النص الكامل بدلاً منه
            deleteCurrentWord(ic, typedWord.length());
            ic.commitText(expansion, 1);
            currentWordBuffer.setLength(0);
            return;
        }

        // 3. فحص استبدال المسافة
        boolean spaceReplacementEnabled = prefs.getBoolean(PREF_SPACE_REPLACEMENT_ENABLED, false);
        if (spaceReplacementEnabled) {
            String replacementChar = prefs.getString(PREF_SPACE_REPLACEMENT_CHAR, "");
            if (!TextUtils.isEmpty(replacementChar)) {
                ic.commitText(replacementChar, 1);
                currentWordBuffer.setLength(0);
                return;
            }
        }

        // 4. إدخال مسافة عادية
        ic.commitText(" ", 1);
        currentWordBuffer.setLength(0);
    }

    /**
     * حذف الكلمة الحالية قبل إدراج الاستبدال.
     */
    private void deleteCurrentWord(InputConnection ic, int length) {
        if (length > 0) {
            ic.deleteSurroundingText(length, 0);
        }
    }

    // ================== Auto Typing ==================

    /**
     * بدء الكتابة التلقائية: يكتب النص حرفاً حرفاً بالسرعة المحددة.
     * لا يضغط زر الإرسال بعد الانتهاء.
     */
    private void startAutoTyping(InputConnection ic, String text, int speedMs) {
        if (TextUtils.isEmpty(text)) return;

        isAutoTyping = true;
        autoTypingText = text;
        autoTypingSpeedMs = speedMs;
        autoTypingIndex = 0;

        scheduleNextAutoTypeCharacter();
    }

    /**
     * جدولة كتابة الحرف التالي في الكتابة التلقائية.
     */
    private void scheduleNextAutoTypeCharacter() {
        if (!isAutoTyping || autoTypingText == null) return;

        if (autoTypingIndex >= autoTypingText.length()) {
            // انتهت الكتابة التلقائية - لا نضغط إرسال، نترك المستخدم يرسل بنفسه
            isAutoTyping = false;
            autoTypingText = null;
            autoTypingIndex = 0;
            return;
        }

        final char nextChar = autoTypingText.charAt(autoTypingIndex);
        autoTypingIndex++;

        autoTypingHandler.postDelayed(() -> {
            InputConnection connection = getCurrentInputConnection();
            if (connection != null && isAutoTyping) {
                connection.commitText(String.valueOf(nextChar), 1);
                scheduleNextAutoTypeCharacter();
            } else {
                // توقفت الكتابة التلقائية
                isAutoTyping = false;
            }
        }, autoTypingSpeedMs);
    }

    /**
     * إيقاف الكتابة التلقائية فوراً.
     */
    private void stopAutoTyping() {
        isAutoTyping = false;
        autoTypingText = null;
        autoTypingIndex = 0;
        autoTypingHandler.removeCallbacksAndMessages(null);
    }

    // ================== Delete Handling ==================

    /**
     * معالجة ضغط مفتاح الحذف (Backspace).
     */
    private void handleDelete(InputConnection ic) {
        // إيقاف الكتابة التلقائية عند الضغط على Backspace
        if (isAutoTyping) {
            stopAutoTyping();
            return;
        }

        // حذف الحرف الأخير من البفر
        if (currentWordBuffer.length() > 0) {
            currentWordBuffer.deleteCharAt(currentWordBuffer.length() - 1);
        } else {
            // مسح بفر الكلمة الحالية (وصلنا إلى مسافة أو بداية النص)
            currentWordBuffer.setLength(0);
        }

        // إرسال أمر الحذف
        ic.deleteSurroundingText(1, 0);
    }

    // ================== Shift Handling ==================

    /**
     * معالجة ضغط مفتاح Shift.
     */
    private void handleShift() {
        isShifted = !isShifted;
        keyboardView.setShifted(isShifted);
        keyboardView.invalidateAllKeys();
    }

    // ================== Enter Handling ==================

    /**
     * معالجة ضغط مفتاح الإدخال (Enter).
     */
    private void handleEnter(InputConnection ic) {
        // إيقاف الكتابة التلقائية
        if (isAutoTyping) {
            stopAutoTyping();
            return;
        }

        // مسح بفر الكلمة
        currentWordBuffer.setLength(0);

        // إرسال حدث الإدخال وفق نوع الـ IME Action
        EditorInfo editorInfo = getCurrentInputEditorInfo();
        if (editorInfo != null) {
            int imeOptions = editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;
            if (imeOptions != EditorInfo.IME_ACTION_NONE) {
                ic.performEditorAction(imeOptions);
                return;
            }
        }

        // إدخال سطر جديد
        ic.commitText("\n", 1);
    }

    // ================== Mode Switching ==================

    /**
     * التبديل بين وضع الحروف والرموز.
     */
    private void handleModeChange() {
        if (currentMode == MODE_ARABIC || currentMode == MODE_ENGLISH) {
            // الانتقال إلى الرموز
            currentMode = MODE_SYMBOLS;
            keyboardView.setKeyboard(symbolsKeyboard);
        } else {
            // العودة إلى اللغة الأخيرة (العربية افتراضياً)
            currentMode = MODE_ARABIC;
            keyboardView.setKeyboard(arabicKeyboard);
        }
        currentWordBuffer.setLength(0);
    }

    /**
     * التبديل بين العربية والإنجليزية.
     */
    private void handleLanguageSwitch() {
        if (currentMode == MODE_ARABIC) {
            currentMode = MODE_ENGLISH;
            keyboardView.setKeyboard(englishKeyboard);
        } else {
            currentMode = MODE_ARABIC;
            keyboardView.setKeyboard(arabicKeyboard);
        }
        isShifted = false;
        keyboardView.setShifted(false);
        currentWordBuffer.setLength(0);
    }

    /**
     * التبديل إلى الرموز الثانية أو العودة منها.
     */
    private void handleSymbols2Switch() {
        if (currentMode == MODE_SYMBOLS2) {
            currentMode = MODE_SYMBOLS;
            keyboardView.setKeyboard(symbolsKeyboard);
        } else {
            currentMode = MODE_SYMBOLS2;
            keyboardView.setKeyboard(symbols2Keyboard);
        }
        currentWordBuffer.setLength(0);
    }

    // ================== KeyboardView.OnKeyboardActionListener ==================

    @Override
    public void onPress(int primaryCode) {
        // يمكن إضافة اهتزاز/صوت هنا
    }

    @Override
    public void onRelease(int primaryCode) {
        // لا شيء
    }

    @Override
    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.commitText(text, 1);
        }
    }

    @Override
    public void swipeLeft() {
        // التمرير لليسار: حذف
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) handleDelete(ic);
    }

    @Override
    public void swipeRight() { /* غير مستخدم */ }

    @Override
    public void swipeDown() { /* إخفاء لوحة المفاتيح */ }

    @Override
    public void swipeUp() { /* غير مستخدم */ }

    // ================== Cleanup ==================

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAutoTyping();
        backgroundExecutor.shutdown();
    }
}
