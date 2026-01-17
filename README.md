
# WIFIbuhera – Release APK build útmutató (Android 15)

## 1) Gyors build és telepíthető Release APK (debug keystore-rel)
A projekt úgy van beállítva, hogy **ha nincs saját release kulcs megadva**, akkor a `release` build **a rendszer debug keystore-jával** lesz aláírva (telepíthető, de Play-re nem való).

- Android Studio: `Build > Generate Signed Bundle / APK...` helyett elég a `Build > Build APK(s)` és válaszd a `release` variánst.
- Parancssor: `./gradlew assembleRelease`
  - Az APK: `app/build/outputs/apk/release/app-release.apk`

> A debug keystore a gépeden: `~/.android/debug.keystore` (jelszó: `android`, alias: `androiddebugkey`).

## 2) Saját, éles release kulccsal aláírt APK
Adj meg egy saját keystore-t a projekt `gradle.properties`-ében (a projekt gyökerében hozd létre):

```
RELEASE_STORE_FILE=/abszolut/elérési/út/keystore.jks
RELEASE_STORE_PASSWORD=***
RELEASE_KEY_ALIAS=***
RELEASE_KEY_PASSWORD=***
```

Ezután: `./gradlew clean assembleRelease` → **WIFIbuhera-1.0-release.apk** (telepíthető, éles kulccsal).

### Keystore létrehozása (Java keytool)
```
keytool -genkeypair -v  -keystore keystore.jks -storetype JKS -keyalg RSA -keysize 2048 -validity 10000  -alias wifibuhera
```

## 3) HyperOS/MIUI beállítások (stabil háttérfutáshoz)
- Akkumulátor-optimalizálás: **Ne optimalizálja** az appot.
- Automatikus indítás: engedélyezd.
- Háttértevékenység engedélyezése.
- Wi‑Fi alvó módban: **Soha**.

## 4) Név és csomag
- App neve: **WIFIbuhera**
- Csomagazonosító: **hu.zoli.wifibuhera**
```
```
