package ru.mishbanya.nodepinger

import android.app.Application
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import org.koin.ksp.generated.module
import ru.mishbanya.nodepinger.util.AppConfig

class NodePingerApp: Application() {
    override fun onCreate() {
        super.onCreate()

        val appDirName = runBlocking {
            getString(R.string.app_directory)
        }

        val appModule = module {
            single {
                AppConfig(
                    directory = Path(
                        System.getenv("LOCALAPPDATA") ?: System.getProperty("user.home"), appDirName
                    ).apply {
                        if (!SystemFileSystem.exists(this)) {
                            SystemFileSystem.createDirectories(this, true)
                        }
                    }
                )
            }
        }

        GlobalContext.startKoin {
            printLogger()
            modules(appModule)
            modules(NodePingerModule().module)
        }
    }
}