# PocketMapsNG

A libGDX-based offline navigation app. This is a **pure Java/Gradle project** (no npm, no Node.js).

## Build & Run

All commands run from `pocketmaps/` directory:

```bash
cd pocketmaps
./gradlew desktop:run    # Run desktop app
./gradlew desktop:build  # Build desktop JAR
./gradlew html:superdev  # Run HTML GWT dev server
./gradlew html:war       # Build HTML WAR
```

## Project Structure

| Module | Purpose |
|--------|---------|
| `core/` | Main app logic, views, navigator, map handling |
| `desktop/` | LWJGL3 desktop launcher |
| `android/` | Android backend |
| `html/` | GWT web client |
| `ios/` | RoboVM iOS backend |
| `routing-engine-gh/` | GraphHopper routing implementation |
| `gps-module/` | GPS client implementations |
| `starcom-base/` | Shared utilities |
| `tts-engine-espeakcmd/` | espeak TTS backend |

**Entry point**: `pocketmaps/desktop/src/com/mygdx/game/DesktopLauncher.java`

## Notes

- Uses **libGDX 1.12.0**, **VTM 0.21.0** (mapsforge), **GraphHopper** for routing
- No test directories exist; no test commands available
- No linter or typechecker configured
- `scripts/build_maps.sh` - map generation scripts
