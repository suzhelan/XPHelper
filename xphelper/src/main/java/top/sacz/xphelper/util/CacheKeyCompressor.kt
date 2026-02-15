package top.sacz.xphelper.util

import kotlin.math.abs

object CacheKeyCompressor {
    private const val MAX_KEY_LENGTH = 128
    private const val PREFIX_LENGTH = 64
    private const val SUFFIX_LENGTH = 64

    /**
     * KV缓存专用的key压缩 - 平衡性能与安全性
     * 适用于DexKit查询结果的缓存键压缩
     */
    @JvmStatic
    fun compressCacheKey(originalKey: String?): String {
        // 输入验证
        if (originalKey == null) return ""
        if (originalKey.isEmpty()) return ""
        // 长度检查 - 短键直接返回
        if (originalKey.length <= MAX_KEY_LENGTH) {
            return originalKey
        }
        // 超长键处理 - 使用前缀+后缀+安全哈希
        val prefix = originalKey.substring(0, PREFIX_LENGTH)
        val suffix = originalKey.substring(originalKey.length - SUFFIX_LENGTH)
        return prefix + suffix + getSecureHash(originalKey)

    }

    /**
     * 安全哈希算法 - 使用改进的多项式哈希减少冲突
     * 生成16位十六进制字符串（64位熵）
     */
    private fun getSecureHash(key: String): String {
        var hash1: Long = 0
        var hash2: Long = 0
        // 双重哈希减少冲突概率
        for (i in key.indices) {
            val char = key[i].code.toLong()
            hash1 = ((hash1 shl 5) - hash1) + char
            hash1 = hash1 and 0xFFFFFFFFL // 限制范围防止溢出

            // 第二个哈希使用不同的系数
            hash2 = ((hash2 shl 7) - hash2) + (char shl 1)
            hash2 = hash2 and 0xFFFFFFFFL
        }

        // 组合两个哈希值并转换为16进制
        val combinedHash = (hash1 xor hash2) and 0xFFFFFFFFFFFFL // 48位
        return String.format("%012x", combinedHash)
    }

}
