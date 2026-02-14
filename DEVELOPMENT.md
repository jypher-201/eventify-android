# Eventify Development Log

Detailed record of development process, challenges, solutions, and learnings.

---

## 📊 Development Statistics

| Metric | Value |
|--------|-------|
| **Days Completed** | 1 |
| **Total Hours** | 4 |
| **Lines of Code** | ~120 |
| **Commits** | 1 |
| **Features Completed** | Interactive Compose UI |
| **Bugs Fixed** | 2 |
| **Concepts Learned** | 25+ |

---

## Day 1: Project Setup & Jetpack Compose Basics
**Date:** February 14, 2025  
**Time Spent:** 4 hours  
**Status:** ✅ Complete

### 🎯 Goals
- [x] Install and configure Android Studio
- [x] Create Jetpack Compose project
- [x] Understand Compose fundamentals
- [x] Implement interactive UI with state
- [x] Setup Git and GitHub
- [x] Create professional documentation

---

### 📝 What I Built

#### Hour 1: Environment Setup
**Tasks:**
- Installed Android Studio Hedgehog
- Downloaded Android SDK API 34
- Created Pixel 6 emulator (API 34)
- Configured project with Kotlin DSL

**Project Configuration:**
```
Name: Eventify
Package: com.j4.eventify
Language: Kotlin
Min SDK: API 24 (Android 7.0)
Target SDK: API 34
UI: Jetpack Compose
Build: Kotlin DSL
```

---

#### Hour 2-3: Jetpack Compose Implementation

**MainActivity.kt - Complete Implementation:**
```kotlin
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var clickCount by remember { mutableIntStateOf(0) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Welcome to $name! 🎉",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Text(
                text = "Clicked: $clickCount times",
                fontSize = 16.sp,
                color = Color.Gray
            )
            
            Button(onClick = {
                clickCount++
                Toast.makeText(
                    context,
                    "Welcome to Eventify! 🎉",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
                Text("Get Started")
            }
        }
    }
}
```

**Features Implemented:**
- ✅ Reactive state management
- ✅ Click counter with auto-update
- ✅ Toast feedback system
- ✅ Custom styling (colors, typography)
- ✅ Centered responsive layout
- ✅ Material 3 components

---

#### Hour 4: Version Control Setup

**Git Commands Used:**
```bash
# Initial setup
git config --global user.name "Your Name"
git config --global user.email "your@email.com"
git init

# First commit
git add .
git commit -m "Initial commit: Day 1 - Jetpack Compose project with interactive UI"

# GitHub connection
git remote add origin https://github.com/USERNAME/eventify-android.git
git branch -M main
git push -u origin main
```

**Repository Created:**
- URL: `https://github.com/YOUR_USERNAME/eventify-android`
- Visibility: Public
- Includes: README.md, DEVELOPMENT.md, .gitignore

---

### 🧠 Concepts Learned

#### 1. Jetpack Compose Fundamentals

**Declarative UI:**
```kotlin
// Traditional Views (Imperative)
val textView = TextView(context)
textView.text = "Hello"
textView.setTextColor(Color.BLACK)
parent.addView(textView)

// Jetpack Compose (Declarative)
Text(
    text = "Hello",
    color = Color.Black
)
```

**@Composable Functions:**
- Functions that emit UI
- Can call other composables
- Recompose when state changes
- Annotated with `@Composable`

**State Management:**
```kotlin
// Creates observable state
var count by remember { mutableIntStateOf(0) }

// When count changes, UI automatically updates!
Text("Count: $count")

// Update state
count++  // Triggers recomposition
```

**Modifiers:**
```kotlin
// Chain styling and behavior
modifier
    .fillMaxSize()           // Size
    .background(Color.Gray)  // Background
    .padding(16.dp)          // Spacing
```

---

#### 2. Compose Components Used

**Layout Composables:**
- `Box { }` - Overlay/stack container (like FrameLayout)
- `Column { }` - Vertical stack (like LinearLayout vertical)
- `Row { }` - Horizontal stack (like LinearLayout horizontal)

