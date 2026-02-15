package top.sacz.hook

import android.util.Log
import de.robv.android.xposed.XposedBridge
import top.sacz.hook.ext.showToast
import top.sacz.xphelper.dexkit.MethodFinder
import top.sacz.xphelper.ext.toClass
import top.sacz.xphelper.reflect.ClassUtils
import java.lang.reflect.Method

class DexkitTest {
    fun hook() {
        startFindMethod()
    }

    private fun startFindMethod() {
        XposedBridge.log("[Xphelper]开始查找")
        val method = getHasInfo()
        method.toString().showToast()
        XposedBridge.log("[Xphelper]$method")
    }
    private fun getHasInfo() : Method? {
        val aioItemClass = "com.tencent.mobileqq.aio.msg.AIOMsgItem".toClass()
        /*val methodHasInfo = dexKit.findMethod {
            searchPackages("com.tencent.mobileqq.aio.utils")
            matcher {
                returnType(Boolean::class.java)
                paramTypes("com.tencent.mobileqq.aio.msg.AIOMsgItem")
                addInvoke { name = "getMsgRecord" }
                addUsingField { name = "anonymousExtInfo" }
            }
        }.firstOrNull()*/
        val getMsgRecordMethod = aioItemClass.getMethod("getMsgRecord")
        val anonymousExtInfoField = getMsgRecordMethod.returnType.getField("anonymousExtInfo")
        val methodHasInfo = MethodFinder.build()
            .searchPackages("com.tencent.mobileqq.aio.utils")
            .returnType(Boolean::class.java)
            .parameters(aioItemClass)
            .invokeMethods(getMsgRecordMethod)
            .usedFields(anonymousExtInfoField)
            .firstOrNull()
        return methodHasInfo
    }
}