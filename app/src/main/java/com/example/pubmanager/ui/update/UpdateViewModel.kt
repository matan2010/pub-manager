package com.example.pubmanager.ui.update

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pubmanager.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File

data class UpdateState(
    val isBusy: Boolean = false,
    val message: String = "Ready",
    val updateAvailable: Boolean = false,
    val latestVersion: String? = null
)

class UpdateViewModel(app: Application) : AndroidViewModel(app) {

    private val owner = "matan2010"
    private val repo = "pub-manager"
    private val client = OkHttpClient()
    private val _state = MutableStateFlow(UpdateState())
    val state: StateFlow<UpdateState> = _state

    fun checkAndUpdate() {
        viewModelScope.launch {

            if (!_state.value.updateAvailable) {
                _state.value = _state.value.copy(message = "No update available")
                return@launch
            }

            _state.value = _state.value.copy(isBusy = true, message = "Downloading...")

            try {
                val (tagName, apkUrl) = withContext(Dispatchers.IO) { fetchLatestRelease() }

                val latest = tagName.removePrefix("v").trim()
                val apkFile = withContext(Dispatchers.IO) { downloadApk(apkUrl) }

                _state.value = _state.value.copy(message = "Opening installer...")

                // install intent רצוי שירוץ על Main
                withContext(Dispatchers.Main) {
                    openInstaller(apkFile)
                }

                _state.value = _state.value.copy(isBusy = false, message = "Installer opened")
            } catch (e: Exception) {
                val msg = e.message ?: e.toString()
                _state.value = _state.value.copy(
                    isBusy = false,
                    message = "Update failed: $msg"
                )
            }
        }
    }

    private fun fetchLatestRelease(): Pair<String, String> {
        val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
        val req = Request.Builder().url(url).build()

        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) {
                val errBody = res.body?.string()
                throw IllegalStateException("GitHub API error: ${res.code} ${errBody ?: ""}".trim())
            }

            val body = res.body?.string() ?: throw IllegalStateException("Empty response")
            val json = JSONObject(body)

            val tagName = json.getString("tag_name")
            val assets = json.getJSONArray("assets")
            if (assets.length() == 0) throw IllegalStateException("No APK asset in release")

            var downloadUrl: String? = null
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                val name = a.getString("name")
                if (name == "app-release.apk") {
                    downloadUrl = a.getString("browser_download_url")
                    break
                }
            }
            if (downloadUrl == null) throw IllegalStateException("No .apk asset found")

            return tagName to downloadUrl
        }
    }

    private fun downloadApk(apkUrl: String): File {
        val req = Request.Builder().url(apkUrl).build()
        val ctx = getApplication<Application>()

        val outFile = File(ctx.cacheDir, "update.apk")
        if (outFile.exists()) outFile.delete()

        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IllegalStateException("Download failed: ${res.code}")
            val bytes = res.body?.bytes() ?: throw IllegalStateException("Empty APK")
            outFile.writeBytes(bytes)
        }
        return outFile
    }

    private fun openInstaller(apkFile: File) {
        val ctx = getApplication<Application>()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (!ctx.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${ctx.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(intent)

                _state.value = _state.value.copy(
                    isBusy = false,
                    message = "אשר התקנה ממקור לא ידוע ואז לחץ שוב על Update"
                )
                return
            }
        }

        val uri = FileProvider.getUriForFile(
            ctx,
            "${ctx.packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newRawUri("apk", uri)
        }
        ctx.startActivity(intent)
    }

    fun checkOnly() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true, message = "Checking latest release...")

            try {
                val (tagName, _) = withContext(Dispatchers.IO) { fetchLatestRelease() }

                val latest = tagName.removePrefix("v").trim()
                val current = BuildConfig.VERSION_NAME.removePrefix("v").trim()

                if (latest == current) {
                    _state.value = _state.value.copy(
                        isBusy = false,
                        message = "✅ Up to date ($current)",
                        updateAvailable = false,
                        latestVersion = latest
                    )
                } else {
                    _state.value = _state.value.copy(
                        isBusy = false,
                        message = "⬆️ Update available: $latest",
                        updateAvailable = true,
                        latestVersion = latest
                    )
                }
            } catch (e: Exception) {
                val msg = e.message ?: e.toString()
                _state.value = _state.value.copy(
                    isBusy = false,
                    message = "Check failed: $msg"
                )
            }
        }
    }
}



