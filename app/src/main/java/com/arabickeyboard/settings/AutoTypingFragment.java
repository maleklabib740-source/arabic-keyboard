package com.arabickeyboard.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arabickeyboard.R;
import com.arabickeyboard.data.model.AutoTypingEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Fragment: إدارة إدخالات الكتابة التلقائية.
 *
 * المستخدم يضبط:
 * - الاختصار الذي يُشغِّل الكتابة التلقائية
 * - النص الكامل الذي سيُكتب تلقائياً
 * - سرعة الكتابة (SeekBar)
 *
 * ملاحظة: بعد انتهاء الكتابة لا يُضغط زر الإرسال إطلاقاً.
 */
public class AutoTypingFragment extends Fragment {

    // ================== Views ==================

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;

    // ================== ViewModel & Adapter ==================

    private KeyboardViewModel viewModel;
    private AutoTypingAdapter adapter;

    // ================== Lifecycle ==================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auto_typing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupFab();
    }

    // ================== Initialization ==================

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.rv_auto_typing);
        tvEmpty      = view.findViewById(R.id.tv_empty);
        fabAdd       = view.findViewById(R.id.fab_add);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(KeyboardViewModel.class);

        viewModel.autoTypingEntries.observe(getViewLifecycleOwner(), entries -> {
            adapter.submitList(entries);

            if (entries == null || entries.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new AutoTypingAdapter(
                entry -> showAddEditDialog(entry),
                entry -> showDeleteConfirmation(entry)
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
    }

    // ================== Dialog ==================

    /**
     * عرض Dialog إضافة أو تعديل إدخال كتابة تلقائية.
     */
    private void showAddEditDialog(@Nullable AutoTypingEntry entryToEdit) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_auto_typing, null);

        EditText etTrigger  = dialogView.findViewById(R.id.et_trigger_shortcut);
        EditText etFullText = dialogView.findViewById(R.id.et_full_text);
        SeekBar  seekBar    = dialogView.findViewById(R.id.seekbar_speed);
        TextView tvSpeed    = dialogView.findViewById(R.id.tv_speed_value);
        Button   btnSave    = dialogView.findViewById(R.id.btn_save);
        Button   btnCancel  = dialogView.findViewById(R.id.btn_cancel);
        TextView tvError    = dialogView.findViewById(R.id.tv_error);

        // ربط SeekBar بعرض السرعة
        // SeekBar القيمة: 0 = 10ms (أسرع)، 190 = 200ms (أبطأ)
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                int speedMs = progress + 10; // 10ms → 200ms
                tvSpeed.setText(speedMs + " ms");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        boolean isEditing = (entryToEdit != null);

        if (isEditing) {
            etTrigger.setText(entryToEdit.getTriggerShortcut());
            etFullText.setText(entryToEdit.getFullText());
            int seekProgress = Math.max(0, entryToEdit.getTypingSpeedMs() - 10);
            seekBar.setProgress(seekProgress);
            tvSpeed.setText(entryToEdit.getTypingSpeedMs() + " ms");
        } else {
            // قيمة افتراضية: 50ms
            seekBar.setProgress(40);
            tvSpeed.setText("50 ms");
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(isEditing ? R.string.edit_auto_typing : R.string.add_auto_typing)
                .setView(dialogView)
                .create();

        btnSave.setOnClickListener(v -> {
            String trigger  = etTrigger.getText().toString().trim();
            String fullText = etFullText.getText().toString().trim();
            int    speedMs  = seekBar.getProgress() + 10; // تحويل SeekBar إلى ms

            if (TextUtils.isEmpty(trigger) || TextUtils.isEmpty(fullText)) {
                tvError.setText(R.string.error_empty_fields);
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            if (isEditing) {
                viewModel.updateAutoTypingEntry(entryToEdit, trigger, fullText, speedMs);
                Toast.makeText(requireContext(), "تم التحديث", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.addAutoTypingEntry(trigger, fullText, speedMs);
                Toast.makeText(requireContext(), "تمت الإضافة", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * Dialog تأكيد الحذف.
     */
    private void showDeleteConfirmation(AutoTypingEntry entry) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.btn_confirm_delete)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.btn_delete, (d, which) -> {
                    viewModel.deleteAutoTypingEntry(entry);
                    Toast.makeText(requireContext(), "تم الحذف", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
