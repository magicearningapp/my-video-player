package com.example.data.model

data class FolderItem(
    val name: String,
    val videoCount: Int,
    val latestVideoUri: String?,
    val totalSizeBytes: Long
)
