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
import com.arabickeyboard.data.model.AutoTypingEntry;

/**
 * RecyclerView Adapter لعرض قائمة إدخالات الكتابة التلقائية.
 */
public class AutoTypingAdapter extends ListAdapter<AutoTypingEntry, AutoTypingAdapter.AutoTypingViewHolder> {

    // ================== Interfaces ==================

    public interface OnEditClickListener {
        void onEdit(AutoTypingEntry entry);
    }

    public interface OnDeleteClickListener {
        void onDelete(AutoTypingEntry entry);
    }

    // ================== Fields ==================

    private final OnEditClickListener editListener;
    private final OnDeleteClickListener deleteListener;

    // ================== DiffUtil ==================

    private static final DiffUtil.ItemCallback<AutoTypingEntry> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AutoTypingEntry>() {

                @Override
                public boolean areItemsTheSame(@NonNull AutoTypingEntry o, @NonNull AutoTypingEntry n) {
                    return o.getId() == n.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull AutoTypingEntry o, @NonNull AutoTypingEntry n) {
                    return o.getTriggerShortcut().equals(n.getTriggerShortcut())
                            && o.getFullText().equals(n.getFullText())
                            && o.getTypingSpeedMs() == n.getTypingSpeedMs();
                }
            };

    // ================== Constructor ==================

    public AutoTypingAdapter(OnEditClickListener editListener,
                             OnDeleteClickListener deleteListener) {
        super(DIFF_CALLBACK);
        this.editListener   = editListener;
        this.deleteListener = deleteListener;
    }

    // ================== RecyclerView.Adapter ==================

    @NonNull
    @Override
    public AutoTypingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_auto_typing, parent, false);
        return new AutoTypingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AutoTypingViewHolder holder, int position) {
        AutoTypingEntry entry = getItem(position);
        holder.bind(entry, editListener, deleteListener);
    }

    // ================== ViewHolder ==================

    static class AutoTypingViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTrigger;
        private final TextView tvTextPreview;
        private final TextView tvSpeed;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        public AutoTypingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTrigger     = itemView.findViewById(R.id.tv_trigger);
            tvTextPreview = itemView.findViewById(R.id.tv_text_preview);
            tvSpeed       = itemView.findViewById(R.id.tv_speed);
            btnEdit       = itemView.findViewById(R.id.btn_edit);
            btnDelete     = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(AutoTypingEntry entry,
                         OnEditClickListener editListener,
                         OnDeleteClickListener deleteListener) {

            tvTrigger.setText(entry.getTriggerShortcut());
            tvTextPreview.setText(entry.getFullText());
            tvSpeed.setText(entry.getSpeedDescription() + " (" + entry.getTypingSpeedMs() + "ms)");

            btnEdit.setOnClickListener(v -> {
                if (editListener != null) editListener.onEdit(entry);
            });

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(entry);
            });
        }
    }
}
