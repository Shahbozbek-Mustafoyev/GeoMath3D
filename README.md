# GeoMath 3D — Android App

Matematika darsliklari uchun interaktiv 3D shakllar ilovasi.
Jetpack Compose · Kotlin · Material 3

---

## Loyiha tuzilmasi

```
GeoMath3D/
├── app/src/main/java/com/geomath3d/
│   ├── MainActivity.kt              ← Navigation + BottomBar
│   ├── MainViewModel.kt             ← UI state management
│   ├── data/
│   │   └── ShapeData.kt             ← Modellar + formulalar
│   └── ui/
│       ├── theme/Theme.kt           ← Ranglar va Material3 tema
│       ├── components/
│       │   └── Shape3DCanvas.kt     ← Sof Canvas bilan 3D shakl (drag-to-rotate)
│       └── screens/
│           ├── CalculatorScreen.kt  ← Formula kalkulyatori + slayderlar
│           ├── TeacherScreen.kt     ← O'qituvchi paneli
│           └── ARPreviewScreen.kt   ← AR ko'rinish (placeholder)
├── app/build.gradle.kts
├── gradle/libs.versions.toml
└── settings.gradle.kts
```

---

## Android Studio da ochish

1. **Android Studio** Hedgehog (2023.1.1) yoki yangroq versiyasini oching
2. `File → Open` → `GeoMath3D` papkasini tanlang
3. Gradle sync kutib turing (~1-2 daqiqa)
4. Emulyator yoki haqiqiy qurilmada **Run** (`Shift+F10`)

### Minimal talablar
- Android SDK 26+ (Android 8.0)
- compileSdk 35
- Kotlin 2.0.21
- Jetpack Compose BOM 2024.09

---

## Xususiyatlar

### 1. Formula Kalkulyatori
- 5 ta shakl: Shar, Silindr, Konus, Kub, Piramida
- Slayder orqali o'lchamlarni o'zgartirish
- Hajm (V) va yuza (S) real vaqtda hisoblanadi
- **3D Canvas** — barmoq bilan aylantirish mumkin

### 2. O'qituvchi Paneli
- Sinf statistikasi (o'quvchi soni, o'zlashtirish, darslar)
- Dars rejalari va progress ko'rsatkich
- QR-kod orqali darsni ulashish

### 3. AR Ko'rinish (Preview)
- Shakllarni katta 3D formatda ko'rish
- Har bir shakl uchun formula va o'lchamlar
- Kamera integratsiyasi (kelajakda ARCore bilan to'ldirish)

---

## Keyingi qadamlar (kengaytirish uchun)

```
# ARCore integratsiya
implementation("io.github.sceneview:arsceneview:2.2.1")

# Room database (darslar saqlash)
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")

# DataStore (sozlamalar)
implementation("androidx.datastore:datastore-preferences:1.1.1")
```

---

## Texnologiyalar
| Texnologiya | Versiya | Maqsad |
|---|---|---|
| Kotlin | 2.0.21 | Asosiy til |
| Jetpack Compose | BOM 2024.09 | UI framework |
| Material 3 | 1.3.0 | Dizayn tizimi |
| ViewModel | 2.8.6 | UI state |
| Navigation Compose | 2.8.1 | Sahifalar |
| Canvas API | — | 3D shakl chizish |
