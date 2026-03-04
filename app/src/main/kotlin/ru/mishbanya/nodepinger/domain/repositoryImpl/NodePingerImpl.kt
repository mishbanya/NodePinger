package ru.mishbanya.nodepinger.domain.repositoryImpl

import io.libp2p.core.PeerId
import io.libp2p.core.multiformats.Multiaddr
import io.libp2p.protocol.Ping
import io.libp2p.protocol.PingController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.peergos.EmbeddedIpfs
import ru.mishbanya.nodepinger.model.di.EmbeddedIpfsProvider
import ru.mishbanya.nodepinger.model.repository.NodePinger
import ru.mishbanya.nodepinger.model.routing.NabuRouting
import java.util.concurrent.TimeUnit

//https://github.com/libp2p/jvm-libp2p
@Single(binds = [NodePinger::class])
class NodePingerImpl(
    private val ipfsProvider: EmbeddedIpfsProvider
) : NodePinger {

    private var controller: PingController? = null
    private val mutex = Mutex()

    private suspend fun getController(timeoutMillis: Long): PingController {
        mutex.withLock {
            controller?.let { return it }
            val newController = createController(ipfsProvider.get(), timeoutMillis)
            controller = newController
            return newController
        }
    }

    private suspend fun resetAndGetController(timeoutMillis: Long): PingController {
        mutex.withLock {
            controller = null
            val newController = createController(ipfsProvider.reconnect(), timeoutMillis)
            controller = newController
            return newController
        }
    }

    private suspend fun createController(
        ipfs: EmbeddedIpfs,
        timeoutMillis: Long
    ): PingController = withContext(Dispatchers.IO) {
        val peerId = PeerId.fromBase58(NabuRouting.nodeAddress.substringAfterLast("/p2p/"))
        val address = Multiaddr(NabuRouting.nodeAddress)

        Ping().dial(ipfs.node, peerId, address)
            .controller
            .get(timeoutMillis, TimeUnit.MILLISECONDS)
    }

    override suspend fun pingNode(timeoutMillis: Long): Long = withContext(Dispatchers.IO) {
        try {
            getController(timeoutMillis)
                .ping()
                .get(timeoutMillis, TimeUnit.MILLISECONDS)
        } catch (_: Exception) {
            try {
                resetAndGetController(timeoutMillis)
                    .ping()
                    .get(timeoutMillis, TimeUnit.MILLISECONDS)
            } catch (retryException: Exception) {
                retryException.printStackTrace()
                throw retryException
            }
        }
    }
}