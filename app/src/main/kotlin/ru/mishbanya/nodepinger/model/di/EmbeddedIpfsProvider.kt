package ru.mishbanya.nodepinger.model.di

import io.ipfs.multiaddr.MultiAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.peergos.EmbeddedIpfs
import org.peergos.HostBuilder
import org.peergos.blockstore.RamBlockstore
import org.peergos.config.IdentitySection
import org.peergos.protocol.dht.RamRecordStore
import ru.mishbanya.nodepinger.model.routing.NabuRouting
import java.util.Optional
import java.util.concurrent.CompletableFuture

@Single
class EmbeddedIpfsProvider {

    private var ipfs: EmbeddedIpfs? = null

    private val mutex = Mutex()

    suspend fun get(): EmbeddedIpfs {
        mutex.withLock {
            ipfs?.let { return it }
            val newIpfs = buildAndStart()
            ipfs = newIpfs
            return newIpfs
        }
    }

    suspend fun reconnect(): EmbeddedIpfs {
        mutex.withLock {
            stopQuietly(ipfs)
            val newIpfs = buildAndStart()
            ipfs = newIpfs
            return newIpfs
        }
    }

    suspend fun stop() {
        mutex.withLock {
            stopQuietly(ipfs)
            ipfs = null
        }
    }

    private suspend fun buildAndStart(): EmbeddedIpfs = withContext(Dispatchers.IO) {
        val bootstrapAddresses = listOf(MultiAddress(NabuRouting.nodeAddress))
        val builder = HostBuilder().generateIdentity()

        val newIpfs = EmbeddedIpfs.build(
            RamRecordStore(),
            RamBlockstore(),
            true,
            listOf(MultiAddress("/ip4/127.0.0.1/tcp/0")),
            bootstrapAddresses,
            IdentitySection(builder.privateKey.bytes(), builder.peerId),
            { _, _, _ ->
                CompletableFuture.completedFuture(true)
            },
            Optional.empty()
        )

        newIpfs.start()
        newIpfs
    }

    private suspend fun stopQuietly(instance: EmbeddedIpfs?) = withContext(Dispatchers.IO) {
        try {
            instance?.stop()?.join()
        } catch (_: Exception) {
        }
    }
}

