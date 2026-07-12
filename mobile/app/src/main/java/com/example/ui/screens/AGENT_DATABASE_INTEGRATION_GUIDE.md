# AI Agent & Developer Database & Backend Integration Manual
### STAGE-1 DEPLOYMENT MANIFEST: DESIGN PRESERVATION & SYSTEM ARCHITECTURE
**App Context:** Engraced Smile Premium Delivery App (VIP Transport Logistics)
**Design System Vibe:** Luxury Dark & High-Contrast Premium Gold M3 Aesthetic

---

## 🛑 MANDATORY RULE: THE DESIGN PRESERVATION OATH
As an AI Coding Agent or Developer, you are **STRICTLY FORBIDDEN** from altering any visual characteristics of the user interface during backend or database integration. 
- **NO SHAPE MODIFICATIONS:** Keep all card corners strictly at `RoundedCornerShape(24.dp)` and bottom panels at `RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)`.
- **NO COLOR RE-MAPPINGS:** All text, cards, and layouts must dynamically map to `AppBackground`, `AppSurface`, `Charcoal`, `Gold`, and `Obsidian`. Under no circumstances are you to introduce generic gray backgrounds or white-on-gold/gold-on-white high contrast violations.
- **NO LAYOUT DISPLACEMENT:** The **Driver Agent Card** must stay pinned, fixedly docked at the absolute bottom (`Alignment.BottomCenter` inside the root container) on the track screen. Under no circumstance should it be placed inside scrolls, rows, or floating drawers.

---

## 1. 🗺️ REAL MAPBOX SDK INTEGRATION PROTOCOL
To transition the simulated `MapCanvas` to a live Mapbox SDK implementation, follow these exact technical steps.

### Step 1.1: Build Configuration (`gradle/libs.versions.toml`)
Ensure the Mapbox SDK version coordinates are defined:
```toml
[versions]
mapbox = "11.3.0"

[libraries]
mapbox-android = { group = "com.mapbox.maps", name = "android", version.ref = "mapbox" }
```

### Step 1.2: Repository Injection (`settings.gradle.kts`)
Mapbox SDK requires a private downloads token. Inject the custom repository inside your repositories block:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"
                password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").getOrElse("")
            }
        }
    }
}
```
> **CRITICAL:** Store your private token in the **AI Studio Secrets panel** as `MAPBOX_DOWNLOADS_TOKEN` (never hardcode).

### Step 1.3: Jetpack Compose Bridge (`TrackingScreen.kt`)
Replace the standard `MapCanvas` call with an `AndroidView` wrapper. Maintain the design system by loading Mapbox's Dark or custom luxury theme:
```kotlin
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

@Composable
fun MapboxView(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    onMapReady: (MapView) -> Unit
) {
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                getMapboxMap().loadStyleUri(Style.DARK) {
                    onMapReady(this@apply)
                }
            }
        },
        modifier = modifier
    )
}
```

---

## 2. 🗄️ ROOM LOCAL PERSISTENCE SYSTEM (OFFLINE LAYER)
The local cache handles offline operations, instant startup, and configuration sync.

### Step 2.1: Entity Definitions (`ParcelEntity.kt`)
Keep Room database models in alignment with our UI State:
```kotlin
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parcels")
data class ParcelEntity(
    @PrimaryKey val id: String,
    val itemName: String,
    val weight: Double,
    val progress: Float,
    val pickupAddress: String,
    val deliveryAddress: String,
    val courierName: String,
    val courierAvatar: String,
    val status: String
)
```

### Step 2.2: DAO Implementation (`ParcelDao.kt`)
```kotlin
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcels")
    fun getAllParcels(): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM parcels WHERE id = :id")
    fun getParcelById(id: String): Flow<ParcelEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateParcel(parcel: ParcelEntity)
}
```

---

## 3. ☁️ BACKEND DATABASE BINDING CONTRACT (FIREBASE / SPANNER)
For live multiplayer tracking and sync:

### Step 3.1: Firebase Firestore Connection (`DeliveryViewModel.kt`)
Bind the viewmodel states directly to real-time Snapshot Listeners so progress bar updates glide smoothly:
```kotlin
class DeliveryViewModel(private val parcelDao: ParcelDao) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    // Expose as StateFlow to UI
    private val _selectedParcel = MutableStateFlow<Parcel?>(null)
    val selectedParcel = _selectedParcel.asStateFlow()

    fun startRealtimeTracking(parcelId: String) {
        firestore.collection("parcels").document(parcelId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val item = snapshot.toObject(Parcel::class.java)
                _selectedParcel.value = item
                
                // Immediately cache locally in Room
                viewModelScope.launch(Dispatchers.IO) {
                    if (item != null) {
                        parcelDao.insertOrUpdateParcel(item.toEntity())
                    }
                }
            }
    }
}
```

---

## 4. 🔑 SECURITY AND ENV CONFIGURATION
Access keys must remain secure and dynamically fed into the build via `.env` injection.
- **SECRET INJECTION:** Add API credentials to your `.env` file (e.g., `MAPBOX_ACCESS_TOKEN=pk.eyJ...`).
- **BUILDCONFIG COUPLING:** Access variables at runtime using `com.example.BuildConfig.MAPBOX_ACCESS_TOKEN`.
- **NULL/EMPTY CHECKS:** Always verify key existence at application launch. If empty, fallback gracefully with a user-friendly custom toast indicating: *"Map configuration key missing. Running simulated visual stream."*

---

## 📜 RULES OF COMPILATION & TESTING
Before releasing any integration code, execute these local commands to ensure nothing is broken:
1. **Compilation Check:** Run `gradle :app:compileDebugKotlin` to verify type-safety.
2. **Screenshots Test:** Run `gradle :app:verifyRoborazziDebug` to verify that your layout remains mathematically symmetrical and identical.

**SYSTEM ENGINE CONFIRMED ACTIVE. DO NOT ALTER DESIGN MARGINS.**
