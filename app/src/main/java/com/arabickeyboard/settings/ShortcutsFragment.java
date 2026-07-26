package com.arabickeyboard.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arabickeyboard.R;
import com.arabickeyboard.data.model.Shortcut;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Fragment: إدارة اختصارات النصوص.
 * يعرض قائمة بالاختصارات ويتيح إضافتها وتعديلها وحذفها.
 */
public class ShortcutsFragment extends Fragment {

    // ================== Views ==================

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;

    // ================== ViewModel & Adapter ==================

    private KeyboardViewModel viewModel;
    private ShortcutsAdapter adapter;

    // ================== Lifecycle ==================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shortcuts, container, false);
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
        recyclerView = view.findViewById(R.id.rv_shortcuts);
        tvEmpty      = view.findViewById(R.id.tv_empty);
        fabAdd       = view.findViewById(R.id.fab_add);
    }

    private void setupViewModel() {
        // ViewModel مشترك مع الـ Activity
        viewModel = new ViewModelProvider(requireActivity()).get(KeyboardViewModel.class);

        // مراقبة التغييرات في قائمة الاختصارات
        viewModel.shortcuts.observe(getViewLifecycleOwner(), shortcuts -> {
            adapter.submitList(shortcuts);

            // إظهار رسالة الفراغ إذا لم تكن هناك اختصارات
            if (shortcuts == null || shortcuts.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ShortcutsAdapter(
                // زر التعديل
                shortcut -> showAddEditDialog(shortcut),
                // زر الحذف
                shortcut -> showDeleteConfirmation(shortcut)
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
    }

    // ================== Dialogs ==================

    /**
     * عرض Dialog إضافة أو تعديل اختصار.
     * @param shortcutToEdit null لإضافة جديد، أو الاختصار المراد تعديله
     */
    private void showAddEditDialog(@Nullable Shortcut shortcutToEdit) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_shortcut, null);

        EditText etOriginalWord = dialogView.findViewById(R.id.et_original_word);
        EditText etShortcut     = dialogView.findViewById(R.id.et_shortcut);
        Button   btnSave        = dialogView.findViewById(R.id.btn_save);
        Button   btnCancel      = dialogView.findViewById(R.id.btn_cancel);
        TextView tvError        = dialogView.findViewById(R.id.tv_error);

        // إذا كان تعديلاً، نملأ الحقول بالبيانات الحالية
        boolean isEditing = (shortcutToEdit != null);
        if (isEditing) {
            etOriginalWord.setText(shortcutToEdit.getFullText());
            etShortcut.setText(shortcutToEdit.getShortcutText());
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(isEditing ? R.string.edit_shortcut : R.string.add_shortcut)
                .setView(dialogView)
                .create();

        btnSave.setOnClickListener(v -> {
            String originalWord = etOriginalWord.getText().toString().trim();
            String shortcutText = etShortcut.getText().toString().trim();

            // التحقق من عدم فراغ الحقول
            if (TextUtils.isEmpty(originalWord) || TextUtils.isEmpty(shortcutText)) {
                tvError.setText(R.string.error_empty_fields);
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            if (isEditing) {
                viewModel.updateShortcut(shortcutToEdit, shortcutText, originalWord);
                Toast.makeText(requireContext(), "تم تحديث الاختصار", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.addShortcut(shortcutText, originalWord);
                Toast.makeText(requireContext(), "تمت إضافة الاختصار", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * عرض Dialog تأكيد الحذف.
     */
    private void showDeleteConfirmation(Shortcut shortcut) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.btn_confirm_delete)
                .setMessage(getString(R.string.delete_confirm_msg))
                .setPositiveButton(R.string.btn_delete, (d, which) -> {
                    viewModel.deleteShortcut(shortcut);
                    Toast.makeText(requireContext(), "تم الحذف", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
