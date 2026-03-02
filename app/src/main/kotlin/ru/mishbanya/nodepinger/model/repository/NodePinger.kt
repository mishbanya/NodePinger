package ru.mishbanya.nodepinger.model.repository

interface NodePinger {
    suspend fun pingNode(cid: String): Result<String>
}