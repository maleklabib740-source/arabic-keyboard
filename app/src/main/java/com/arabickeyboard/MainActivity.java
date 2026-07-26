package com.arabickeyboard;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.arabickeyboard.settings.SettingsActivity;

import java.util.List;

/**
 * الشاشة الرئيسية للتطبيق.
 * تساعد المستخدم على:
 * 1. تفعيل لوحة المفاتيح من إعدادات النظام
 * 2. تعيينها كلوحة مفاتيح افتراضية
 * 3. الانتقال إلى شاشة الإعدادات
 */
public class MainActivity extends AppCompatActivity {

    // ================== Views ==================

    private TextView tvEnableStatus;
    private TextView tvDefaultStatus;
    private Button btnEnableKeyboard;
    private Button btnSetDefault;
    private Button btnOpenSettings;

    // ================== Lifecycle ==================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // تحديث الحالة في كل مرة يعود المستخدم إلى الشاشة
        updateStatus();
    }

    // ================== Initialization ==================

    private void initViews() {
        tvEnableStatus  = findViewById(R.id.tv_enable_status);
        tvDefaultStatus = findViewById(R.id.tv_default_status);
        btnEnableKeyboard = findViewById(R.id.btn_enable_keyboard);
        btnSetDefault     = findViewById(R.id.btn_set_default);
        btnOpenSettings   = findViewById(R.id.btn_open_settings);
    }

    private void setupClickListeners() {
        // الخطوة 1: فتح إعدادات لوحات المفاتيح لتفعيل الكيبورد
        btnEnableKeyboard.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
            startActivity(intent);
        });

        // الخطوة 2: تعيين لوحة المفاتيح الافتراضية
        btnSetDefault.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showInputMethodPicker();
            }
        });

        // فتح شاشة الإعدادات
        btnOpenSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    // ================== Status Checking ==================

    /**
     * تحديث حالة التفعيل والإعداد الافتراضي.
     */
    private void updateStatus() {
        boolean isEnabled  = isKeyboardEnabled();
        boolean isDefault  = isKeyboardDefault();

        // حالة التفعيل
        if (isEnabled) {
            tvEnableStatus.setText(R.string.status_enabled);
            tvEnableStatus.setTextColor(getColor(R.color.success));
        } else {
            tvEnableStatus.setText(R.string.status_not_enabled);
            tvEnableStatus.setTextColor(getColor(R.color.error_color));
        }

        // حالة الافتراضية
        if (isDefault) {
            tvDefaultStatus.setText(R.string.status_default);
            tvDefaultStatus.setTextColor(getColor(R.color.success));
        } else {
            tvDefaultStatus.setText(R.string.status_not_default);
            tvDefaultStatus.setTextColor(getColor(R.color.error_color));
        }
    }

    /**
     * التحقق مما إذا كانت لوحة المفاتيح مُفعَّلة في إعدادات النظام.
     */
    private boolean isKeyboardEnabled() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm == null) return false;

        List<InputMethodInfo> enabledMethods = imm.getEnabledInputMethodList();
        for (InputMethodInfo info : enabledMethods) {
            if (info.getPackageName().equals(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * التحقق مما إذا كانت لوحة المفاتيح هي الافتراضية.
     */
    private boolean isKeyboardDefault() {
        String defaultIME = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD
        );
        return defaultIME != null && defaultIME.startsWith(getPackageName());
    }
}
