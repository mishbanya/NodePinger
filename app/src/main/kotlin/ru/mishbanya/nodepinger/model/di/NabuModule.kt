package ru.mishbanya.nodepinger.model.di

import io.ipfs.multiaddr.MultiAddress
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

        return ipfs
    }
}