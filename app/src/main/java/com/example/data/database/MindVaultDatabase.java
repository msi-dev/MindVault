package com.example.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.data.database.entities.CategoryEntity;
import com.example.data.database.entities.NoteEntity;
import com.example.data.database.entities.NoteFtsEntity;
import com.example.data.database.entities.NoteTagEntity;
import com.example.data.database.entities.TagEntity;

import java.util.concurrent.Executors;

@Database(
    entities = {
        NoteEntity.class,
        CategoryEntity.class,
        TagEntity.class,
        NoteTagEntity.class,
        NoteFtsEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class MindVaultDatabase extends RoomDatabase {

    private static volatile MindVaultDatabase INSTANCE;

    public abstract NoteDao noteDao();
    public abstract CategoryDao categoryDao();
    public abstract TagDao tagDao();

    public static MindVaultDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (MindVaultDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        MindVaultDatabase.class,
                        "mindvault.db"
                    )
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executors.newSingleThreadExecutor().execute(() -> {
                                prePopulateDatabase(getInstance(context));
                            });
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void prePopulateDatabase(MindVaultDatabase database) {
        CategoryDao catDao = database.categoryDao();
        TagDao tagDao = database.tagDao();
        NoteDao noteDao = database.noteDao();

        // 1. Seed Categories
        long personalId = catDao.insert(new CategoryEntity("Personal", "#6366F1", "ic_category"));
        long ideasId = catDao.insert(new CategoryEntity("Ideas", "#F59E0B", "ic_category"));
        long workId = catDao.insert(new CategoryEntity("Work", "#3B82F6", "ic_category"));
        long progId = catDao.insert(new CategoryEntity("Programming", "#10B981", "ic_category"));
        long learnId = catDao.insert(new CategoryEntity("Learning", "#8B5CF6", "ic_category"));
        long projId = catDao.insert(new CategoryEntity("Projects", "#EC4899", "ic_category"));
        catDao.insert(new CategoryEntity("Uncategorized", "#6B7280", "ic_category"));

        // 2. Seed Tags
        long tagWelcome = tagDao.insert(new TagEntity("Welcome"));
        long tagOffline = tagDao.insert(new TagEntity("Offline"));
        long tagGuide = tagDao.insert(new TagEntity("Guide"));
        long tagArchitecture = tagDao.insert(new TagEntity("Architecture"));

        // 3. Seed Starter Notes
        NoteEntity note1 = new NoteEntity(
            "Welcome to MindVault",
            "MindVault is your private personal knowledge management system.\n\n" +
            "Core Principles:\n" +
            "• 100% offline: No account, server, or cloud network needed.\n" +
            "• Local-first: All data resides strictly inside mindvault.db on your device.\n" +
            "• Smart Search: Find saved information across titles and bodies in milliseconds.\n" +
            "• Collections: Group your knowledge into categories and tags.\n\n" +
            "Tap any note to view or edit, or tap the '+' button below to capture a new thought.",
            personalId,
            true // Pinned
        );
        long n1Id = noteDao.insert(note1);
        if (tagWelcome > 0) tagDao.insertNoteTag(new NoteTagEntity(n1Id, tagWelcome));
        if (tagOffline > 0) tagDao.insertNoteTag(new NoteTagEntity(n1Id, tagOffline));

        NoteEntity note2 = new NoteEntity(
            "Quick Capture & Autosave Guide",
            "Capture thoughts the moment inspiration strikes.\n\n" +
            "Features:\n" +
            "• Continuous Autosave: Edits are automatically saved as you pause typing.\n" +
            "• Instant Filtering: Filter notes by categories or view pinned items with one tap.\n" +
            "• Tag Management: Attach multiple tags to create non-linear connections.",
            ideasId,
            true // Pinned
        );
        long n2Id = noteDao.insert(note2);
        if (tagGuide > 0) tagDao.insertNoteTag(new NoteTagEntity(n2Id, tagGuide));

        NoteEntity note3 = new NoteEntity(
            "Native Engine & Architecture",
            "MindVault includes an integrated C++ native engine bridge (libmindvault.so) for native text metrics, word count, and future knowledge graph calculations.\n\n" +
            "The database uses SQLite with FTS full-text indexing, delivering sub-millisecond search performance even across thousands of entries.",
            progId,
            false
        );
        long n3Id = noteDao.insert(note3);
        if (tagArchitecture > 0) tagDao.insertNoteTag(new NoteTagEntity(n3Id, tagArchitecture));

        NoteEntity note4 = new NoteEntity(
            "Offline Sovereignty & Privacy",
            "Your thoughts belong solely to you. MindVault does not request network access permissions and explicitly disables OS-level automated cloud backup extraction.\n\n" +
            "Switch to Airplane mode anytime — MindVault operates with complete independence.",
            learnId,
            false
        );
        long n4Id = noteDao.insert(note4);
        if (tagOffline > 0) tagDao.insertNoteTag(new NoteTagEntity(n4Id, tagOffline));
    }
}
