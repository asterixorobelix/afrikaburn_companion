package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.domain.model.MOOPReport
import io.asterixorobelix.afrikaburn.domain.repository.MOOPRepository
import io.asterixorobelix.afrikaburn.domain.usecase.SyncResult

/**
 * Stub implementation of MOOPRepository for development
 */
class MOOPRepositoryStub : MOOPRepository {
    private val reports = mutableMapOf<String, MOOPReport>()
    
    override suspend fun saveMOOPReport(report: MOOPReport) {
        reports[report.id] = report
    }
    
    override suspend fun syncMOOPReport(reportId: String): SyncResult {
        return SyncResult(
            success = true,
            error = null
        )
    }
    
    override suspend fun getMOOPReport(reportId: String): MOOPReport? {
        return reports[reportId]
    }
    
    override suspend fun getAllMOOPReports(): List<MOOPReport> {
        return reports.values.toList()
    }
    
    override suspend fun getPendingSyncReports(): List<MOOPReport> {
        // For stub, return all reports as pending
        return reports.values.toList()
    }
    
    override suspend fun updateMOOPReport(report: MOOPReport) {
        reports[report.id] = report
    }
    
    override suspend fun deleteMOOPReport(reportId: String) {
        reports.remove(reportId)
    }
}