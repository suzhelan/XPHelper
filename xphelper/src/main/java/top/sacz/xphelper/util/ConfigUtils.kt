package top.sacz.xphelper.util

import android.content.Context
import io.fastkv.FastKV
import io.fastkv.interfaces.FastCipher
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer


/**
 * 简单数据存储类
 * 完整构造方法 默认会生成无密码文件名为default的数据库
 * 如果需要加密传入密码
 */
class ConfigUtils @JvmOverloads constructor(
    key: String = "default",
    password: String = globalPassword,
) {
    @PublishedApi
    internal val jsonTool = Json {
        //忽略未知jsonKey
        ignoreUnknownKeys = true
        //是否将null的属性写入json 默认true
        explicitNulls = true
        //是否使用默认值 默认false
        encodeDefaults = false
        //是否格式化json
        prettyPrint = true
        //宽容解析模式 可以解析不规范的json格式
        isLenient = false
    }

    private var id: String = key

    private var kv: FastKV


    init {
        if (storePath.isEmpty()) {
            throw RuntimeException("storePath is empty(请使用KvHelper.initialize(String path)初始化")
        }
        kv = if (globalPassword.isEmpty()) {
            FastKV.Builder(storePath, id)
                .build()
        } else {
            FastKV.Builder(storePath, id)
                .cipher(Cipher(password))
                .build()
        }
    }


    companion object {
        private var storePath = ""

        private var globalPassword = ""

        /**
         * 初始化 传入文件夹路径
         */
        @JvmStatic
        fun initialize(path: String) {
            storePath = path
        }

        @JvmStatic
        fun setGlobalPassword(password: String) {
            globalPassword = password
        }

        @JvmStatic
        fun initialize(context: Context) {
            storePath = context.filesDir.absolutePath + "/XpHelper"
        }
    }

    /**
     * 保存数据的方法,这种写法适用于简单存储
     * @param key
     * @param value
     */
    fun put(key: String, value: Any) {
        when (value) {
            is String -> {
                kv.putString(key, value)
            }

            is Int -> {
                kv.putInt(key, value)
            }

            is Boolean -> {
                kv.putBoolean(key, value)
            }

            is Float -> {
                kv.putFloat(key, value)
            }

            is Long -> {
                kv.putLong(key, value)
            }

            is Double -> {
                kv.putDouble(key, value)
            }

            is ByteArray -> {
                kv.putArray(key, value)
            }

            is List<*> -> {
                kv.putString(key, jsonTool.encodeToString(anyToJsonElement(value)))
            }

            is Map<*, *> -> {
                kv.putString(key, jsonTool.encodeToString(anyToJsonElement(value)))
            }

            else -> {
                @Suppress("UNCHECKED_CAST")
                val serializer = jsonTool.serializersModule
                    .serializer(value::class.java)
                putObject(key, value, serializer)
            }
        }
    }


    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     */
    fun getInt(key: String, def: Int = 0): Int {
        return kv.getInt(key, def)
    }

    fun getDouble(key: String, def: Double = 0.00): Double {
        return kv.getDouble(key, def)
    }

    fun getLong(key: String, def: Long = 0L): Long {
        return kv.getLong(key, def)
    }

    fun getBoolean(key: String, def: Boolean = false): Boolean {
        return kv.getBoolean(key, def)
    }

    fun getFloat(key: String, def: Float = 0f): Float {
        return kv.getFloat(key, def)
    }

    fun getBytes(key: String, def: ByteArray = byteArrayOf()): ByteArray {
        return kv.getArray(key, def)
    }

    fun getString(key: String, def: String = ""): String {
        return kv.getString(key, def) ?: ""
    }

    fun putStringList(key: String, value: List<String>) {
        kv.putString(key, jsonTool.encodeToString(value))
    }

    fun getStringList(key: String): MutableList<String> {
        val data = kv.getString(key)
        if (data.isNullOrEmpty()) {
            return arrayListOf()
        }
        return jsonTool.decodeFromString<List<String>>(data).toMutableList()
    }


    fun <T> putObject(key: String, value: T, serializer: KSerializer<T>) {
        kv.putString(key, jsonTool.encodeToString(serializer, value))
    }

    inline fun <reified T> putObject(key: String, value: T) {
        putObject(key, value, serializer())
    }

    fun <T> getObject(key: String, serializer: KSerializer<T>): T? {
        val data = kv.getString(key)
        if (data.isNullOrEmpty()) {
            return null
        }
        return jsonTool.decodeFromString(serializer, data)
    }

    inline fun <reified T> getObject(key: String): T? {
        return getObject(key, serializer())
    }

    fun <T> getList(key: String, serializer: KSerializer<T>): MutableList<T> {
        val data = kv.getString(key)
        if (data.isNullOrEmpty()) {
            return mutableListOf()
        }
        return jsonTool.decodeFromString(ListSerializer(serializer), data).toMutableList()
    }

    inline fun <reified T> getList(key: String): MutableList<T> {
        return getList(key, serializer())
    }

    fun <T> putList(key: String, value: MutableList<T>, serializer: KSerializer<T>) {
        kv.putString(key, jsonTool.encodeToString(ListSerializer(serializer), value))
    }

    inline fun <reified T> putList(key: String, value: MutableList<T>) {
        putList(key, value, serializer())
    }

    /**
     * Java桥接方法
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> putObject(key: String, value: T, clazz: Class<T>) {
        val serializer = jsonTool.serializersModule.serializer(clazz) as KSerializer<T>
        putObject(key, value, serializer)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getObject(key: String, clazz: Class<T>): T? {
        val serializer = jsonTool.serializersModule.serializer(clazz) as KSerializer<T>
        return getObject(key, serializer)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> putList(key: String, value: MutableList<T>, clazz: Class<T>) {
        val serializer = jsonTool.serializersModule.serializer(clazz) as KSerializer<T>
        putList(key, value, serializer)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getList(key: String, clazz: Class<T>): MutableList<T> {
        val serializer = jsonTool.serializersModule.serializer(clazz) as KSerializer<T>
        return getList(key, serializer)
    }

    /**
     * 批量保存数据
     */
    fun putAll(map: Map<String, Any>) {
        kv.putAll(map)
    }

    /**
     * 转Map
     */
    fun toMap(): Map<String, Any> {
        return kv.all
    }

    /**
     * 清除所有key
     */
    fun clearAll() {
        kv.clear()
    }

    fun remove(key: String) {
        kv.remove(key)
    }

    /**
     * 获取所有key
     */
    fun getAllKeys(): MutableSet<String> {
        return kv.all.keys
    }

    /**
     * 是否包含某个key
     */
    fun containsKey(key: String): Boolean {
        return kv.contains(key)
    }

    /**
     * 将任意值递归转为 JsonElement，支持混合类型的 List 和 Map
     */
    private fun anyToJsonElement(value: Any?): JsonElement {
        return when (value) {
            null -> JsonNull
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is List<*> -> JsonArray(value.map { anyToJsonElement(it) })
            is Map<*, *> -> JsonObject(
                value.entries.associate { (k, v) ->
                    k.toString() to anyToJsonElement(v)
                }
            )

            else -> {
                @Suppress("UNCHECKED_CAST")
                val serializer = jsonTool.serializersModule
                    .serializer(value::class.java)
                jsonTool.encodeToJsonElement(serializer, value)
            }
        }
    }


    /**
     * fast kv的加密实现接口
     */
    private class Cipher(val key: String) : FastCipher {
        override fun encrypt(src: ByteArray): ByteArray {
            return AESHelper.encrypt(src, key)
        }

        override fun decrypt(dst: ByteArray): ByteArray {
            return AESHelper.decrypt(dst, key)
        }

        override fun encrypt(src: Int): Int {
            return src
        }

        override fun encrypt(src: Long): Long {
            return src
        }

        override fun decrypt(dst: Int): Int {
            return dst
        }

        override fun decrypt(dst: Long): Long {
            return dst
        }

    }
}
