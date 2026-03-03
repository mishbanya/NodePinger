package ru.mishbanya.nodepinger.domain.repositoryImpl

import io.libp2p.protocol.PingController
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mishbanya.nodepinger.model.repository.NodePinger
import java.util.concurrent.TimeUnit

//https://github.com/libp2p/jvm-libp2p
@Single(binds = [NodePinger::class])
class NodePingerImpl : NodePinger, KoinComponent {

    private val controller: PingController by inject()

    override fun pingNode(timeoutMillis: Long): Long {
        try {
            val latency = controller.ping().get(timeoutMillis, TimeUnit.MILLISECONDS)
            return latency
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}