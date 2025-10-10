package io.asterixorobelix.afrikaburn.platform

import android.content.Context
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AndroidFileStorage(
    private val context: Context
) : FileStorage {
    
    private val appStorageDir = context.filesDir
    private val cacheDir = context.cacheDir
    
    override fun getAppStorageDirectory(): String {
        return appStorageDir.absolutePath
    }
    
    override fun getCacheDirectory(): String {
        return cacheDir.absolutePath
    }
    
    override fun getMapTilesDirectory(): String {
        return File(appStorageDir, "maps").absolutePath
    }
    
    override fun getImagesDirectory(): String {
        return File(appStorageDir, "images").absolutePath
    }
    
    override fun getContentPackagesDirectory(): String {
        return File(appStorageDir, "content").absolutePath
    }
    
    override suspend fun saveFile(relativePath: String, data: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(appStorageDir, relativePath)
            file.parentFile?.mkdirs()
            
            FileOutputStream(file).use { output ->
                output.write(data)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override suspend fun readFile(relativePath: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val file = File(appStorageDir, relativePath)
            if (!file.exists()) return@withContext null
            
            FileInputStream(file).use { input ->
                input.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    override suspend fun deleteFile(relativePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(appStorageDir, relativePath)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override fun fileExists(relativePath: String): Boolean {
        return File(appStorageDir, relativePath).exists()
    }
    
    override fun getFileSize(relativePath: String): Long {
        val file = File(appStorageDir, relativePath)
        return if (file.exists()) file.length() else -1
    }
    
    override suspend fun getTotalStorageUsed(): Long = withContext(Dispatchers.IO) {
        calculateDirectorySize(appStorageDir) + calculateDirectorySize(cacheDir)
    }
    
    override fun getAvailableStorageSpace(): Long {
        val stat = StatFs(appStorageDir.path)
        return stat.availableBlocksLong * stat.blockSizeLong
    }
    
    override suspend fun clearCache(): Long = withContext(Dispatchers.IO) {
        val initialSize = calculateDirectorySize(cacheDir)
        deleteRecursive(cacheDir)
        cacheDir.mkdirs()
        initialSize
    }
    
    override suspend fun clearOldFiles(maxAgeMillis: Long): Long = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        var freedSpace = 0L
        
        val filesToDelete = mutableListOf<File>()
        findOldFiles(cacheDir, currentTime - maxAgeMillis, filesToDelete)
        
        filesToDelete.forEach { file ->
            freedSpace += file.length()
            file.delete()
        }
        
        freedSpace
    }
    
    override fun createDirectory(relativePath: String): Boolean {
        return File(appStorageDir, relativePath).mkdirs()
    }
    
    override fun listFiles(relativePath: String): List<String>? {
        val dir = File(appStorageDir, relativePath)
        return if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.map { it.name } ?: emptyList()
        } else {
            null
        }
    }
    
    override suspend fun copyFile(sourcePath: String, destinationPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(appStorageDir, sourcePath)
            val destFile = File(appStorageDir, destinationPath)
            
            if (!sourceFile.exists()) return@withContext false
            
            destFile.parentFile?.mkdirs()
            sourceFile.copyTo(destFile, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override suspend fun moveFile(sourcePath: String, destinationPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(appStorageDir, sourcePath)
            val destFile = File(appStorageDir, destinationPath)
            
            if (!sourceFile.exists()) return@withContext false
            
            destFile.parentFile?.mkdirs()
            sourceFile.renameTo(destFile)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        directory.walkTopDown().forEach { file ->
            if (file.isFile) {
                size += file.length()
            }
        }
        return size
    }
    
    private fun deleteRecursive(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                deleteRecursive(child)
            }
        }
        file.delete()
    }
    
    private fun findOldFiles(directory: File, maxAge: Long, result: MutableList<File>) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                findOldFiles(file, maxAge, result)
            } else if (file.lastModified() < maxAge) {
                result.add(file)
            }
        }
    }
}

actual fun createFileStorage(): FileStorage {
    // This will need to be injected with proper context via Koin
    throw IllegalStateException("FileStorage must be provided via dependency injection with Android Context")
}