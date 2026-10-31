package com.example.util;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import com.example.data.database.entities.NoteEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteFileHelper {

    /**
     * Share a single note via Android Share Intent.
     */
    public static void shareNote(Context context, NoteEntity note) {
        if (note == null || context == null) return;

        String title = (note.title == null || note.title.trim().isEmpty()) ? "Untitled Note" : note.title;
        String content = note.content != null ? note.content : "";
        String fullText = title + "\n\n" + content;

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        sendIntent.putExtra(Intent.EXTRA_TEXT, fullText);

        Intent chooser = Intent.createChooser(sendIntent, "Share Note via");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }

    /**
     * Share multiple notes via Android Share Intent.
     */
    public static void shareMultipleNotes(Context context, List<NoteEntity> notes) {
        if (notes == null || notes.isEmpty() || context == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("MindVault Export (").append(notes.size()).append(" notes)\n");
        sb.append("========================================\n\n");

        for (int i = 0; i < notes.size(); i++) {
            NoteEntity n = notes.get(i);
            String title = (n.title == null || n.title.trim().isEmpty()) ? "Untitled Note " + (i + 1) : n.title;
            sb.append("## ").append(title).append("\n\n");
            if (n.content != null) {
                sb.append(n.content).append("\n\n");
            }
            sb.append("----------------------------------------\n\n");
        }

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "MindVault Notes (" + notes.size() + ")");
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());

        Intent chooser = Intent.createChooser(sendIntent, "Share " + notes.size() + " Notes via");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }

    /**
     * Get or create the MindVault/files directory on storage.
     */
    public static File getMindVaultFilesDirectory(Context context) {
        File targetDir = null;

        // Try primary public Documents directory first
        try {
            File docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (docsDir != null) {
                targetDir = new File(docsDir, "MindVault/files");
                if (!targetDir.exists()) {
                    boolean created = targetDir.mkdirs();
                    if (!created && !targetDir.exists()) {
                        targetDir = null;
                    }
                }
            }
        } catch (Exception ignored) {
            targetDir = null;
        }

        // Fallback to app external storage folder if public docs is restricted
        if (targetDir == null || !targetDir.canWrite()) {
            File appExternal = context.getExternalFilesDir(null);
            if (appExternal != null) {
                targetDir = new File(appExternal, "MindVault/files");
            } else {
                targetDir = new File(context.getFilesDir(), "MindVault/files");
            }
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
        }

        return targetDir;
    }

    /**
     * Export a single note as a .txt file into MindVault/files.
     */
    public static File exportNote(Context context, NoteEntity note) {
        if (note == null || context == null) return null;

        File directory = getMindVaultFilesDirectory(context);
        if (directory == null || (!directory.exists() && !directory.mkdirs())) {
            return null;
        }

        String rawTitle = (note.title == null || note.title.trim().isEmpty()) ? "untitled" : note.title.trim();
        // Sanitize filename
        String safeName = rawTitle.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (safeName.length() > 50) {
            safeName = safeName.substring(0, 50);
        }

        File file = new File(directory, safeName + ".txt");
        // Avoid overwriting by appending timestamp if file already exists
        if (file.exists()) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            file = new File(directory, safeName + "_" + timeStamp + ".txt");
        }

        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {

            String title = (note.title == null || note.title.trim().isEmpty()) ? "Untitled Note" : note.title;
            writer.write(title);
            writer.write("\n\n");
            if (note.content != null) {
                writer.write(note.content);
            }
            writer.flush();
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Export multiple notes into MindVault/files.
     */
    public static int exportMultipleNotes(Context context, List<NoteEntity> notes) {
        if (notes == null || notes.isEmpty() || context == null) return 0;
        int count = 0;
        for (NoteEntity note : notes) {
            if (exportNote(context, note) != null) {
                count++;
            }
        }
        return count;
    }
}
