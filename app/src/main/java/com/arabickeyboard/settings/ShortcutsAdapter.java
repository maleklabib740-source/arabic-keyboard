package com.arabickeyboard.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.arabickeyboard.R;
import com.arabickeyboard.data.model.Shortcut;

/**
 * RecyclerView Adapter لعرض قائمة اختصارات النصوص.
 * يستخدم ListAdapter مع DiffUtil لتحديثات فعّالة.
 */
public class ShortcutsAdapter extends ListAdapter<Shortcut, ShortcutsAdapter.ShortcutViewHolder> {

    // ================== Interfaces ==================

    public interface OnEditClickListener {
        void onEdit(Shortcut shortcut);
    }

    public interface OnDeleteClickListener {
        void onDelete(Shortcut shortcut);
    }

    // ================== Fields ==================

    private final OnEditClickListener editListener;
    private final OnDeleteClickListener deleteListener;

    // ================== DiffUtil ==================

    private static final DiffUtil.ItemCallback<Shortcut> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Shortcut>() {

                @Override
                public boolean areItemsTheSame(@NonNull Shortcut oldItem, @NonNull Shortcut newItem) {
                    // نفس الـ ID يعني نفس العنصر
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Shortcut oldItem, @NonNull Shortcut newItem) {
                    // نفس المحتوى؟
                    return oldItem.getShortcutText().equals(newItem.getShortcutText())
                            && oldItem.getFullText().equals(newItem.getFullText());
                }
            };

    // ================== Constructor ==================

    public ShortcutsAdapter(OnEditClickListener editListener,
                            OnDeleteClickListener deleteListener) {
        super(DIFF_CALLBACK);
        this.editListener   = editListener;
        this.deleteListener = deleteListener;
    }

    // ================== RecyclerView.Adapter ==================

    @NonNull
    @Override
    public ShortcutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shortcut, parent, false);
        return new ShortcutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShortcutViewHolder holder, int position) {
        Shortcut shortcut = getItem(position);
        holder.bind(shortcut, editListener, deleteListener);
    }

    // ================== ViewHolder ==================

    static class ShortcutViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvShortcut;
        private final TextView tvOriginalWord;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        public ShortcutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShortcut    = itemView.findViewById(R.id.tv_shortcut);
            tvOriginalWord = itemView.findViewById(R.id.tv_original_word);
            btnEdit       = itemView.findViewById(R.id.btn_edit);
            btnDelete     = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Shortcut shortcut,
                         OnEditClickListener editListener,
                         OnDeleteClickListener deleteListener) {

            tvShortcut.setText(shortcut.getShortcutText());
            tvOriginalWord.setText(shortcut.getFullText());

            btnEdit.setOnClickListener(v -> {
                if (editListener != null) editListener.onEdit(shortcut);
            });

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(shortcut);
            });
        }
    }
}
