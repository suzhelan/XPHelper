package top.sacz.hook.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sacz.hook.entity.ConfigTestCategoryResult
import top.sacz.hook.entity.ConfigTestCategoryType
import top.sacz.hook.entity.ConfigTestTaskResult
import top.sacz.hook.entity.ScenarioPayload
import top.sacz.xphelper.util.ConfigUtils
import java.io.File

data class TestUiState(
    val isRunning: Boolean = false,
    val runVersion: Int = 0,
    val categories: List<ConfigTestCategoryResult> = emptyList(),
    val startTime: Long = 0L,
    val endTime: Long = 0L,
) {
    val totalTasks: Int
        get() = categories.sumOf { it.tasks.size }

    val passedTasks: Int
        get() = categories.sumOf { category -> category.tasks.count { it.passed } }

    val failedTasks: Int
        get() = categories.sumOf { category -> category.tasks.count { !it.passed } }

    val elapsedTotal: Long
        get() = if (endTime > startTime) endTime - startTime else 0L
}

class TestViewModel : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    var uiState by mutableStateOf(TestUiState())
        private set

    fun startTest(context: Context) {
        if (uiState.isRunning) return

        uiState = uiState.copy(
            isRunning = true,
            runVersion = uiState.runVersion + 1,
            categories = emptyList(),
            startTime = System.currentTimeMillis(),
            endTime = 0L
        )

        scope.launch {
            runConfigTest(context.applicationContext) { latestCategories ->
                uiState = uiState.copy(categories = latestCategories)
            }
            uiState = uiState.copy(
                isRunning = false,
                endTime = System.currentTimeMillis()
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    private suspend fun runConfigTest(
        context: Context,
        onEmit: (List<ConfigTestCategoryResult>) -> Unit,
    ) {
        val categories = mutableListOf<ConfigTestCategoryResult>()

        suspend fun startCategory(type: ConfigTestCategoryType) {
            categories.add(ConfigTestCategoryResult(type))
            withContext(Dispatchers.Main) { onEmit(categories.toList()) }
            delay(80)
        }

        suspend fun emit(task: ConfigTestTaskResult) {
            val last = categories.last()
            categories[categories.lastIndex] = last.copy(tasks = last.tasks + task)
            withContext(Dispatchers.Main) { onEmit(categories.toList()) }
            delay(200)
        }

        withContext(Dispatchers.IO) {
            // ==================== 1. 构造安全验证 ====================
            startCategory(ConfigTestCategoryType.SAFETY)
            emit(task(
                name = "未初始化时直接 new ConfigUtils() 应抛异常",
                writeLabel = "ConfigUtils()"
            ) {
                resetConfigUtilsGlobals()
                val exception = runCatching { ConfigUtils() }.exceptionOrNull()
                check(exception is RuntimeException)
                check(exception.message?.contains("storePath is empty") == true)
                "抛出 RuntimeException(\"storePath is empty\")"
            })

            // ==================== 2. 基础类型读写 ====================
            startCategory(ConfigTestCategoryType.BASIC_TYPES)
            resetConfigUtilsGlobals()
            ConfigUtils.initialize(context)
            ConfigUtils.setGlobalPassword("")
            val basicConfig = ConfigUtils(key = "basic_${System.nanoTime()}")

            emit(task(
                name = "put(String) → getString",
                writeLabel = "写入: \"hello\""
            ) {
                basicConfig.put("string", "hello")
                val readValue = basicConfig.getString("string")
                check(readValue == "hello")
                readValue
            })
            emit(task(
                name = "put(Int) → getInt",
                writeLabel = "写入: 7"
            ) {
                basicConfig.put("int", 7)
                val readValue = basicConfig.getInt("int")
                check(readValue == 7)
                readValue.toString()
            })
            emit(task(
                name = "put(Boolean) → getBoolean",
                writeLabel = "写入: true"
            ) {
                basicConfig.put("boolean", true)
                val readValue = basicConfig.getBoolean("boolean")
                check(readValue)
                readValue.toString()
            })
            emit(task(
                name = "put(Float) → getFloat",
                writeLabel = "写入: 1.5f"
            ) {
                basicConfig.put("float", 1.5f)
                val readValue = basicConfig.getFloat("float")
                check(readValue == 1.5f)
                readValue.toString()
            })
            emit(task(
                name = "put(Long) → getLong",
                writeLabel = "写入: 9L"
            ) {
                basicConfig.put("long", 9L)
                val readValue = basicConfig.getLong("long")
                check(readValue == 9L)
                readValue.toString()
            })
            emit(task(
                name = "put(Double) → getDouble",
                writeLabel = "写入: 3.14"
            ) {
                basicConfig.put("double", 3.14)
                val readValue = basicConfig.getDouble("double")
                check(readValue == 3.14)
                readValue.toString()
            })
            emit(task(
                name = "put(ByteArray) → getBytes",
                writeLabel = "写入: byteArrayOf(1, 2, 3)"
            ) {
                basicConfig.put("bytes", byteArrayOf(1, 2, 3))
                val readValue = basicConfig.getBytes("bytes")
                check(readValue.contentEquals(byteArrayOf(1, 2, 3)))
                readValue.contentToString()
            })
            emit(task(
                name = "put(List) 传入 List 不会落库 (put 不处理 List 类型)",
                writeLabel = "写入: listOf(\"unused\")"
            ) {
                basicConfig.put("ignoredList", listOf("unused"))
                val result = basicConfig.containsKey("ignoredList")
                check(!result)
                "containsKey = false (未写入存储)"
            })

            // ==================== 3. 默认值与边界 ====================
            startCategory(ConfigTestCategoryType.DEFAULTS)
            resetConfigUtilsGlobals()
            ConfigUtils.initialize(context)
            ConfigUtils.setGlobalPassword("")
            val defaultConfig = ConfigUtils(key = "defaults_${System.nanoTime()}")

            emit(task(
                name = "getString(key不存在) → 返回默认值",
                writeLabel = "默认值: \"fallback\""
            ) {
                val readValue = defaultConfig.getString("miss_str", "fallback")
                check(readValue == "fallback")
                readValue
            })
            emit(task(
                name = "getInt(key不存在) → 返回默认值",
                writeLabel = "默认值: 11"
            ) {
                val readValue = defaultConfig.getInt("miss_int", 11)
                check(readValue == 11)
                readValue.toString()
            })
            emit(task(
                name = "getBoolean(key不存在) → 返回默认值 false",
                writeLabel = "默认值: false"
            ) {
                val readValue = defaultConfig.getBoolean("miss_bool")
                check(!readValue)
                readValue.toString()
            })
            emit(task(
                name = "getFloat(key不存在) → 返回默认值",
                writeLabel = "默认值: 2.5f"
            ) {
                val readValue = defaultConfig.getFloat("miss_float", 2.5f)
                check(readValue == 2.5f)
                readValue.toString()
            })
            emit(task(
                name = "getLong(key不存在) → 返回默认值",
                writeLabel = "默认值: 22L"
            ) {
                val readValue = defaultConfig.getLong("miss_long", 22L)
                check(readValue == 22L)
                readValue.toString()
            })
            emit(task(
                name = "getDouble(key不存在) → 返回默认值",
                writeLabel = "默认值: 6.28"
            ) {
                val readValue = defaultConfig.getDouble("miss_double", 6.28)
                check(readValue == 6.28)
                readValue.toString()
            })
            emit(task(
                name = "getBytes(key不存在) → 返回默认值",
                writeLabel = "默认值: byteArrayOf(9)"
            ) {
                val readValue = defaultConfig.getBytes("miss_bytes", byteArrayOf(9))
                check(readValue.contentEquals(byteArrayOf(9)))
                readValue.contentToString()
            })
            emit(task(
                name = "getStringList(key不存在) → 返回空列表",
                writeLabel = "默认值: 空列表"
            ) {
                val readValue = defaultConfig.getStringList("miss_sl")
                check(readValue.isEmpty())
                "[] (空列表)"
            })

            // ==================== 4. 集合与批量操作 ====================
            startCategory(ConfigTestCategoryType.COLLECTIONS)
            resetConfigUtilsGlobals()
            ConfigUtils.initialize(context)
            ConfigUtils.setGlobalPassword("")
            val collectionConfig = ConfigUtils(key = "collection_${System.nanoTime()}")

            // putStringList 专用方法 (仅限 String)
            emit(task(
                name = "putStringList / getStringList",
                writeLabel = "写入: [\"a\", \"b\"]"
            ) {
                collectionConfig.putStringList("string_list", listOf("a", "b"))
                val readValue = collectionConfig.getStringList("string_list")
                check(readValue == mutableListOf("a", "b"))
                readValue.toString()
            })

            // putList 泛型方法 — 基础类型
            emit(task(
                name = "putList<String> / getList<String> (reified)",
                writeLabel = "写入: [\"x\", \"y\", \"z\"]"
            ) {
                collectionConfig.putList("list_string", mutableListOf("x", "y", "z"))
                val readValue = collectionConfig.getList<String>("list_string")
                check(readValue == mutableListOf("x", "y", "z"))
                readValue.toString()
            })
            emit(task(
                name = "putList<Int> / getList<Int> (reified)",
                writeLabel = "写入: [1, 2, 3]"
            ) {
                collectionConfig.putList("list_int", mutableListOf(1, 2, 3))
                val readValue = collectionConfig.getList<Int>("list_int")
                check(readValue == mutableListOf(1, 2, 3))
                readValue.toString()
            })
            emit(task(
                name = "putList<Long> / getList<Long> (reified)",
                writeLabel = "写入: [100L, 200L, 300L]"
            ) {
                collectionConfig.putList("list_long", mutableListOf(100L, 200L, 300L))
                val readValue = collectionConfig.getList<Long>("list_long")
                check(readValue == mutableListOf(100L, 200L, 300L))
                readValue.toString()
            })
            emit(task(
                name = "putList<Float> / getList<Float> (reified)",
                writeLabel = "写入: [1.5f, 2.5f]"
            ) {
                collectionConfig.putList("list_float", mutableListOf(1.5f, 2.5f))
                val readValue = collectionConfig.getList<Float>("list_float")
                check(readValue == mutableListOf(1.5f, 2.5f))
                readValue.toString()
            })
            emit(task(
                name = "putList<Double> / getList<Double> (reified)",
                writeLabel = "写入: [3.14, 6.28]"
            ) {
                collectionConfig.putList("list_double", mutableListOf(3.14, 6.28))
                val readValue = collectionConfig.getList<Double>("list_double")
                check(readValue == mutableListOf(3.14, 6.28))
                readValue.toString()
            })
            emit(task(
                name = "putList<Boolean> / getList<Boolean> (reified)",
                writeLabel = "写入: [true, false, true]"
            ) {
                collectionConfig.putList("list_boolean", mutableListOf(true, false, true))
                val readValue = collectionConfig.getList<Boolean>("list_boolean")
                check(readValue == mutableListOf(true, false, true))
                readValue.toString()
            })

            // 批量操作
            emit(task(
                name = "putAll 批量写入多个键值对",
                writeLabel = "写入: {b_str=\"bulk\", b_int=8, b_bool=false}"
            ) {
                collectionConfig.putAll(mapOf("b_str" to "bulk", "b_int" to 8, "b_bool" to false))
                val readValue = collectionConfig.toMap()["b_str"]
                check(readValue == "bulk")
                readValue.toString()
            })
            emit(task(
                name = "toMap 导出为 Map",
                writeLabel = "读取全部键值对"
            ) {
                val allData = collectionConfig.toMap()
                check(allData.containsKey("string_list") && allData.containsKey("b_str"))
                "Map size = ${allData.size}"
            })
            emit(task(
                name = "getAllKeys 获取所有键名",
                writeLabel = "读取全部键名集合"
            ) {
                val keys = collectionConfig.getAllKeys()
                check(keys.contains("string_list") && keys.contains("b_str"))
                "Set size = ${keys.size}"
            })
            emit(task(
                name = "containsKey 检查键是否存在",
                writeLabel = "检查键: \"string_list\""
            ) {
                val result = collectionConfig.containsKey("string_list")
                check(result)
                "containsKey(\"string_list\") = true"
            })

            // ==================== 5. 生命周期操作 ====================
            startCategory(ConfigTestCategoryType.LIFECYCLE)
            resetConfigUtilsGlobals()
            ConfigUtils.initialize(context)
            ConfigUtils.setGlobalPassword("")
            val lifecycleConfig = ConfigUtils(key = "lifecycle_${System.nanoTime()}")
            lifecycleConfig.put("temp", "value")

            emit(task(
                name = "remove 删除指定键",
                writeLabel = "remove(\"temp\")"
            ) {
                lifecycleConfig.remove("temp")
                val result = lifecycleConfig.containsKey("temp")
                check(!result)
                "containsKey(\"temp\") = false"
            })
            emit(task(
                name = "删除后再读取应返回默认值",
                writeLabel = "getString(\"temp\", \"fallback\")"
            ) {
                val readValue = lifecycleConfig.getString("temp", "fallback")
                check(readValue == "fallback")
                readValue
            })
            emit(task(
                name = "clearAll 清空全部数据",
                writeLabel = "写入 a=1, b=2 后 clearAll()"
            ) {
                lifecycleConfig.put("a", 1)
                lifecycleConfig.put("b", 2)
                lifecycleConfig.clearAll()
                val allData = lifecycleConfig.toMap()
                check(allData.isEmpty())
                "Map size = ${allData.size} (已清空)"
            })
            emit(task(
                name = "put(不支持类型) 应抛出 IllegalArgumentException",
                writeLabel = "put(\"bad\", ScenarioPayload(\"x\", 0))"
            ) {
                try {
                    lifecycleConfig.put("bad", ScenarioPayload("x", 0))
                    "未抛出异常 (不应该)"
                } catch (_: IllegalArgumentException) {
                    "抛出 IllegalArgumentException ✓"
                }
            })

            // ==================== 6. 加密与序列化 ====================
            startCategory(ConfigTestCategoryType.ENCRYPTION)
            resetConfigUtilsGlobals()
            val storeDir = File(context.cacheDir, "enc_test_${System.nanoTime()}")
            check(storeDir.mkdirs())
            ConfigUtils.initialize(storeDir.absolutePath)
            ConfigUtils.setGlobalPassword("secret-key")
            val encryptConfig = ConfigUtils(key = "enc_case", password = "secret-key")
            val payload = ScenarioPayload("alice", 3)
            val payloadList = mutableListOf(
                ScenarioPayload("alpha", 1),
                ScenarioPayload("beta", 2)
            )

            emit(task(
                name = "putObject 显式传入 Serializer",
                writeLabel = "写入: $payload"
            ) {
                encryptConfig.putObject("obj_explicit", payload, ScenarioPayload.serializer())
                val readValue = encryptConfig.getObject("obj_explicit", ScenarioPayload.serializer())
                check(readValue == payload)
                readValue.toString()
            })
            emit(task(
                name = "putObject reified 自动推断类型",
                writeLabel = "写入: $payload"
            ) {
                encryptConfig.putObject("obj_reified", payload)
                val readValue = encryptConfig.getObject<ScenarioPayload>("obj_reified")
                check(readValue == payload)
                readValue.toString()
            })
            emit(task(
                name = "putList 显式传入 Serializer",
                writeLabel = "写入: $payloadList"
            ) {
                encryptConfig.putList("list_explicit", payloadList, ScenarioPayload.serializer())
                val readValue = encryptConfig.getList("list_explicit", ScenarioPayload.serializer())
                check(readValue == payloadList)
                readValue.toString()
            })
            emit(task(
                name = "putList reified 自动推断类型",
                writeLabel = "写入: $payloadList"
            ) {
                encryptConfig.putList("list_reified", payloadList)
                val readValue = encryptConfig.getList<ScenarioPayload>("list_reified")
                check(readValue == payloadList)
                readValue.toString()
            })
            emit(task(
                name = "加密文件关闭后重新打开可正常读取",
                writeLabel = "new ConfigUtils(key=\"enc_case\", password=\"secret-key\")"
            ) {
                val reOpened = ConfigUtils(key = "enc_case", password = "secret-key")
                val readValue = reOpened.getObject<ScenarioPayload>("obj_reified")
                check(readValue == payload)
                readValue.toString()
            })
            emit(task(
                name = "getObject(key不存在) → 返回 null",
                writeLabel = "key: \"miss\" 不存在"
            ) {
                val readValue = encryptConfig.getObject("miss", ScenarioPayload.serializer())
                check(readValue == null)
                "null"
            })
            emit(task(
                name = "getList(key不存在) → 返回空列表",
                writeLabel = "key: \"miss\" 不存在"
            ) {
                val readValue = encryptConfig.getList<ScenarioPayload>("miss")
                check(readValue.isEmpty())
                "[] (空列表)"
            })
            ConfigUtils.setGlobalPassword("")
        }
    }
}

private inline fun task(
    name: String,
    writeLabel: String,
    crossinline action: () -> String,
): ConfigTestTaskResult {
    val start = System.nanoTime()
    return try {
        val readValue = action()
        val elapsed = (System.nanoTime() - start) / 1_000_000
        ConfigTestTaskResult(name, writeLabel, readValue, elapsed, true)
    } catch (e: Exception) {
        val elapsed = (System.nanoTime() - start) / 1_000_000
        ConfigTestTaskResult(
            name = name,
            writeValue = writeLabel,
            readValue = "ERROR",
            elapsedMs = elapsed,
            passed = false,
            error = e.message ?: e::class.java.simpleName
        )
    }
}

private fun resetConfigUtilsGlobals() {
    setStaticField("storePath", "")
    setStaticField("globalPassword", "")
}

private fun setStaticField(name: String, value: String) {
    val field = ConfigUtils::class.java.getDeclaredField(name)
    field.isAccessible = true
    field.set(null, value)
}
