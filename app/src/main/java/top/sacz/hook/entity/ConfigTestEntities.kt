package top.sacz.hook.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ConfigTestTaskResult(
    val name: String,
    val writeValue: String,
    val readValue: String,
    val elapsedMs: Long,
    val passed: Boolean,
    val error: String? = null
)

data class ConfigTestCategoryResult(
    val type: ConfigTestCategoryType,
    val tasks: List<ConfigTestTaskResult> = emptyList()
)

enum class ConfigTestCategoryType(val displayName: String) {
    SAFETY("构造安全验证"),
    BASIC_TYPES("基础类型读写"),
    DEFAULTS("默认值与边界"),
    COLLECTIONS("集合与批量操作"),
    LIFECYCLE("生命周期操作"),
    ENCRYPTION("加密与序列化")
}

@Serializable
data class ScenarioPayload(
    @SerialName("name")
    val name: String,
    @SerialName("count")
    val count: Int
)
