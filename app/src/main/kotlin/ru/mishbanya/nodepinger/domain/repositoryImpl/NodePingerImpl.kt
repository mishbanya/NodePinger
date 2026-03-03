package ru.mishbanya.nodepinger.domain.repositoryImpl

import io.ipfs.cid.Cid
import io.ipfs.multiaddr.MultiAddress
import io.libp2p.core.PeerId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.peergos.EmbeddedIpfs
import org.peergos.HashedBlock
import org.peergos.HostBuilder
import org.peergos.Want
import org.peergos.blockstore.RamBlockstore
import org.peergos.config.IdentitySection
import org.peergos.protocol.dht.RamRecordStore
import ru.mishbanya.nodepinger.model.repository.NodePinger
import ru.mishbanya.nodepinger.model.routing.NabuRouting
import java.util.Optional
import java.util.concurrent.CompletableFuture

//https://github.com/Peergos/nabu/blob/master/src/test/java/org/peergos/EmbeddedIpfsTest.java
//https://github.com/Peergos/nabu/blob/master/src/main/java/org/peergos/EmbeddedIpfs.java
@Single(binds = [NodePinger::class])
class NodePingerImpl : NodePinger {

    override suspend fun pingNode(cid: String): Result<String> = withContext(Dispatchers.IO) {
        var ipfs: EmbeddedIpfs? = null
        try {
            val bootstrapAddresses = listOf(MultiAddress(NabuRouting.nodeAddress))

            val builder = HostBuilder().generateIdentity()

            ipfs = EmbeddedIpfs.build(
                RamRecordStore(),
                RamBlockstore(),
                true,
                listOf(MultiAddress("/ip4/0.0.0.0/tcp/0")), //Пустой список для локальных слушающих адресов
                bootstrapAddresses,
                IdentitySection(builder.privateKey.bytes(), builder.peerId),
                { _, _, _ ->
                    CompletableFuture.completedFuture(true)
                },
                Optional.empty() //без прокси
            )
            ipfs.start()

            val nodePeerId = PeerId.fromBase58(NabuRouting.nodeAddress.substringAfterLast("/p2p/"))

            val blocks: List<HashedBlock> = ipfs.getBlocks(
                listOf(Want(Cid.decode(cid))),
                setOf(nodePeerId),
                false
            )

            if (blocks.isNotEmpty()) {
                val data = blocks[0].block
                val content = String(data, Charsets.UTF_8)
                Result.success(content)
            } else {
                Result.failure(Exception("Блок не найден"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        } finally {
            try {
                ipfs?.stop()?.join()
            } catch (_: Exception) {
            }
        }
    }
}