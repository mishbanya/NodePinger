package ru.mishbanya.nodepinger

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import ru.mishbanya.nodepinger.model.di.NabuModule

@ComponentScan
@Module(includes = [NabuModule::class])
class NodePingerModule()