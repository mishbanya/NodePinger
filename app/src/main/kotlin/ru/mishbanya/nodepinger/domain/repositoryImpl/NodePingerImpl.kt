package ru.mishbanya.nodepinger.domain.repositoryImpl

import io.ipfs.cid.Cid
import io.ipfs.multiaddr.MultiAddress
import io.libp2p.core.PeerId
import io.libp2p.core.Stream
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.koin.mp.KoinPlatform.getKoin
import org.peergos.BlockRequestAuthoriser
import org.peergos.EmbeddedIpfs
import org.peergos.HashedBlock
import org.peergos.HostBuilder
import org.peergos.Want
import org.peergos.blockstore.FileBlockstore
import org.peergos.config.IdentitySection
import org.peergos.protocol.dht.RamRecordStore
import org.peergos.protocol.http.HttpProtocol
import org.peergos.protocol.http.HttpProtocol.HttpRequestProcessor
import ru.mishbanya.nodepinger.model.repository.NodePinger
import ru.mishbanya.nodepinger.model.routing.NabuRouting
import ru.mishbanya.nodepinger.util.AppConfig
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import kotlin.io.path.Path


@Single(binds = [NodePinger::class])
class NodePingerImpl : NodePinger {

    override suspend fun pingNode(cid: String): Result<String> = withContext(Dispatchers.IO) {
        val directory = getKoin().get<AppConfig>().directory
        var ipfs: EmbeddedIpfs? = null
        try {
            val bootstrapAddresses = listOf(MultiAddress(NabuRouting.nodeAddress))
            val authoriser = BlockRequestAuthoriser { _, _, _ ->
                CompletableFuture.completedFuture(true)
            }

            val builder = HostBuilder().generateIdentity()
            val privKey = builder.privateKey
            val peerId = builder.peerId
            val identity = IdentitySection(privKey.bytes(), peerId)
            val httpTarget: SocketAddress = InetSocketAddress("localhost", 10000)
            val httpProxyTarget = Optional.of(HttpRequestProcessor { stream, msg, replyHandler -> HttpProtocol.proxyRequest(msg, httpTarget, replyHandler) })

            ipfs = EmbeddedIpfs.build(
                RamRecordStore(),
                FileBlockstore(Path(directory.toString())),
                true,
                bootstrapAddresses,
                bootstrapAddresses,
                identity,
                authoriser,
                httpProxyTarget
            )
            ipfs.start()

            val wants = listOf(Want(Cid.decode(cid)))
            val retrieveFrom = setOf(PeerId.fromBase58(cid))
            val blocks: List<HashedBlock> = ipfs.getBlocks(wants, retrieveFrom, false)

            if (blocks.isNotEmpty()) {
                val data = blocks[0].block
                Result.success("Получено ${data.size} байт")
            } else {
                Result.failure(Exception("Блок не найден"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                ipfs?.stop()?.join()
            } catch (_: Exception) {
            }
        }
    }
}