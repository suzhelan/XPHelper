package top.sacz.hook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import top.sacz.hook.activity.ModuleActivity

class ActivityTest {

    fun hook() {
        //activity的onCreate
        val activityOnCreate = Activity::class.java.getDeclaredMethod("onCreate", Bundle::class.java)
        activityOnCreate.isAccessible = true
        XposedBridge.hookMethod(activityOnCreate, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as Activity
                //延迟三秒后跳转
                Handler(activity.mainLooper).postDelayed({
                    val intent = Intent(activity, ModuleActivity::class.java)
                    activity.startActivity(intent)
                }, 3000)
            }
        })
    }
}