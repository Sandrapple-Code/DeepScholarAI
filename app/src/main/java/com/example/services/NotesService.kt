package com.example.services

import com.example.data.local.Note
import com.example.data.local.SavedReport

interface NotesService {
    fun convertReportToNote(report: SavedReport, workspace: String, folder: String): Note
}

class NotesServiceImpl : NotesService {
    override fun convertReportToNote(report: SavedReport, workspace: String, folder: String): Note {
        val contentBuilder = StringBuilder()
        contentBuilder.append("# ${report.topic}\n\n")
        contentBuilder.append("## Overview\n")
        contentBuilder.append("${report.overview}\n\n")
        contentBuilder.append("## Detailed Analysis\n")
        contentBuilder.append("${report.detailedAnalysis}\n\n")
        contentBuilder.append("## Key Insights\n")
        contentBuilder.append("${report.keyInsights}\n\n")
        contentBuilder.append("## Important Concepts\n")
        contentBuilder.append("${report.importantConcepts}\n\n")
        contentBuilder.append("## Further Reading\n")
        contentBuilder.append("${report.furtherReading}\n\n")
        contentBuilder.append("## Conclusion\n")
        contentBuilder.append("${report.conclusion}\n")

        return Note(
            workspace = workspace,
            folder = folder,
            title = "Research: ${report.topic}",
            content = contentBuilder.toString(),
            tags = "ai-report,${workspace.lowercase().replace(" ", "-")}",
            isFavorite = false,
            lastModified = System.currentTimeMillis()
        )
    }
}
