package ru.mishbanya.nodepinger.domain.repositoryImpl

import io.libp2p.core.multiformats.Multiaddr
import org.koin.core.annotation.Single
import org.peergos.EmbeddedIpfs
import ru.mishbanya.nodepinger.model.repository.NodePinger
import ru.mishbanya.nodepinger.model.routing.NabuRouting

@Single(binds = [NodePinger::class])
class NodePingerImpl(
    private val ipfs: EmbeddedIpfs
) : NodePinger {

    override fun pingNode(): Long {
        try {
            ipfs.start()

            val nodeMultiaddr = Multiaddr(NabuRouting.nodeAddress)

            val startTime = System.currentTimeMillis()
            ipfs.node.network.connect(nodeMultiaddr).get()
            val endTime = System.currentTimeMillis()

            return endTime - startTime
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            try {
                ipfs.stop()?.join()
            } catch (_: Exception) {
            }
        }
    }
}