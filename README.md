# ORI — Jetpack Compose port (full project)

This is now a complete, self-contained Android Studio project — not just
loose source files. It has everything Android Studio needs to recognize
and sync it as a Gradle project:

```
ori-app/
├── settings.gradle.kts          # declares the :app module
├── build.gradle.kts             # root — plugin versions
├── gradle.properties
├── gradlew / gradlew.bat        # wrapper launcher scripts
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts         # module — dependencies
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/values/strings.xml, themes.xml
        └── java/com/example/ori/
            ├── MainActivity.kt
            ├── navigation/OriNavGraph.kt
            ├── ui/screens/  (LoginScreen, SignUpScreen, ModeSelectionScreen, ContactsScreen)
            └── ui/theme/    (Color, Type, Theme)
```

## ⚠️ One thing I could not include: `gradle-wrapper.jar`

That file is a compiled binary, and I don't have network/build access in
this environment to generate or download it. Without it, the `gradlew` /
`gradlew.bat` scripts won't run on their own yet. **This is a two-minute
fix** — pick whichever is easiest for you:

- **Easiest — let Android Studio do it:** just open the project (see
  below). Android Studio bundles its own Gradle and, on first sync, will
  either regenerate the missing wrapper jar automatically or prompt you
  to fix/upgrade the wrapper — click through that prompt and it'll sort
  itself out.
- **If you have Gradle installed separately:** open a terminal in the
  `ori-app` folder and run:
  ```
  gradle wrapper --gradle-version 8.7
  ```
  That generates `gradle/wrapper/gradle-wrapper.jar` for you.

## How to open and run it

1. **Unzip** `ori-app.zip` somewhere simple, e.g. `C:\Users\Daksh\AndroidStudioProjects\ori-app`
   (avoid nesting it inside another folder that also has a project in it
   — that's what caused the "related Gradle project not linked" warning
   you saw before).
2. In Android Studio: **File → Open**, and select the `ori-app` folder
   itself (the one containing `settings.gradle.kts`) — not a subfolder.
3. Let Gradle sync run. If it complains about the wrapper jar, see the
   note above — click through Android Studio's own fix prompt, or run
   the `gradle wrapper` command.
4. Once sync finishes with no red errors, go to **Tools → Device
   Manager** and create a virtual device (any recent Pixel + system
   image) if you don't already have one — or plug in a phone with USB
   debugging enabled.
5. Hit the green **Run ▶** button (or `Shift+F10`). It should build and
   launch straight into the Login screen.

## App flow

```
Login  --[Login button]-->  Mode  -->  Contacts
  |
  '--[tap "Sign Up" link]--> Sign Up --[Sign Up button]--> Mode
```
Sign Up is only reachable by tapping the blue link on Login, and a
successful sign-up forwards straight to Mode (same as a successful
login).

## Notes / things you'll likely want to tweak

- **Fonts**: the mockups use Inter. `ui/theme/Type.kt` currently falls
  back to `FontFamily.Default`; drop the Inter `.ttf` files into
  `app/src/main/res/font` and build a `FontFamily` from them for a
  pixel-exact match.
- **App icon**: the manifest points at a placeholder system icon
  (`@android:drawable/sym_def_app_icon`). Add your own via **Right-click
  res → New → Image Asset** in Android Studio, then update the
  `android:icon` line in `AndroidManifest.xml`.
- **Avatars**: `ContactsScreen.kt` uses placeholder circles where the
  HTML had remote `<img>` avatars — swap in Coil's `AsyncImage` once you
  have real avatar URLs (add `implementation("io.coil-kt:coil-compose:2.6.0")`).
- **Auth wiring**: `onLoginSuccess` / `onSignUpSuccess` currently just
  navigate straight to Mode — hook in your real network call and only
  navigate on success.
- **SOS Mode**: `onSosModeClick` in `OriNavGraph.kt` is a no-op stub —
  wire it up to your emergency-broadcast flow. The pulsing red ring
  animation from the HTML wasn't ported either — happy to add an
  `infiniteRepeatable` Compose animation for that if you want it.
