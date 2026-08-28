# MafiaApp

Android client for MafiaApp — a rating tracker for a Mafia board game club. Players are
ranked by MMR earned across games; admins record match results and the leaderboard
updates automatically.

**Web client:** https://github.com/JeffreyIhesinulo/MafiaAppWeb — live at https://mafiaapp-819fd.web.app

Both clients share one Firebase backend and are independent of each other — neither
needs the other to be deployed or running.

## Features

- Email/password auth with mandatory email verification and password reset
- Admin approval queue — new accounts can't sign in until an admin approves them
- Leaderboard with MMR, rank tiers and win/loss records
- Game creation: assign players to roles (Citizen, Sheriff, Mafia, Don), record the
  result, MMR is calculated and distributed automatically
- Game history with per-player MMR changes
- Player profiles with an MMR-over-time graph
- Activity feed with admin announcements

## Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3
- **Navigation:** Navigation Compose
- **Backend:** Firebase Authentication + Cloud Firestore
- **Async:** Coroutines (`kotlinx-coroutines-play-services` for `Task.await()`)
- **Min SDK:** 26 · **Target SDK:** 36

## Security model

`google-services.json` contains no secrets — the project ID and Android API key are
public identifiers that ship inside every APK. Access control lives in Firestore
Security Rules, not in the client:

- Users can read their own document always; reading the player list requires an
  approved account, so an unapproved sign-up can't enumerate other players
- `isAdmin` and `approved` can only be written by an existing admin — a user creating
  their own document is constrained to `approved: false`, `isAdmin: false` and zeroed
  stats
- Username uniqueness is enforced by a `usernames/{name}` lock collection: `create`
  succeeds only if the document doesn't exist, which makes it atomic and free of the
  check-then-write race a query-based check would have
- Games and activity entries are writable by admins only; deletes are disabled outright

The API key is additionally restricted by package name + SHA-1 fingerprint in the
Google Cloud Console.

## Project structure

```
app/src/main/java/io/github/jeffreyihesinulo/mafiaapp/
├── MainActivity.kt          # Entry point
├── AppNavigation.kt         # Routes and bottom navigation
├── *Screen.kt               # Compose UI per screen
├── *Repository.kt           # Firestore access layer
└── ui/theme/                # Colors, typography, theme
```

Screens talk to repositories; repositories are the only layer touching Firebase.

## Setup

### 1. Prerequisites

- Android Studio (Ladybug or newer)
- JDK 17+
- A device or emulator running API 26+

### 2. Firebase configuration

The app needs its own Android app registered in the Firebase project. In the Firebase
Console:

**Project Settings → General → Your apps → Add app → Android**

Package name must match `applicationId` in `app/build.gradle.kts` exactly. Download the
resulting `google-services.json` and place it in `app/`.

If the API key is restricted by fingerprint, add your debug SHA-1 to the allowed list:

```bash
./gradlew signingReport
```

Take the SHA-1 from `Variant: debug` and add it in Google Cloud Console under
**APIs & Services → Credentials → [Android key] → Android apps**. Release builds need
their release SHA-1 added the same way, or auth will fail with `API key not valid`.

### 3. Build and run

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and hit Run.

### 4. Admin access

`isAdmin` cannot be granted from within the app by design — the security rules only
allow an existing admin to set it. Bootstrap the first admin by editing the user
document directly in the Firestore Console.

## Implementation notes

- `DocumentSnapshot.getBoolean()` returns `null` when the field or document is missing,
  so approval is checked with `!= true` rather than `== false` — the latter let accounts
  with no Firestore document through.
- Registration creates the auth account *before* querying Firestore, because the
  security rules require `request.auth != null`. On failure the account is rolled back
  with `user.delete()`.
- MMR updates use `FieldValue.increment()` rather than read-modify-write, so concurrent
  game submissions don't clobber each other.
- After registration the session is signed out deliberately — the account still needs
  email verification and admin approval before it can be used.

## Roadmap

- Unit tests for MMR calculation and rank thresholds
- Release signing config and Play Store listing
- Firebase App Check
