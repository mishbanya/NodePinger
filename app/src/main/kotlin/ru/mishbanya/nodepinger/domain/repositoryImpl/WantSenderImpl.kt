package ru.mishbanya.nodepinger.domain.repositoryImpl

import io.ipfs.cid.Cid
import io.libp2p.core.PeerId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.peergos.HashedBlock
import org.peergos.Want
import ru.mishbanya.nodepinger.model.di.EmbeddedIpfsProvider
import ru.mishbanya.nodepinger.model.repository.WantSender
import ru.mishbanya.nodepinger.model.routing.NabuRouting

//https://github.com/Peergos/nabu/blob/master/src/test/java/org/peergos/EmbeddedIpfsTest.java
@Single(binds = [WantSender::class])
class WantSenderImpl : WantSender, KoinComponent {

    private val ipfsProvider: EmbeddedIpfsProvider by inject()

    private suspend fun send(cid: String): Result<String> {
        val nodePeerId = PeerId.fromBase58(NabuRouting.nodeAddress.substringAfterLast("/p2p/"))

        val blocks: List<HashedBlock> = ipfsProvider.get().getBlocks(
            listOf(Want(Cid.decode(cid))),
            setOf(nodePeerId),
            false
        )

        return if (blocks.isNotEmpty()) {
            val data = blocks[0].block.decodeToString().filter { it != '\uFFFD' }
            Result.success(data)
        } else {
            Result.failure(Exception("Блок не найден"))
        }
    }

    override suspend fun sendWants(cid: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            send(cid)
        } catch (_: Exception) {
            try {
                ipfsProvider.reconnect()
                send(cid)
            } catch (retryException: Exception) {
                retryException.printStackTrace()
                Result.failure(retryException)
            }
        }
    }
}