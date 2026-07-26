package com.arabickeyboard.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.arabickeyboard.R;
import com.arabickeyboard.keyboard.ArabicKeyboardService;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Fragment: إعداد استبدال زر المسافة.
 *
 * يتيح للمستخدم:
 * - تفعيل/إيقاف استبدال المسافة
 * - تحديد الرمز البديل
 *
 * الإعدادات تُحفَظ في SharedPreferences وتُقرأ من ArabicKeyboardService.
 */
public class SpaceReplacementFragment extends Fragment {

    // ================== Views ==================

    private SwitchMaterial switchEnabled;
    private EditText etReplacementChar;
    private Button btnSave;

    // ================== SharedPreferences ==================

    private SharedPreferences prefs;

    // ================== Lifecycle ==================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_space_replacement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences(
                ArabicKeyboardService.PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE
        );

        initViews(view);
        loadCurrentSettings();
        setupClickListeners();
    }

    // ================== Initialization ==================

    private void initViews(View view) {
        switchEnabled       = view.findViewById(R.id.switch_space_replacement);
        etReplacementChar   = view.findViewById(R.id.et_replacement_char);
        btnSave             = view.findViewById(R.id.btn_save_space);
    }

    /**
     * تحميل الإعدادات الحالية من SharedPreferences.
     */
    private void loadCurrentSettings() {
        boolean isEnabled = prefs.getBoolean(
                ArabicKeyboardService.PREF_SPACE_REPLACEMENT_ENABLED, false);
        String replacementChar = prefs.getString(
                ArabicKeyboardService.PREF_SPACE_REPLACEMENT_CHAR, "");

        switchEnabled.setChecked(isEnabled);
        etReplacementChar.setText(replacementChar);

        // تفعيل/تعطيل حقل الإدخال بناءً على حالة السويتش
        etReplacementChar.setEnabled(isEnabled);
    }

    private void setupClickListeners() {
        // عند تغيير حالة السويتش، تفعيل/تعطيل حقل الإدخال
        switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etReplacementChar.setEnabled(isChecked);
        });

        // زر الحفظ
        btnSave.setOnClickListener(v -> saveSettings());
    }

    // ================== Save ==================

    /**
     * حفظ الإعدادات في SharedPreferences.
     */
    private void saveSettings() {
        boolean isEnabled = switchEnabled.isChecked();
        String replacementChar = etReplacementChar.getText().toString().trim();

        // التحقق: إذا كان مفعلاً يجب أن يكون هناك رمز بديل
        if (isEnabled && TextUtils.isEmpty(replacementChar)) {
            etReplacementChar.setError(getString(R.string.error_empty_fields));
            return;
        }

        // التحقق: يجب أن يكون حرفاً واحداً فقط
        if (isEnabled && replacementChar.length() > 1) {
            etReplacementChar.setError(getString(R.string.error_single_char));
            return;
        }

        // الحفظ في SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ArabicKeyboardService.PREF_SPACE_REPLACEMENT_ENABLED, isEnabled);
        editor.putString(ArabicKeyboardService.PREF_SPACE_REPLACEMENT_CHAR, replacementChar);
        editor.apply();

        // رسالة تأكيد
        String message = isEnabled
                ? "تم تفعيل الاستبدال بـ \"" + replacementChar + "\""
                : "تم إيقاف استبدال المسافة";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
