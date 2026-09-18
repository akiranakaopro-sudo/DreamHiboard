package gd.app.hiboard

import android.app.Application
import gd.app.hiboard.data.BoardRepository
import gd.app.hiboard.engine.CardEngineRegistry

class HiboardApp : Application() {
    lateinit var boardRepository: BoardRepository
        private set
    lateinit var engines: CardEngineRegistry
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        boardRepository = BoardRepository(this)
        engines = CardEngineRegistry(this)
    }

    companion object {
        lateinit var instance: HiboardApp
            private set
    }
}
