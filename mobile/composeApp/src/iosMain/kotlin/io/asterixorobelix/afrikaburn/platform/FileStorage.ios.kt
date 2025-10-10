package io.asterixorobelix.afrikaburn.platform

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.darwin.dispatch_get_main_queue

class IOSFileStorage : FileStorage {
    
    private val fileManager = NSFileManager.defaultManager
    
    private val documentsDirectory: String by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        )
        paths.firstOrNull() as? String ?: ""
    }
    
    private val cachesDirectory: String by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory,
            NSUserDomainMask,
            true
        )
        paths.firstOrNull() as? String ?: ""
    }
    
    override fun getAppStorageDirectory(): String {
        return documentsDirectory
    }
    
    override fun getCacheDirectory(): String {
        return cachesDirectory
    }
    
    override fun getMapTilesDirectory(): String {
        return "$documentsDirectory/maps"
    }
    
    override fun getImagesDirectory(): String {
        return "$documentsDirectory/images"
    }
    
    override fun getContentPackagesDirectory(): String {
        return "$documentsDirectory/content"
    }
    
    override suspend fun saveFile(relativePath: String, data: ByteArray): Boolean = withContext(Dispatchers.Default) {
        try {
            val filePath = "$documentsDirectory/$relativePath"
            val fileURL = NSURL.fileURLWithPath(filePath)
            
            // Create parent directories
            val parentPath = (filePath as NSString).stringByDeletingLastPathComponent
            fileManager.createDirectoryAtPath(
                parentPath,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
            
            // Convert ByteArray to NSData
            val nsData = data.usePinned { pinned ->
                NSData.create(
                    bytes = pinned.addressOf(0),
                    length = data.size.toULong()
                )
            }
            
            // Write data to file
            nsData.writeToURL(fileURL, atomically = true)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override suspend fun readFile(relativePath: String): ByteArray? = withContext(Dispatchers.Default) {
        try {
            val filePath = "$documentsDirectory/$relativePath"
            val fileURL = NSURL.fileURLWithPath(filePath)
            
            if (!fileManager.fileExistsAtPath(filePath)) {
                return@withContext null
            }
            
            val nsData = NSData.dataWithContentsOfURL(fileURL) ?: return@withContext null
            
            // Convert NSData to ByteArray
            ByteArray(nsData.length.toInt()).apply {
                usePinned { pinned ->
                    memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    override suspend fun deleteFile(relativePath: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val filePath = "$documentsDirectory/$relativePath"
            if (fileManager.fileExistsAtPath(filePath)) {
                fileManager.removeItemAtPath(filePath, error = null)
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override fun fileExists(relativePath: String): Boolean {
        val filePath = "$documentsDirectory/$relativePath"
        return fileManager.fileExistsAtPath(filePath)
    }
    
    override fun getFileSize(relativePath: String): Long {
        val filePath = "$documentsDirectory/$relativePath"
        val attributes = fileManager.attributesOfItemAtPath(filePath, error = null)
        return (attributes?.get(NSFileSize) as? NSNumber)?.longValue ?: -1
    }
    
    override suspend fun getTotalStorageUsed(): Long = withContext(Dispatchers.Default) {
        calculateDirectorySize(documentsDirectory) + calculateDirectorySize(cachesDirectory)
    }
    
    override fun getAvailableStorageSpace(): Long {
        val fileSystemAttributes = fileManager.attributesOfFileSystemForPath(
            documentsDirectory,
            error = null
        )
        return (fileSystemAttributes?.get(NSFileSystemFreeSize) as? NSNumber)?.longValue ?: 0
    }
    
    override suspend fun clearCache(): Long = withContext(Dispatchers.Default) {
        val initialSize = calculateDirectorySize(cachesDirectory)
        
        fileManager.contentsOfDirectoryAtPath(cachesDirectory, error = null)?.forEach { item ->
            val itemPath = "$cachesDirectory/${item as String}"
            fileManager.removeItemAtPath(itemPath, error = null)
        }
        
        initialSize
    }
    
    override suspend fun clearOldFiles(maxAgeMillis: Long): Long = withContext(Dispatchers.Default) {
        val currentTime = NSDate().timeIntervalSince1970 * 1000
        var freedSpace = 0L
        
        val enumerator = fileManager.enumeratorAtPath(cachesDirectory)
        while (true) {
            val relativePath = enumerator?.nextObject() as? String ?: break
            val fullPath = "$cachesDirectory/$relativePath"
            
            val attributes = fileManager.attributesOfItemAtPath(fullPath, error = null)
            val modificationDate = attributes?.get(NSFileModificationDate) as? NSDate
            val fileSize = (attributes?.get(NSFileSize) as? NSNumber)?.longValue ?: 0
            
            modificationDate?.let { date ->
                val fileAge = currentTime - (date.timeIntervalSince1970 * 1000)
                if (fileAge > maxAgeMillis) {
                    fileManager.removeItemAtPath(fullPath, error = null)
                    freedSpace += fileSize
                }
            }
        }
        
        freedSpace
    }
    
    override fun createDirectory(relativePath: String): Boolean {
        val dirPath = "$documentsDirectory/$relativePath"
        return fileManager.createDirectoryAtPath(
            dirPath,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }
    
    override fun listFiles(relativePath: String): List<String>? {
        val dirPath = "$documentsDirectory/$relativePath"
        var isDirectory: ObjCBool = false
        
        if (fileManager.fileExistsAtPath(dirPath, isDirectory = isDirectory.ptr) && isDirectory.value) {
            return fileManager.contentsOfDirectoryAtPath(dirPath, error = null)
                ?.map { it.toString() }
                ?: emptyList()
        }
        return null
    }
    
    override suspend fun copyFile(sourcePath: String, destinationPath: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val sourceFullPath = "$documentsDirectory/$sourcePath"
            val destFullPath = "$documentsDirectory/$destinationPath"
            
            if (!fileManager.fileExistsAtPath(sourceFullPath)) {
                return@withContext false
            }
            
            // Create parent directories for destination
            val parentPath = (destFullPath as NSString).stringByDeletingLastPathComponent
            fileManager.createDirectoryAtPath(
                parentPath,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
            
            // Remove destination if it exists
            if (fileManager.fileExistsAtPath(destFullPath)) {
                fileManager.removeItemAtPath(destFullPath, error = null)
            }
            
            fileManager.copyItemAtPath(sourceFullPath, toPath = destFullPath, error = null)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override suspend fun moveFile(sourcePath: String, destinationPath: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val sourceFullPath = "$documentsDirectory/$sourcePath"
            val destFullPath = "$documentsDirectory/$destinationPath"
            
            if (!fileManager.fileExistsAtPath(sourceFullPath)) {
                return@withContext false
            }
            
            // Create parent directories for destination
            val parentPath = (destFullPath as NSString).stringByDeletingLastPathComponent
            fileManager.createDirectoryAtPath(
                parentPath,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
            
            fileManager.moveItemAtPath(sourceFullPath, toPath = destFullPath, error = null)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun calculateDirectorySize(directoryPath: String): Long {
        var totalSize = 0L
        
        val enumerator = fileManager.enumeratorAtPath(directoryPath)
        while (true) {
            val relativePath = enumerator?.nextObject() as? String ?: break
            val fullPath = "$directoryPath/$relativePath"
            
            val attributes = fileManager.attributesOfItemAtPath(fullPath, error = null)
            val fileSize = (attributes?.get(NSFileSize) as? NSNumber)?.longValue ?: 0
            totalSize += fileSize
        }
        
        return totalSize
    }
}

actual fun createFileStorage(): FileStorage {
    return IOSFileStorage()
}