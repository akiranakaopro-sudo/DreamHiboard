package gd.app.hiboard

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import gd.app.hiboard.data.BoardRepository
import gd.app.hiboard.engine.CardEngineRegistry
import gd.app.hiboard.ui.BoardBackdrop

class HiboardApp : Application() {
    lateinit var boardRepository: BoardRepository
        private set
    lateinit var engines: CardEngineRegistry
        private set

    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate()
        instance = this
        boardRepository = BoardRepository(this)
        engines = CardEngineRegistry(this)
        BoardBackdrop.prefetch(this)
    }

    companion object {
        lateinit var instance: HiboardApp
            private set
    }
}
