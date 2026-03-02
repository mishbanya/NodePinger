package ru.mishbanya.nodepinger.domain.repositoryImpl

import org.koin.core.annotation.Single
import ru.mishbanya.nodepinger.model.repository.NodePinger

@Single(binds = [NodePinger::class])
class NodePingerImpl: NodePinger {

    override suspend fun pingNode(node: String): Result<String> {
        TODO("Not yet implemented")
    }
}