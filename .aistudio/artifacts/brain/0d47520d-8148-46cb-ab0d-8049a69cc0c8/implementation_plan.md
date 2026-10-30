# MindVault — Private Offline Knowledge Management System

MindVault is a native Android personal knowledge management system built with Material Design 3, classic XML layouts and fragments, an offline-first Room database with SQLite FTS5 full-text search, and a native C++ engine bridge. It provides a quiet, distraction-free environment to capture, organize, search, and connect ideas with zero network dependency.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed through user input and will guide the implementation:

- **UI Framework (Confirmed)**: Classic Android XML Layouts, Material Design 3 Components (`MaterialCardView`, `MaterialToolbar`, `BottomNavigationView`, `Chip`, `TextInputLayout`), and Fragments with AndroidX Navigation.
- **Initial Content (Confirmed)**: Pre-populated with starter guide notes highlighting Markdown formatting, tags, categories, search syntax, and keyboard shortcuts.
- **Search & Native Processing (Confirmed)**: Room FTS5 full-text search indexing combined with a C++ JNI native bridge (`libmindvault.so`) for native text metrics, hash validation, and token analysis.
- **Data Privacy & Storage (Confirmed)**: 100% offline, local-only SQLite storage (`mindvault.db`), with network permissions strictly omitted and Android system backup excluded.

---

## 1. Overview & Core Concept

- **What It Does**: MindVault enables users to instantly capture thoughts, organize knowledge into hierarchical categories and flexible tags, pin essential notes, perform blazing-fast full-text search across titles and note bodies, and switch seamlessly between adaptive light, dark, and system themes.
- **Target Audience**: Researchers, developers, writers, and privacy-conscious thinkers who require complete on-device data sovereignty and instant responsiveness without cloud accounts or subscriptions.
- **Key Value**: Guaranteed local persistence, rapid capture with autosave, zero latency, and zero telemetry.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Dashboard & Quick Overview (Home)**:
   - Greeting dynamically calculated from the local hour ("Good morning", "Good afternoon", "Good evening").
   - Metrics cards showing total active notes and total collections/categories.
   - Quick capture FAB to immediately start a new note.
   - Pinned notes horizontal carousel or quick-access list.
   - Recent notes section with instant tap-to-edit.
2. **Knowledge Vault (Vault)**:
   - Complete list of active notes with toggleable List and Compact Grid layouts.
   - Filter chips for Categories (Personal, Ideas, Work, Programming, Learning, Projects) and Tags.
   - Quick search and sort modal (by updated time, created time, alphabetical title, or pinned first).
   - Swipe or contextual menu actions for Pin, Archive, and Soft-delete.
3. **Distraction-Free Note Editor (Editor)**:
   - Transparent toolbar with back arrow, pin toggle, category picker, and save status indicator ("Saved", "Editing...").
   - Title input with bold headline styling.
   - Multiline expandable content editor.
   - Tag manager with interactive chip addition and deletion.
   - Background debounced autosave (saving changes after user pauses typing, without blocking the UI).
4. **Full-Text Instant Search (Search)**:
   - Search bar with clear button and real-time query filtering.
   - FTS-backed keyword matching across title and content.
   - Category and tag match filters with query highlighting.
   - Empty query state displaying recent search queries and quick filter suggestions.
5. **Explore & Taxonomy (Explore)**:
   - Category overview with note counts and color-coded chips.
   - Tag cloud showing tag frequencies.
   - Foundation for future Phase 3 interactive knowledge graph.
6. **Settings & Customization (Settings)**:
   - Theme mode selector: System Default, Light, Dark (applied immediately via `AppCompatDelegate`).
   - Database telemetry: note count, storage usage estimate, native engine version badge.
   - Placeholder actions for Phase 5 encrypted backup and export.
   - About MindVault and local privacy guarantee.

### Visual Identity & Theme Palette

