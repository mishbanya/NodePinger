package ru.mishbanya.nodepinger.model.repository

interface NodePinger {
    suspend fun pingNode(
        timeoutMillis: Long
    ): Long
}