**Material 3 Components:**
- `Text()` - Display text
- `Button()` - Clickable button
- `Scaffold()` - App structure container

**Modifiers:**
- `.fillMaxSize()` - Fill parent
- `.background()` - Set background color
- `.padding()` - Add spacing
- `Alignment.Center` - Center content
- `Arrangement.spacedBy()` - Space between items

---

#### 3. Kotlin Concepts

**Property Delegation (`by`):**
```kotlin
var count by remember { mutableIntStateOf(0) }
// 'by' delegates getting/setting to the State object
```

**Lambda Expressions:**
```kotlin
Button(onClick = {
    // This is a lambda
    count++
}) {
    Text("Click")
}
```

**String Interpolation:**
```kotlin
val name = "Eventify"
Text("Welcome to $name!")  // "Welcome to Eventify!"
```

**Named Parameters:**
```kotlin
Text(
    text = "Hello",
    fontSize = 20.sp,
    color = Color.Black
)
```

---

### ⚠️ Challenges & Solutions

#### Challenge 1: Unresolved Reference `spacedBy`
**Problem:** Red error on `Arrangement.spacedBy(16.dp)`  
**Cause:** Missing import for `Arrangement`  
**Solution:**
```kotlin
import androidx.compose.foundation.layout.Arrangement
```
**Learning:** Compose requires explicit imports for every component. Use Alt+Enter for quick import.

---

#### Challenge 2: Duplicate Import Warning
**Problem:** Imported `Arrangement.spacedBy` separately  
**Cause:** Redundant specific import when parent was already imported  
**Solution:** Remove specific import, keep parent:
```kotlin
// ✅ Keep this
import androidx.compose.foundation.layout.Arrangement

// ❌ Remove this
import androidx.compose.foundation.layout.Arrangement.spacedBy
```
**Learning:** Import parent classes, not individual members.

---

#### Challenge 3: State Not Updating UI
**Problem:** Counter variable changed but UI didn't update  
**Cause:** Used regular `var` instead of `mutableStateOf`  
**Solution:**
```kotlin
// ❌ Wrong
var count = 0  // Changes don't trigger recomposition

// ✅ Correct
var count by remember { mutableIntStateOf(0) }  // Observable state
```
**Learning:** Compose needs observable state to know when to recompose.

---

#### Challenge 4: Performance Warning
**Problem:** Android Studio suggested `mutableIntStateOf` instead of `mutableStateOf`  
**Cause:** Generic state boxing integers  
**Solution:**
```kotlin
// Before (works but slower)
var count by remember { mutableStateOf(0) }

// After (optimized for Int)
var count by remember { mutableIntStateOf(0) }
```
**Learning:** Use specialized state types for primitives:
- `mutableIntStateOf()` for Int
- `mutableFloatStateOf()` for Float
- `mutableLongStateOf()` for Long
- `mutableDoubleStateOf()` for Double
- `mutableStateOf()` for objects, String, Boolean

---

#### Challenge 5: GitHub Authentication
**Problem:** Git push rejected password  
**Cause:** GitHub deprecated password authentication  
**Solution:** Created Personal Access Token:
1. GitHub → Settings → Developer Settings
2. Personal Access Tokens → Generate new token
3. Select `repo` scope
4. Use token as password when pushing
   **Learning:** Modern Git authentication uses tokens, not passwords.

---

### 💡 Key Takeaways

#### Compose vs XML Views

| Aspect | XML Views | Jetpack Compose |
|--------|-----------|-----------------|
| UI Definition | XML files | Kotlin functions |
| State Updates | Manual (setText) | Automatic (recomposition) |
| Preview | Run app | Live preview |
| Type Safety | Runtime errors | Compile-time errors |
| Learning Curve | Separate XML + Kotlin | Pure Kotlin |
| Performance | Mature, optimized | Newer, improving |

