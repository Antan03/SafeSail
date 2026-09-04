package no.uio.ifi.in2000.sofiaalo.team44.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SettingsUtil {
    private const val PREFS = "app_debug_settings"

    private fun prefs(context: Context): SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun setAnimationDelay(context: Context, ms: Int) { prefs(context).edit { putInt("animation_delay_ms", ms) } }
    fun getAnimationDelay(context: Context, default: Int = 500) = prefs(context).getInt("animation_delay_ms", default)

    fun setParticleCount(context: Context, count: Int) { prefs(context).edit { putInt("particle_count", count) } }
    fun getParticleCount(context: Context, default: Int = 1000) = prefs(context).getInt("particle_count", default)

    fun setLineOpacity(context: Context, opacity: Float) { prefs(context).edit { putFloat("line_opacity", opacity) } }
    fun getLineOpacity(context: Context, default: Float = 0.08f) = prefs(context).getFloat("line_opacity", default)

    fun setThresholdKm(context: Context, km: Float) { prefs(context).edit { putFloat("threshold_km", km) } }
    fun getThresholdKm(context: Context, default: Float = 50f) = prefs(context).getFloat("threshold_km", default)
}