- **Aesthetic Direction**: Minimalist, calm, academic/editorial. Generous whitespace, clean contrast, refined typography, and purposeful Material 3 cards.
- **Color Palette (Semantic M3)**:
  - *Light Mode*:
    - Background: `#F7F7FA`
    - Surface: `#FFFFFF`
    - Primary: `#465D9A`
    - Secondary: `#596780`
    - Accent: `#647DB5`
    - On-Surface / Text: `#191C24`
  - *Dark Mode*:
    - Background: `#111318`
    - Surface: `#191C23`
    - Primary: `#B3C5FF`
    - Secondary: `#BBC6DF`
    - Accent: `#A4B9F5`
    - On-Surface / Text: `#E2E2EA`
- **Typography & Rhythm**:
  - Clean sans-serif font pairing (`Inter` / `sans-serif`) with clear hierarchical scale (`HeadlineSmall` for headers, `TitleMedium` for note titles, `BodyMedium` for content).
- **Empty & Feedback States**:
  - Thoughtfully designed empty states with a custom vault icon and inviting call-to-actions ("Your vault is waiting", "Capture your first thought").
  - Subtle snackbar feedback on archive, delete, or restore actions with "Undo" capabilities.

---

## 3. Key Product Decisions & Trade-Offs

- **XML Layouts & Material Components vs. Compose**:
  - *Chosen Approach*: Classic XML layouts with `MaterialToolbar`, `MaterialCardView`, `RecyclerView`, and AndroidX Navigation.
  - *Why*: Directly honors the user's architectural brief and explicit answer, ensuring compatibility with standard View-based components and transitions.
- **Room SQLite + FTS4/5 External Content**:
  - *Chosen Approach*: Room entities (`NoteEntity`, `CategoryEntity`, `TagEntity`, `NoteTagEntity`) coupled with a virtual `NoteFtsEntity` full-text search table.
  - *Why*: Delivers sub-millisecond full-text searches even across thousands of notes, avoiding table-scan performance degradation.
- **Single-Activity Multi-Fragment Navigation**:
  - *Chosen Approach*: `MainActivity` hosts `NavHostFragment`, `MaterialToolbar`, and `BottomNavigationView`, driving 6 modular fragments (`HomeFragment`, `VaultFragment`, `EditorFragment`, `SearchFragment`, `ExploreFragment`, `SettingsFragment`).
  - *Why*: Unified backstack, smoother fragment transitions, and consistent navigation bar state management.
- **Autosave with Debounce**:
  - *Chosen Approach*: Coroutine or handler debounce (500ms after last keystroke) that triggers a repository background update.
  - *Why*: Prevents loss of user thoughts upon app switching without flooding the SQLite write queue on every keystroke.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                              │
│       ┌──────────────────────┐          ┌──────────────────────┐       │
│       │   MaterialToolbar    │          │ BottomNavigationView │       │
│       └──────────────────────┘          └──────────────────────┘       │
│                         NavHostFragment / NavGraph                     │
│  ┌───────────────┬───────────────┬───────────────┬──────────────────┐  │
│  │ HomeFragment  │ VaultFragment │ SearchFragment│ ExploreFragment  │  │
│  ├───────────────┴───────────────┼───────────────┴──────────────────┤  │
│  │        EditorFragment         │         SettingsFragment         │  │
│  └───────────────────────────────┴──────────────────────────────────┘  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ LiveData / ViewModel
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                            ViewModels Layer                            │
│  HomeViewModel  •  VaultViewModel  •  EditorViewModel  •  SearchVM    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        NoteRepository & Storage                        │
│   • Background Executor / Coroutine Dispatchers                        │
│   • Seed / Starter Guide Notes Generator                               │
│   • SharedPreferences (Theme Mode: System / Light / Dark)              │
│                                                                        │
│   ┌──────────────────────────────┐    ┌─────────────────────────────┐  │
│   │     MindVaultDatabase        │    │    NativeBridge (JNI)       │  │
│   │  Room SQLite (FTS4/5)        │    │    libmindvault.so (C++)    │  │
│   │  • NoteDao                   │    │    • Engine version         │  │
│   │  • CategoryDao               │    │    • Text character count   │  │
│   │  • TagDao                    │    │    • Reading time & tokens  │  │
│   └──────────────────────────────┘    └─────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Database Schema

