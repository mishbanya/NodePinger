package ru.mishbanya.nodepinger.model.di

import io.ipfs.multiaddr.MultiAddress
import io.libp2p.core.PeerId
import io.libp2p.core.multiformats.Multiaddr
import io.libp2p.protocol.Ping
import io.libp2p.protocol.PingController
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.peergos.EmbeddedIpfs
import org.peergos.HostBuilder
import org.peergos.blockstore.RamBlockstore
import org.peergos.config.IdentitySection
import org.peergos.protocol.dht.RamRecordStore
import ru.mishbanya.nodepinger.model.routing.NabuRouting
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.getValue

@Module()
class NabuModule {
    @Single
    fun provideEmbeddedIpfs(): EmbeddedIpfs {
        val bootstrapAddresses = listOf(MultiAddress(NabuRouting.nodeAddress))
        val builder = HostBuilder().generateIdentity()

        val ipfs = EmbeddedIpfs.build(
            RamRecordStore(),
            RamBlockstore(),
            true,
            listOf(MultiAddress("/ip4/0.0.0.0/tcp/0")), //Пустой список для локальных слушающих адресов
            bootstrapAddresses,
            IdentitySection(builder.privateKey.bytes(), builder.peerId),
            { _, _, _ ->
                CompletableFuture.completedFuture(true)
            },
            Optional.empty()
        )

        ipfs.start()

        return ipfs
    }

    @Single
    fun providePingController(
        ipfs: EmbeddedIpfs
    ): PingController {

        val bootstrapAddresses by lazy { Multiaddr(NabuRouting.nodeAddress) }
        val peerId by lazy { PeerId.fromBase58(NabuRouting.nodeAddress.substringAfterLast("/p2p/")) }

        return Ping().dial(
            ipfs.node,
            peerId,
            bootstrapAddresses
        ).controller.get(3000L, TimeUnit.MILLISECONDS)
    }
}