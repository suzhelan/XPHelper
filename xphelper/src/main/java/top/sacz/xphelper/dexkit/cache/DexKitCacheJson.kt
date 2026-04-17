package top.sacz.xphelper.dexkit.cache

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import top.sacz.xphelper.reflect.ClassUtils
import top.sacz.xphelper.reflect.ConstructorUtils
import top.sacz.xphelper.reflect.FieldUtils
import top.sacz.xphelper.reflect.MethodUtils
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

@SuppressLint("UnsafeOptInUsageError")
object DexKitCacheJson {
    private val json = Json {
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

    @JvmStatic
    fun encodeConstructor(constructor: Constructor<*>): String {
        return json.encodeToString(
            ConstructorInfo(
                declareClass = constructor.declaringClass.name,
                params = constructor.parameterTypes.map { it.name }
            )
        )
    }

    @JvmStatic
    fun decodeConstructor(constructorInfoJson: String): Constructor<*> {
        val constructorInfo = json.decodeFromString<ConstructorInfo>(constructorInfoJson)
        val params = constructorInfo.params.map(ClassUtils::findClass).toTypedArray()
        return ConstructorUtils.create(constructorInfo.declareClass)
            .paramTypes(*params)
            .first()
    }

    @JvmStatic
    fun encodeMethod(method: Method): String {
        method.isAccessible = true
        return json.encodeToString(
            MethodInfo(
                declareClass = method.declaringClass.name,
                methodName = method.name,
                params = method.parameterTypes.map { it.name },
                returnType = method.returnType.name
            )
        )
    }

    @JvmStatic
    fun decodeMethod(methodInfoJson: String): Method {
        val methodInfo = json.decodeFromString<MethodInfo>(methodInfoJson)
        val params = methodInfo.params.map(ClassUtils::findClass).toTypedArray()
        return MethodUtils.create(methodInfo.declareClass)
            .methodName(methodInfo.methodName)
            .returnType(ClassUtils.findClass(methodInfo.returnType))
            .params(*params)
            .first()
    }

    @JvmStatic
    fun encodeField(field: Field): String {
        field.isAccessible = true
        return json.encodeToString(
            FieldInfo(
                declareClass = field.declaringClass.name,
                fieldName = field.name,
                fieldType = field.type.name
            )
        )
    }

    @JvmStatic
    fun decodeField(fieldInfoJson: String): Field {
        val fieldInfo = json.decodeFromString<FieldInfo>(fieldInfoJson)
        return FieldUtils.create(fieldInfo.declareClass)
            .fieldName(fieldInfo.fieldName)
            .fieldType(ClassUtils.findClass(fieldInfo.fieldType))
            .first()
    }

    @JvmStatic
    fun encodeClass(clazz: Class<*>): String {
        return json.encodeToString(ClassInfo(className = clazz.name))
    }

    @JvmStatic
    fun decodeClass(classInfoJson: String): Class<*> {
        val classInfo = json.decodeFromString<ClassInfo>(classInfoJson)
        return ClassUtils.findClass(classInfo.className)
    }


    @Serializable
    data class ConstructorInfo(
        @SerialName("DeclareClass") val declareClass: String,
        @SerialName("Params") val params: List<String>,
    )

    @Serializable
    private data class MethodInfo(
        @SerialName("DeclareClass") val declareClass: String,
        @SerialName("MethodName") val methodName: String,
        @SerialName("Params") val params: List<String>,
        @SerialName("ReturnType") val returnType: String,
    )

    @Serializable
    private data class FieldInfo(
        @SerialName("DeclareClass") val declareClass: String,
        @SerialName("FieldName") val fieldName: String,
        @SerialName("FieldType") val fieldType: String,
    )

    @Serializable
    private data class ClassInfo(
        @SerialName("ClassName") val className: String,
    )
}
