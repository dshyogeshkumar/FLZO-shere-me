package com.example.file

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.model.FileCategory
import com.example.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalFileManager(private val context: Context) {

    suspend fun queryFilesByCategory(category: FileCategory): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()

        when (category) {
            FileCategory.ALL -> {
                result.addAll(queryMediaImages())
                result.addAll(queryMediaVideos())
                result.addAll(queryMediaAudio())
                result.addAll(queryMediaDocuments())
                result.addAll(queryApkFiles())
                result.addAll(queryZipFiles())
                result.addAll(queryAppStorageFiles())
            }
            FileCategory.PHOTOS -> result.addAll(queryMediaImages())
            FileCategory.VIDEOS -> result.addAll(queryMediaVideos())
            FileCategory.MUSIC -> result.addAll(queryMediaAudio())
            FileCategory.DOCUMENTS -> result.addAll(queryMediaDocuments())
            FileCategory.APKS -> result.addAll(queryApkFiles())
            FileCategory.ZIPS -> result.addAll(queryZipFiles())
            FileCategory.FOLDERS -> result.addAll(queryFolders())
            FileCategory.OTHER -> result.addAll(queryOtherFiles())
        }

        // Include device demo files ONLY if real scanned storage was empty
        if (result.isEmpty()) {
            result.addAll(getDeviceRealGeneratedFiles())
        }

        result.sortedByDescending { it.lastModified }
    }

    private fun queryMediaImages(): List<FileItem> {
        val list = mutableListOf<FileItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_MODIFIED
        )
        try {
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_MODIFIED} DESC LIMIT 100"
            )
            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val mimeCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val name = it.getString(nameCol) ?: "Image_$id.jpg"
                    val size = it.getLong(sizeCol)
                    val mime = it.getString(mimeCol) ?: "image/jpeg"
                    val date = it.getLong(dateCol) * 1000
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    list.add(
                        FileItem(
                            id = "img_$id",
                            name = name,
                            path = uri.toString(),
                            size = if (size > 0) size else 2_450_000L,
                            mimeType = mime,
                            category = FileCategory.PHOTOS,
                            lastModified = date,
                            uriString = uri.toString()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun queryMediaVideos(): List<FileItem> {
        val list = mutableListOf<FileItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_MODIFIED
        )
        try {
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_MODIFIED} DESC LIMIT 50"
            )
            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val mimeCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val name = it.getString(nameCol) ?: "Video_$id.mp4"
                    val size = it.getLong(sizeCol)
                    val mime = it.getString(mimeCol) ?: "video/mp4"
                    val date = it.getLong(dateCol) * 1000
                    val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                    list.add(
                        FileItem(
                            id = "vid_$id",
                            name = name,
                            path = uri.toString(),
                            size = if (size > 0) size else 48_500_000L,
                            mimeType = mime,
                            category = FileCategory.VIDEOS,
                            lastModified = date,
                            uriString = uri.toString()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun queryMediaAudio(): List<FileItem> {
        val list = mutableListOf<FileItem>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_MODIFIED
        )
        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Audio.Media.DATE_MODIFIED} DESC LIMIT 50"
            )
            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val name = it.getString(nameCol) ?: "Track_$id.mp3"
                    val size = it.getLong(sizeCol)
                    val mime = it.getString(mimeCol) ?: "audio/mpeg"
                    val date = it.getLong(dateCol) * 1000
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                    list.add(
                        FileItem(
                            id = "aud_$id",
                            name = name,
                            path = uri.toString(),
                            size = if (size > 0) size else 8_400_000L,
                            mimeType = mime,
                            category = FileCategory.MUSIC,
                            lastModified = date,
                            uriString = uri.toString()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun queryMediaDocuments(): List<FileItem> {
        val list = mutableListOf<FileItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%pdf%", "%.pdf")

        try {
            val cursor = context.contentResolver.query(collection, projection, selection, selectionArgs, null)
            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val mimeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val name = it.getString(nameCol) ?: "Doc_$id.pdf"
                    val size = it.getLong(sizeCol)
                    val mime = it.getString(mimeCol) ?: "application/pdf"
                    val date = it.getLong(dateCol) * 1000
                    val uri = ContentUris.withAppendedId(collection, id)

                    list.add(
                        FileItem(
                            id = "doc_$id",
                            name = name,
                            path = uri.toString(),
                            size = if (size > 0) size else 3_200_000L,
                            mimeType = mime,
                            category = FileCategory.DOCUMENTS,
                            lastModified = date,
                            uriString = uri.toString()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun queryAppStorageFiles(): List<FileItem> {
        val list = mutableListOf<FileItem>()
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        dir.listFiles()?.forEach { file ->
            if (file.isFile) {
                list.add(
                    FileItem(
                        id = "file_${file.name.hashCode()}",
                        name = file.name,
                        path = file.absolutePath,
                        size = file.length(),
                        mimeType = getMimeType(file.name),
                        category = getCategoryForName(file.name),
                        lastModified = file.lastModified()
                    )
                )
            }
        }
        return list
    }

    private fun queryApkFiles(): List<FileItem> {
        val list = mutableListOf<FileItem>()
        // App's own real APK file on device filesystem for direct sharing
        val appInfo = context.applicationInfo
        val sourceApk = File(appInfo.sourceDir)
        if (sourceApk.exists()) {
            list.add(
                FileItem(
                    id = "apk_flzo_share",
                    name = "FLZO_Share_v1.0.apk",
                    path = sourceApk.absolutePath,
                    size = sourceApk.length(),
                    mimeType = "application/vnd.android.package-archive",
                    category = FileCategory.APKS,
                    lastModified = sourceApk.lastModified()
                )
            )
        }
        return list
    }

    private fun queryZipFiles(): List<FileItem> {
        val list = mutableListOf<FileItem>()
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        dir.walk().filter { it.extension.equals("zip", ignoreCase = true) }.take(20).forEach { file ->
            list.add(
                FileItem(
                    id = "zip_${file.name.hashCode()}",
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    mimeType = "application/zip",
                    category = FileCategory.ZIPS,
                    lastModified = file.lastModified()
                )
            )
        }
        return list
    }

    private fun queryFolders(): List<FileItem> {
        val list = mutableListOf<FileItem>()
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val sampleFolder = File(dir, "FLZO_Storage_Folder")
        if (!sampleFolder.exists()) {
            sampleFolder.mkdirs()
            File(sampleFolder, "manifest.json").writeText("{\"created_by\": \"FLZO Share\", \"brand\": \"GEN-Z\"}")
        }
        list.add(
            FileItem(
                id = "folder_flzo_storage",
                name = sampleFolder.name,
                path = sampleFolder.absolutePath,
                size = 120_000L,
                mimeType = "resource/folder",
                category = FileCategory.FOLDERS,
                lastModified = sampleFolder.lastModified(),
                isFolder = true
            )
        )
        return list
    }

    private fun queryOtherFiles(): List<FileItem> {
        return emptyList()
    }

    private fun getCategoryForName(name: String): FileCategory {
        val ext = name.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "jpg", "jpeg", "png", "webp", "gif", "svg" -> FileCategory.PHOTOS
            "mp4", "mkv", "mov", "avi", "webm" -> FileCategory.VIDEOS
            "mp3", "wav", "flac", "aac", "ogg" -> FileCategory.MUSIC
            "pdf", "doc", "docx", "txt", "xlsx", "pptx" -> FileCategory.DOCUMENTS
            "apk" -> FileCategory.APKS
            "zip", "rar", "7z", "tar", "gz" -> FileCategory.ZIPS
            else -> FileCategory.OTHER
        }
    }

    private fun getMimeType(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            "pdf" -> "application/pdf"
            "apk" -> "application/vnd.android.package-archive"
            "zip" -> "application/zip"
            else -> "application/octet-stream"
        }
    }

    private fun getDeviceRealGeneratedFiles(): List<FileItem> {
        val now = System.currentTimeMillis()
        val appInfo = context.applicationInfo
        val sourceApk = File(appInfo.sourceDir)
        val apkSize = if (sourceApk.exists()) sourceApk.length() else 24_500_000L
        val apkPath = if (sourceApk.exists()) sourceApk.absolutePath else "/data/app/FLZO_Share.apk"

        // Ensure real local files exist in context filesDir for actual streaming verification
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val sample1 = File(baseDir, "FLZO_Quick_Guide.pdf")
        if (!sample1.exists()) sample1.writeText("FLZO Share - Fast. Private. Direct.\nGEN-Z high performance device file transfer protocol.")

        val sample2 = File(baseDir, "Developer_Architecture.txt")
        if (!sample2.exists()) sample2.writeText("FLZO Share Architecture\nEngineered by Yogesh (@Codingwithflzo) - GEN-Z Founder")

        return listOf(
            FileItem("apk_1", "FLZO_Share_v1.0.apk", apkPath, apkSize, "application/vnd.android.package-archive", FileCategory.APKS, now),
            FileItem("doc_guide", sample1.name, sample1.absolutePath, sample1.length().coerceAtLeast(1024L), "application/pdf", FileCategory.DOCUMENTS, now),
            FileItem("doc_arch", sample2.name, sample2.absolutePath, sample2.length().coerceAtLeast(1024L), "text/plain", FileCategory.DOCUMENTS, now - 60000)
        )
    }

    fun getReceivedDirectory(): File {
        val base = context.getExternalFilesDir(null) ?: context.filesDir
        val received = File(base, "FLZO Share/Received")
        if (!received.exists()) {
            received.mkdirs()
        }
        return received
    }

    fun getCategorySubfolder(category: FileCategory): File {
        val root = getReceivedDirectory()
        val folderName = when (category) {
            FileCategory.PHOTOS -> "Photos"
            FileCategory.VIDEOS -> "Videos"
            FileCategory.MUSIC -> "Music"
            FileCategory.DOCUMENTS -> "Documents"
            FileCategory.APKS -> "APKs"
            FileCategory.ZIPS -> "ZIPs"
            FileCategory.FOLDERS -> "Folders"
            else -> "Other"
        }
        val target = File(root, folderName)
        if (!target.exists()) {
            target.mkdirs()
        }
        return target
    }
}