#### Best Practices Discovered
1. ✅ Use `val` by default (immutable)
2. ✅ Name composables with PascalCase
3. ✅ Use specialized state types (`mutableIntStateOf`)
4. ✅ Keep composables small and focused
5. ✅ Extract reusable components
6. ✅ Use preview functions for rapid development
7. ✅ Listen to Android Studio warnings

#### Development Workflow
```
1. Write composable function
2. Check live preview
3. Run on emulator/device
4. Test interactions
5. Commit working code
6. Push to GitHub
```

---

### 📚 Resources Used

**Official Documentation:**
- [Jetpack Compose Basics](https://developer.android.com/jetpack/compose/tutorial)
- [State in Compose](https://developer.android.com/jetpack/compose/state)
- [Material 3 Compose](https://developer.android.com/jetpack/compose/designsystems/material3)

**Tools:**
- Android Studio Hedgehog (2023.1.1)
- Git 2.43.0
- GitHub
- Pixel 6 Emulator (API 34)

**Learning Resources:**
- Android Compose Codelabs
- Compose documentation
- Stack Overflow
- YouTube: Philipp Lackner, Coding in Flow

---

### 📅 Tomorrow's Plan (Day 2)

#### Goals:
- [ ] Create custom color palette in `Color.kt`
- [ ] Implement neo-brutalism color scheme
- [ ] Update `Theme.kt` with custom colors
- [ ] Create reusable composable components
- [ ] Learn advanced modifiers
- [ ] Practice Compose layouts

#### Expected Time: 3-4 hours

#### Expected Commits:
```bash
feat: Add neo-brutalism color system
feat: Create reusable UI components
refactor: Extract composables for reusability
```

---

### 🤔 Questions for Further Research

- [ ] How does Compose handle deep UI trees (performance)?
- [ ] When to use `derivedStateOf` vs `mutableStateOf`?
- [ ] What's the difference between `LaunchedEffect` and `DisposableEffect`?
- [ ] How to test composable functions?
- [ ] Best practices for Compose navigation?

---

### 📊 Daily Metrics

| Metric | Target | Actual | Status |
|--------|--------|--------|--------|
| Hours Spent | 4 | 4 | ✅ |
| Commits | 1+ | 1 | ✅ |
| Features | 1 | 1 | ✅ |
| Documentation | 2 files | 2 files | ✅ |
| GitHub Push | Yes | Yes | ✅ |
| Bugs | 0 | 2 fixed | ✅ |

---

### 🎯 Self-Assessment

**What Went Well:**
- ✅ Quick understanding of Compose concepts
- ✅ Successfully implemented state management on first try
- ✅ Clean, readable code structure
- ✅ Professional documentation from Day 1
- ✅ Git workflow established smoothly

**What Was Challenging:**
- ⚠️ Managing many imports (solved with Alt+Enter)
- ⚠️ Understanding state vs regular variables
- ⚠️ GitHub token authentication (but learned it!)

**What I'll Do Better Tomorrow:**
- 💡 Read relevant docs before coding
- 💡 Use preview more to avoid running app constantly
- 💡 Make smaller, more frequent commits
- 💡 Comment complex logic as I write it

**Confidence Level:** 🟢 High (8/10)

**Motivation:** 🔥🔥🔥 Very High

---

### 🏆 Day 1 Achievements Unlocked

- 🎖️ **First Compose App** - Built reactive UI
- 🎖️ **State Master** - Implemented observable state
- 🎖️ **Git Guru** - Initialized repo and pushed to GitHub
- 🎖️ **Documentation Pro** - Created professional docs
- 🎖️ **Problem Solver** - Fixed 2 bugs independently

---

**Next Entry:** Day 2 - Color System & Theming  
**Status:** 🟢 On Track  
**Days Until MCO1 Deadline:** 16 days  
**Current Phase 1 Progress:** 10%

---

## Day 2: Neo-Brutalism Color System & Custom Components
**Date:** February 15, 2025  
**Time Spent:** 4 hours  
**Status:** ✅ Complete

### 🎯 Goals
- [x] Create custom color palette
- [x] Update app theme
- [x] Build reusable EventCard component
- [x] Create FilterChip component
- [x] Implement filter functionality
- [x] Add dummy data
- [x] Build complete home screen

---

### 📝 What I Built

#### Hour 1-2: Color System
**Created comprehensive color palette in `Color.kt`:**
- Base colors (Black, White, Grays)
- Event type colors (Blue, Pink, Yellow) with variants
- Badge colors for type indicators
- UI element colors (FAB, Success, Error, etc.)

**Updated `Theme.kt` with custom Material 3 color scheme:**
- EventifyLightColorScheme
- EventifyDarkColorScheme (for future)
- Fixed deprecated status bar API
- Enabled edge-to-edge design

#### Hour 3: EventCard Component
**Built neo-brutalism event card with:**
- Shadow effect using offset Box
- Thick black border (4dp)
- Dynamic colors based on event type
- Type badge with contrasting color
- Large countdown display
- Underline decoration
- Rounded corners (12dp)

**Features:**
```kotlin
@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
)
```

#### Hour 4: Complete Home Screen
**Components created:**
1. **EventFilterChip** - Filter buttons with selection state
2. **EventifyTopBar** - Black curved header with title and filters
3. **EventList** - LazyColumn with event cards
4. **EmptyState** - "No events found" message
5. **EventifyFAB** - Red circular floating action button

**State management:**
```kotlin
var selectedFilter by remember { mutableStateOf<EventType?>(null) }
```

**Filtering logic:**
```kotlin
val filteredEvents = if (selectedFilter != null) {
    DummyData.events.filter { it.type == selectedFilter }
} else {
    DummyData.events
}
```

---

### 🧠 Concepts Learned

#### Advanced Modifiers

**Modifier Chaining:**
```kotlin
Modifier
    .fillMaxWidth()              // Size
    .offset(x = 6.dp, y = 6.dp) // Position
    .clip(RoundedCornerShape(12.dp))  // Shape
    .background(Black)           // Color
    .border(4.dp, Black)        // Border
    .padding(20.dp)             // Spacing
```

**Order matters!** Each modifier applies to the result of the previous one.

#### Shadow Effect Technique
```kotlin
Box {
    // Shadow (behind)
    Box(Modifier.offset(x = 6.dp, y = 6.dp).background(Black))
    
    // Content (in front)
    Card { }
}
```

Neo-brutalism shadows = offset solid-color box!

#### When Expressions
```kotlin
val color = when (type) {
    EventType.ACADEMIC -> AcademicBlue
    EventType.PERSONAL -> PersonalPink
    EventType.OCCASION -> OccasionYellow
}
```

Like switch statements but returns a value.

#### LazyColumn (RecyclerView in Compose)
```kotlin
LazyColumn {
    items(events) { event ->
        EventCard(event = event)
    }
}
```

Efficient list rendering - only renders visible items!

#### Preview Functions
```kotlin
@Preview(showBackground = true)
@Composable
fun MyPreview() {
    EventifyTheme {
        MyComponent()
    }
}
```

See UI without running the app!

---

### ⚠️ Challenges & Solutions

#### Challenge 1: Unresolved Reference 'Color'
**Problem:** `Color(0xFF...)` didn't work in Theme.kt  
**Cause:** Missing `import androidx.compose.ui.graphics.Color`  
**Solution:** Added import at top of Theme.kt  
**Learning:** Even when using custom colors from Color.kt, still need to import Color class

#### Challenge 2: Deprecated Status Bar API
**Problem:** `window.statusBarColor` showed deprecation warning  
**Cause:** Old API from Views system  
**Solution:** Used `WindowCompat.setDecorFitsSystemWindows()`  
**Learning:** Material 3 prefers edge-to-edge with transparent status bar

#### Challenge 3: Shadow Not Showing
**Problem:** Shadow box invisible  
**Cause:** Same size as main box (overlapped perfectly)  
**Solution:** Used `.offset()` to move shadow  
**Learning:** Offset creates the 3D effect in neo-brutalism

#### Challenge 4: Filter Not Working
**Problem:** Clicking chips didn't filter events  
**Cause:** Forgot to update `selectedFilter` state  
**Solution:** Properly toggle state on click  
**Learning:** UI updates only when state changes

---

### 💡 Key Takeaways

#### Neo-Brutalism Design Principles
1. **Bold colors** - High saturation, no pastels
2. **Thick borders** - 3-4dp minimum
3. **Sharp shadows** - Solid color, not blur
4. **High contrast** - Black on bright colors
5. **Geometric shapes** - Rectangles, circles (no complex shapes)

#### Compose Best Practices Discovered
1. ✅ Extract reusable components early
2. ✅ Use preview functions extensively
3. ✅ Keep composables small and focused
4. ✅ Use `when` for conditional styling
5. ✅ Leverage Modifier chaining
6. ✅ Use `remember` for local state
7. ✅ Use data classes for structured data

#### Component Design Pattern
```kotlin
@Composable
fun MyComponent(
    data: DataClass,              // Required data
    modifier: Modifier = Modifier, // Always include
    onClick: () -> Unit = {}      // Optional callbacks
) {
    // Implementation
}
```

---

### 📚 New Compose Components Used

| Component | Purpose | Key Properties |
|-----------|---------|----------------|
| `LazyColumn` | Efficient scrolling list | `items()`, `contentPadding` |
| `Scaffold` | App structure | `topBar`, `floatingActionButton` |
| `FloatingActionButton` | Floating action | `containerColor`, `elevation` |
| `Box` | Layering/positioning | `contentAlignment`, `matchParentSize` |
| `Row` | Horizontal layout | `horizontalArrangement` |
| `Column` | Vertical layout | `verticalArrangement` |

---

### 🎨 Design Decisions

**Why these colors?**
- Blue (Academic) = Trust, professionalism
- Pink (Personal) = Energy, personal
- Yellow (Occasion) = Celebration, joy

**Why thick borders?**
- Visual hierarchy
- Neo-brutalism aesthetic
- Makes cards "pop"

**Why shadows instead of elevation?**
- More control over shadow appearance
- Consistent with neo-brutalism style
- Works better with thick borders

---

### 📊 Day 2 Metrics

| Metric | Target | Actual | Status |
|--------|--------|--------|--------|
| Hours Spent | 4 | 4 | ✅ |
| Components Created | 5+ | 7 | ✅ |
| Commits | 2+ | 2 | ✅ |
| Previews Created | 5+ | 8 | ✅ |
| Bugs | 0 | 4 fixed | ✅ |

---

### 🎯 Self-Assessment

**What Went Well:**
- ✅ Colors look amazing!
- ✅ Neo-brutalism style achieved
- ✅ Filter functionality works perfectly
- ✅ Preview functions speed up development
- ✅ Code is clean and reusable

**What Was Challenging:**
- ⚠️ Understanding modifier order
- ⚠️ Getting shadow positioning right
- ⚠️ Fixing deprecated APIs

**What I'll Do Better Tomorrow:**
- 💡 Plan component structure before coding
- 💡 Write preview functions first (TDD style)
- 💡 Use more Kotlin extensions

**Confidence Level:** 🟢 Very High (9/10)

---

### 📅 Tomorrow's Plan (Day 3)

#### Goals:
- [ ] Create Add Event screen layout
- [ ] Build text input fields
- [ ] Create date/time picker UI (dummy)
- [ ] Add save button
- [ ] Practice form layouts

**Estimated Time:** 3 hours

---

**Next Entry:** Day 3 - Add Event Screen  
**Status:** 🟢 Ahead of Schedule  
**Phase 1 Progress:** 25%