1. **`notes` Table**:
   - `id` (INTEGER, PK, Auto-increment)
   - `title` (TEXT)
   - `content` (TEXT)
   - `category_id` (INTEGER, FK -> `categories.id` ON DELETE SET NULL)
   - `is_pinned` (INTEGER / BOOLEAN)
   - `is_archived` (INTEGER / BOOLEAN)
   - `is_deleted` (INTEGER / BOOLEAN)
   - `created_at` (INTEGER)
   - `updated_at` (INTEGER)
2. **`categories` Table**:
   - `id` (INTEGER, PK, Auto-increment)
   - `name` (TEXT)
   - `color` (TEXT hex)
   - `icon` (TEXT identifier)
   - `created_at` (INTEGER)
   - *Default categories*: Personal, Ideas, Work, Programming, Learning, Projects, Uncategorized
3. **`tags` Table**:
   - `id` (INTEGER, PK, Auto-increment)
   - `name` (TEXT, UNIQUE)
   - `created_at` (INTEGER)
4. **`note_tags` Table (Junction)**:
   - `note_id` (INTEGER, FK -> `notes.id` CASCADE)
   - `tag_id` (INTEGER, FK -> `tags.id` CASCADE)
   - Composite PK: `(note_id, tag_id)`
5. **`note_fts` Virtual Table (FTS4)**:
   - External content entity tied to `NoteEntity` indexing `title` and `content`.

### Native C++ Integration
- CMakeLists.txt building `libmindvault.so` from `native-lib.cpp`.
- `NativeBridge.java` exposing:
  - `getEngineVersion()`: Returns `"MindVault Native Engine 1.0 (C++17)"`.
  - `calculateTextMetrics(String text)`: Analyzes word count, character count, and estimated reading time.
  - Graceful fallback: If native library loading fails in any environment, Java fallback methods ensure the app functions without interruption.

---

## 5. Implementation Sequence

1. **Platform Metadata & Custom Launcher Icon**:
   - Set app name "MindVault" in `metadata.json` and `strings.xml`.
   - Create custom adaptive launcher icon with vault/shield vector motif and dark navy palette.
2. **Gradle Dependencies Configuration**:
   - Add Navigation Fragment & UI (`androidx.navigation:navigation-fragment-ktx:2.8.9`, `navigation-ui-ktx`).
   - Add Material Components (`com.google.android.material:material`).
   - Add ConstraintLayout and RecyclerView.
   - Configure CMake / externalNativeBuild with JNI bridge.
3. **Room Database & Schema Setup**:
   - Entities: `NoteEntity`, `CategoryEntity`, `TagEntity`, `NoteTagEntity`, `NoteFtsEntity`.
   - DAOs: `NoteDao`, `CategoryDao`, `TagDao`.
   - `MindVaultDatabase` with starter database prepopulation callback.
4. **Repository & C++ Engine Bridge**:
   - `NoteRepository` for CRUD, query filters, and debounce autosave.
   - `NativeBridge` and `native-lib.cpp`.
5. **Layouts & Themes**:
   - Day & Night color resources and Material 3 theme.
   - `activity_main.xml`, fragment layouts (`fragment_home.xml`, `fragment_vault.xml`, `fragment_editor.xml`, `fragment_search.xml`, `fragment_explore.xml`, `fragment_settings.xml`).
   - `item_note.xml`, `item_category_chip.xml`, `item_tag_chip.xml`.
   - `nav_graph.xml` and bottom navigation menu.
6. **UI ViewModels & Fragment Implementation**:
   - `MainActivity`: setup toolbar and bottom navigation.
   - `HomeFragment`: greeting, stats overview, recent notes list.
   - `VaultFragment`: full note list, category filters, sorting, pin/archive toggles.
   - `EditorFragment`: interactive title, body, category selector, tag management, autosave.
   - `SearchFragment`: live FTS query execution and highlighting.
   - `ExploreFragment`: category cards, tag cloud.
   - `SettingsFragment`: theme picker, database stats, native engine status.
7. **Verification**:
   - Execute `compile_applet` to verify clean build.
   - Verify offline persistence, theme changes, search matching, and starter notes.
