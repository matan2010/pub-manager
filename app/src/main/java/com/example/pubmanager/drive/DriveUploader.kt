package com.example.pubmanager.drive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File as JavaFile

object DriveUploader {

    private fun driveService(context: Context): Drive {
        val account = GoogleSignIn.getLastSignedInAccount(context)
            ?: error("No signed-in Google account")

        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        ).apply {
            selectedAccount = account.account
        }

        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("PubManager")
            .build()
    }

    suspend fun uploadXlsx(
        context: Context,
        localFile: JavaFile,
        driveFileName: String = localFile.name,
        folderId: String? = null
    ): String = withContext(Dispatchers.IO) {

        require(localFile.exists()) { "File not found: ${localFile.absolutePath}" }

        val meta = File().apply {
            name = driveFileName
            if (!folderId.isNullOrBlank()) {
                parents = listOf(folderId)
            }
        }

        val media = FileContent(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            localFile
        )

        val created = driveService(context)
            .files()
            .create(meta, media)
            .setFields("id,name,parents")
            .execute()

        created.id
    }

    suspend fun uploadFile(
        context: Context,
        localFile: JavaFile,
        mimeType: String,
        driveFileName: String = localFile.name,
        folderId: String? = null
    ): String = withContext(Dispatchers.IO) {

        require(localFile.exists()) { "File not found: ${localFile.absolutePath}" }

        val meta = File().apply {
            name = driveFileName
            if (!folderId.isNullOrBlank()) {
                parents = listOf(folderId)
            }
        }

        val media = FileContent(mimeType, localFile)

        val created = driveService(context)
            .files()
            .create(meta, media)
            .setFields("id,name,parents")
            .execute()

        created.id
    }

    suspend fun findFolderIdByName(
        context: Context,
        folderName: String
    ): String? = withContext(Dispatchers.IO) {

        val result = driveService(context).files().list()
            .setQ("mimeType='application/vnd.google-apps.folder' and name='${folderName.replace("'", "\\'")}' and trashed=false")
            .setSpaces("drive")
            .setFields("files(id,name)")
            .execute()

        result.files?.firstOrNull()?.id
    }
}
