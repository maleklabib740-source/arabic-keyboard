# لوحة مفاتيح عربية — Arabic Keyboard IME

مشروع Android أصلي بالكامل بلغة Java يُنشئ لوحة مفاتيح خارجية كاملة (InputMethodService)
مماثلة لـ Gboard وMicrosoft SwiftKey.

---

## 📦 متطلبات الفتح

| أداة | الإصدار المطلوب |
|------|----------------|
| Android Studio | Hedgehog (2023.1.1) أو أحدث |
| JDK | 8 أو أحدث |
| Android SDK | API 34 (compileSdk) |
| minSdk | API 24 (Android 7.0) |

---

## 🚀 خطوات فتح المشروع في Android Studio

1. افتح **Android Studio**
2. اختر **File → Open**
3. حدد مجلد `android-keyboard/` (المجلد الذي يحتوي على `settings.gradle`)
4. انتظر حتى يكتمل مزامنة Gradle
5. اضغط **Build → Make Project** للتأكد من عدم وجود أخطاء

---

## 🏗️ بنية المشروع

```
app/src/main/
├── AndroidManifest.xml          — إعلان الخدمة والأنشطة
├── java/com/arabickeyboard/
│   ├── MainActivity.java        — شاشة الترحيب + خطوات التفعيل
│   ├── keyboard/
│   │   └── ArabicKeyboardService.java  — ❤️ الخدمة الرئيسية (IME)
│   ├── data/
│   │   ├── model/
│   │   │   ├── Shortcut.java           — نموذج الاختصار
│   │   │   └── AutoTypingEntry.java    — نموذج الكتابة التلقائية
│   │   ├── database/
│   │   │   ├── AppDatabase.java        — Room Database
│   │   │   ├── ShortcutDao.java        — DAO الاختصارات
│   │   │   └── AutoTypingDao.java      — DAO الكتابة التلقائية
│   │   └── repository/
│   │       └── KeyboardRepository.java — Repository مشترك
│   └── settings/
│       ├── SettingsActivity.java       — شاشة الإعدادات (3 تبويبات)
│       ├── KeyboardViewModel.java      — ViewModel مشترك
│       ├── ShortcutsFragment.java      — تبويب الاختصارات
│       ├── ShortcutsAdapter.java       — Adapter قائمة الاختصارات
│       ├── SpaceReplacementFragment.java — تبويب استبدال المسافة
│       ├── AutoTypingFragment.java     — تبويب الكتابة التلقائية
│       └── AutoTypingAdapter.java      — Adapter الكتابة التلقائية
└── res/
    ├── xml/
    │   ├── method.xml           — إعدادات IME (subtypes)
    │   ├── keyboard_arabic.xml  — تخطيط الكيبورد العربي
    │   ├── keyboard_english.xml — تخطيط الكيبورد الإنجليزي
    │   ├── keyboard_symbols.xml — الأرقام والرموز
    │   └── keyboard_symbols2.xml — رموز إضافية
    ├── layout/                  — ملفات الواجهة
    └── values/                  — الألوان والنصوص والأبعاد
```

---

## 📱 طريقة التثبيت والتفعيل

### الخطوة 1: تثبيت التطبيق
```bash
# من Android Studio: Run → Run 'app'
# أو من CLI:
./gradlew installDebug
```

### الخطوة 2: تفعيل لوحة المفاتيح
1. افتح التطبيق
2. اضغط **"تفعيل لوحة المفاتيح"**
3. في قائمة النظام، فعّل **"لوحة مفاتيح عربية"**

### الخطوة 3: تعيينها افتراضية
1. اضغط **"تعيين كافتراضية"**
2. اختر **"لوحة مفاتيح عربية"** من قائمة الاختيار

---

## ✨ الميزات

### 1. لوحة المفاتيح
- **عربية**: 28 حرفاً مع ترتيب مشابه لـ Gboard
- **إنجليزية**: QWERTY مع دعم Shift للأحرف الكبيرة
- **رموز**: أرقام + رموز شائعة + رموز خاصة (صفحتان)
- دعم RTL تلقائي من النظام

### 2. اختصارات النصوص
- أضف اختصاراً مثل `هنف` → `عامل إيه`
- عند كتابة الاختصار + مسافة يتوسع تلقائياً
- أضف عدداً غير محدود من الاختصارات

### 3. استبدال زر المسافة
- اختر رمزاً مخصصاً يُكتب بدلاً من المسافة
- مع زر تشغيل/إيقاف

### 4. الكتابة التلقائية (Auto Typing)
- أضف اختصاراً مثل `123` مع النص الكامل
- عند كتابة الاختصار + مسافة يبدأ الكيبورد بالكتابة حرفاً حرفاً
- تحكم في السرعة عبر SeekBar (10ms - 200ms)
- **لا يضغط زر الإرسال أبداً** — المستخدم يرسل بنفسه

---

## 🔧 إعادة توليد الأيقونات

بعد فتح المشروع في Android Studio:
1. انقر بزر الماوس الأيمن على `app/src/main/res`
2. اختر **New → Image Asset**
3. اختر نوع **Launcher Icons (Adaptive and Legacy)**
4. صمم أيقونتك وانقر **Finish**

---

## 🛠️ الـ Stack التقنية

| المكوّن | المكتبة |
|---------|---------|
| IME | Android `InputMethodService` |
| قاعدة بيانات | Room (SQLite) |
| واجهة المستخدم | XML Layouts + RecyclerView |
| Architecture | ViewModel + LiveData + Repository |
| بناء | Gradle 8.4 |
| لغة | Java 8 |
