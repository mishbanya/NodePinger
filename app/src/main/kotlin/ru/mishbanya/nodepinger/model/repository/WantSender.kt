package ru.mishbanya.nodepinger.model.repository

import org.peergos.EmbeddedIpfs

interface WantSender {
    suspend fun sendWants(cid: String): Result<String>
}