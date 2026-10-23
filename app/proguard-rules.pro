# Room Entities
-keep class com.industri.fleettrack.data.local.entity.** { *; }
-keep @androidx.room.Entity class * { *; }

# Retrofit DTOs & Gson
-keep class com.industri.fleettrack.data.remote.api.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Timber Logger
-keep class timber.log.** { *; }
-dontwarn timber.log.**
