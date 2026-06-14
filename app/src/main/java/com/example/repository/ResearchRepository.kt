package com.example.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ResearchProject
import com.example.data.local.ResearchPaper
import com.example.data.local.SavedReport
import kotlinx.coroutines.flow.Flow

class ResearchRepository(private val db: AppDatabase) {

    val allProjects: Flow<List<ResearchProject>> = db.researchProjectDao().getAllProjects()
    val allReports: Flow<List<SavedReport>> = db.savedReportDao().getAllReports()
    val allPapers: Flow<List<ResearchPaper>> = db.researchPaperDao().getAllPapers()

    fun getReportsForProject(projectId: Int): Flow<List<SavedReport>> =
        db.savedReportDao().getReportsByProjectId(projectId)

    fun getPaperByCategory(category: String): Flow<List<ResearchPaper>> =
        db.researchPaperDao().getPapersByCategory(category)

    fun getReportById(id: Int): Flow<SavedReport?> =
        db.savedReportDao().getReportById(id)

    suspend fun insertProject(project: ResearchProject): Long {
        return db.researchProjectDao().insertProject(project)
    }

    suspend fun deleteProject(project: ResearchProject) {
        db.researchProjectDao().deleteProject(project)
    }

    suspend fun insertReport(report: SavedReport): Long {
        return db.savedReportDao().insertReport(report)
    }

    suspend fun updateReport(report: SavedReport) {
        db.savedReportDao().updateReport(report)
    }

    suspend fun deleteReport(report: SavedReport) {
        db.savedReportDao().deleteReport(report)
    }

    suspend fun insertPaper(paper: ResearchPaper): Long {
        return db.researchPaperDao().insertPaper(paper)
    }

    suspend fun updatePaper(paper: ResearchPaper) {
        db.researchPaperDao().updatePaper(paper)
    }

    suspend fun deletePaper(paper: ResearchPaper) {
        db.researchPaperDao().deletePaper(paper)
    }
}
