package com.example.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.database.entities.CategoryEntity;
import com.example.data.database.entities.NoteEntity;
import com.example.nativeengine.NativeBridge;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NoteAdapter extends ListAdapter<NoteEntity, NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onNoteClick(NoteEntity note);
        void onNotePinToggle(NoteEntity note);
        void onNoteHold(NoteEntity note, View view);
        void onSelectionChanged(int selectedCount);
    }

    private final OnNoteClickListener listener;
    private final NativeBridge nativeBridge = new NativeBridge();
    private final Map<Long, CategoryEntity> categoryMap = new HashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    private boolean isSelectionMode = false;
    private final Set<Long> selectedIds = new HashSet<>();

    public NoteAdapter(OnNoteClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setCategories(List<CategoryEntity> categories) {
        categoryMap.clear();
        if (categories != null) {
            for (CategoryEntity cat : categories) {
                categoryMap.put(cat.id, cat);
            }
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void setSelectionMode(boolean enabled) {
        if (this.isSelectionMode != enabled) {
            this.isSelectionMode = enabled;
            if (!enabled) {
                selectedIds.clear();
            }
            notifyDataSetChanged();
            if (listener != null) {
                listener.onSelectionChanged(selectedIds.size());
            }
        }
    }

    public void toggleSelection(long noteId) {
        if (selectedIds.contains(noteId)) {
            selectedIds.remove(noteId);
        } else {
            selectedIds.add(noteId);
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size());
        }
    }

    public void selectAll() {
        for (NoteEntity note : getCurrentList()) {
            selectedIds.add(note.id);
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size());
        }
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(0);
        }
    }

    public Set<Long> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }

    public List<NoteEntity> getSelectedNotes() {
        List<NoteEntity> selected = new ArrayList<>();
        for (NoteEntity note : getCurrentList()) {
            if (selectedIds.contains(note.id)) {
                selected.add(note);
            }
        }
        return selected;
    }

    private static final DiffUtil.ItemCallback<NoteEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<NoteEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull NoteEntity oldItem, @NonNull NoteEntity newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull NoteEntity oldItem, @NonNull NoteEntity newItem) {
            return oldItem.title.equals(newItem.title) &&
                    oldItem.content.equals(newItem.content) &&
                    oldItem.isPinned == newItem.isPinned &&
                    oldItem.isArchived == newItem.isArchived &&
                    oldItem.updatedAt == newItem.updatedAt &&
                    ((oldItem.categoryId == null && newItem.categoryId == null) ||
                     (oldItem.categoryId != null && oldItem.categoryId.equals(newItem.categoryId)));
        }
    };

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteEntity note = getItem(position);
        holder.bind(note);
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final MaterialCheckBox selectCheckBox;
        private final TextView titleText;
        private final TextView contentText;
        private final TextView categoryBadge;
        private final TextView metricsText;
        private final TextView dateText;
        private final ImageView pinIcon;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            selectCheckBox = itemView.findViewById(R.id.checkbox_select);
            titleText = itemView.findViewById(R.id.text_note_title);
            contentText = itemView.findViewById(R.id.text_note_content);
            categoryBadge = itemView.findViewById(R.id.text_category_badge);
            metricsText = itemView.findViewById(R.id.text_metrics);
            dateText = itemView.findViewById(R.id.text_note_date);
            pinIcon = itemView.findViewById(R.id.image_pin);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                NoteEntity note = getItem(pos);

                if (isSelectionMode) {
                    toggleSelection(note.id);
                } else if (listener != null) {
                    listener.onNoteClick(note);
                }
            });

            itemView.setOnLongClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return false;
                NoteEntity note = getItem(pos);

                if (listener != null) {
                    listener.onNoteHold(note, v);
                    return true;
                }
                return false;
            });

            pinIcon.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotePinToggle(getItem(pos));
                }
            });
        }

        public void bind(NoteEntity note) {
            titleText.setText(note.title.isEmpty() ? "Untitled Note" : note.title);

            if (note.content.isEmpty()) {
                contentText.setVisibility(View.GONE);
            } else {
                contentText.setVisibility(View.VISIBLE);
                contentText.setText(note.content);
            }

            pinIcon.setVisibility(note.isPinned ? View.VISIBLE : View.GONE);

            // Selection state
            if (isSelectionMode) {
                selectCheckBox.setVisibility(View.VISIBLE);
                boolean selected = selectedIds.contains(note.id);
                selectCheckBox.setChecked(selected);
                cardView.setStrokeColor(selected ? ContextCompat.getColor(cardView.getContext(), R.color.primary)
                                                 : ContextCompat.getColor(cardView.getContext(), R.color.cardBorder));
                cardView.setStrokeWidth(selected ? 4 : 2);
            } else {
                selectCheckBox.setVisibility(View.GONE);
                cardView.setStrokeColor(ContextCompat.getColor(cardView.getContext(), R.color.cardBorder));
                cardView.setStrokeWidth(2);
            }

            // Category
            if (note.categoryId != null && categoryMap.containsKey(note.categoryId)) {
                CategoryEntity cat = categoryMap.get(note.categoryId);
                categoryBadge.setVisibility(View.VISIBLE);
                categoryBadge.setText(cat != null ? cat.name : "Uncategorized");
            } else {
                categoryBadge.setVisibility(View.GONE);
            }

            // Metrics
            int readingTime = nativeBridge.estimateReadingTimeMinutes(note.content);
            int words = nativeBridge.calculateWordCount(note.content);
            if (words > 0) {
                metricsText.setVisibility(View.VISIBLE);
                metricsText.setText(words + " words · " + readingTime + " min read");
            } else {
                metricsText.setVisibility(View.GONE);
            }

            dateText.setText(dateFormat.format(new Date(note.updatedAt)));
        }
    }
}
