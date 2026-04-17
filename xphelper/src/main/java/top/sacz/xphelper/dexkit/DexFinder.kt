package top.sacz.xphelper.dexkit


import org.luckypray.dexkit.DexKitBridge
import top.sacz.xphelper.XpHelper
import top.sacz.xphelper.dexkit.bean.ClassInfo
import top.sacz.xphelper.dexkit.bean.FieldInfo
import top.sacz.xphelper.dexkit.bean.MethodInfo
import top.sacz.xphelper.dexkit.cache.DexKitCache
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean

object DexFinder {
    val isLoadLibrary = AtomicBoolean()
    private var dexKitBridge: DexKitBridge? = null
    private var timer: Timer? = null

    private var autoCloseTime = (10 * 1000).toLong()

    /**
     * 设置关闭的时间 超过此时间没有使用dexkit 则自动关闭 (其实有可能查找过程中被关闭)
     * 所以确保每次调用getDexKitBridge后十秒内完成查找
     * 默认十秒 设置为0则不会自动关闭
     *
     * @param time 单位毫秒
     */
    fun setAutoCloseTime(time: Long) {
        autoCloseTime = time
    }

    /**
     * 初始化dexkit
     *
     * @param apkPath
     */
    @Synchronized
    fun create(apkPath: String) {
        if (dexKitBridge != null) {
            return
        }
        if (!isLoadLibrary.getAndSet(true)) {
            try {
                System.loadLibrary("dexkit")
            } catch (e: Exception) {
            }
        }
        dexKitBridge = DexKitBridge.create(apkPath)
    }

    /**
     * 得到dexkit实例
     */
    @JvmStatic
    fun getDexKitBridge(): DexKitBridge {
        if (dexKitBridge == null) {
            create(XpHelper.context.applicationInfo.sourceDir)
        }
        resetTimer()
        return dexKitBridge!!
    }

    @JvmSynthetic
    fun findMethod(methodInfo: MethodInfo.() -> Unit): MethodFinder {
        val newInfo = MethodInfo().also(methodInfo)
        return newInfo.generate()
    }

    @JvmSynthetic
    fun findField(fieldInfo: FieldInfo.() -> Unit): FieldFinder {
        val newInfo = FieldInfo().also(fieldInfo)
        return newInfo.generate()
    }

    @JvmSynthetic
    fun findClass(classInfo: ClassInfo.() -> Unit): ClassFinder {
        val newInfo = ClassInfo().also(classInfo)
        return newInfo.generate()
    }

    //region 带缓存的DexKit查询

    /**
     * 带缓存执行DexKit方法查询
     *
     * 使用场景: 当你需要直接使用 DexKitBridge 进行复杂查询，同时又希望走缓存时
     *
     * 用法:
     * ```
     * val method = DexFinder.queryMethod("ChatActivity_onCreate") { bridge ->
     *     val findMethod = FindMethod.create().matcher(
     *         MethodMatcher.create().declaredClass("com.example.ChatActivity").name("onCreate")
     *     )
     *     bridge.findMethod(findMethod).map { it.getMethodInstance(classLoader) }
     * }
     * ```
     *
     * @param key 缓存键(建议使用有业务含义的字符串，如 "ChatActivity_onCreate")
     * @param block 查询逻辑，仅在缓存未命中时执行
     * @return 查找到的方法列表
     */
    @JvmStatic
    fun queryMethod(key: String, block: (DexKitBridge) -> List<Method>): List<Method> {
        val cached = DexKitCache.getMethodList(key)
        if (cached != null) return cached
        val result = block(getDexKitBridge())
        DexKitCache.putMethodList(key, result)
        return result
    }

    /**
     * 带缓存执行DexKit字段查询
     *
     * @param key 缓存键(建议使用有业务含义的字符串)
     * @param block 查询逻辑，仅在缓存未命中时执行
     * @return 查找到的字段列表
     */
    @JvmStatic
    fun queryField(key: String, block: (DexKitBridge) -> List<Field>): List<Field> {
        val cached = DexKitCache.getFieldList(key)
        if (cached != null) return cached
        val result = block(getDexKitBridge())
        DexKitCache.putFieldList(key, result)
        return result
    }

    /**
     * 带缓存执行DexKit类查询
     *
     * @param key 缓存键(建议使用有业务含义的字符串)
     * @param block 查询逻辑，仅在缓存未命中时执行
     * @return 查找到的类列表
     */
    @JvmStatic
    fun queryClass(key: String, block: (DexKitBridge) -> List<Class<*>>): List<Class<*>> {
        val cached = DexKitCache.getClassList(key)
        if (cached != null) return cached
        val result = block(getDexKitBridge())
        DexKitCache.putClassList(key, result)
        return result
    }

    /**
     * 带缓存执行DexKit构造方法查询
     *
     * @param key 缓存键(建议使用有业务含义的字符串)
     * @param block 查询逻辑，仅在缓存未命中时执行
     * @return 查找到的构造方法列表
     */
    @JvmStatic
    fun queryConstructor(key: String, block: (DexKitBridge) -> List<Constructor<*>>): List<Constructor<*>> {
        val cached = DexKitCache.getConstructorList(key)
        if (cached != null) return cached
        val result = block(getDexKitBridge())
        DexKitCache.putConstructorList(key, result)
        return result
    }

    //endregion

    /**
     * 清空缓存
     */
    fun clearCache() {
        DexKitCache.clearCache()
    }

    private fun resetTimer() {
        if (autoCloseTime <= 0) {
            return
        }
        //如果存在则取消 达到重置时间的效果
        if (timer != null) {
            timer!!.cancel()
        }
        //定时 10秒钟后关闭
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                close()
            }
        }, autoCloseTime) // 10 seconds
    }

    /**
     * 释放dexkit资源
     */
    fun close() {
        if (dexKitBridge != null) {
            dexKitBridge!!.close()
            dexKitBridge = null
        }
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }
}
