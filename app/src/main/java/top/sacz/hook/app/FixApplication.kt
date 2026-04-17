package top.sacz.hook.app

import android.app.Application
import top.sacz.xphelper.XpHelper

class FixApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        XpHelper.initContext(this)
    }
}