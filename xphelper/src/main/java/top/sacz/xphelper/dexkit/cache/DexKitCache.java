package top.sacz.xphelper.dexkit.cache;


import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import top.sacz.xphelper.util.CacheKeyCompressor;

public class DexKitCache {

    public static void clearCache() {
        new DexKitCacheProxy().clearCache();
    }

    public static void checkCacheExpired(Context context) {
        new DexKitCacheProxy().checkCacheExpired(context);
    }

    public static boolean exist(String key) {
        String fixKey = CacheKeyCompressor.compressCacheKey(key);
        return new DexKitCacheProxy().keys().contains(fixKey);
    }

    public static void putConstructorList(String key, List<Constructor<?>> constructorList) {
        String fixKey = CacheKeyCompressor.compressCacheKey(key);
        new DexKitCacheProxy().putConstructorList(fixKey, constructorList);
    }

    public static List<Constructor<?>> getConstructorList(String key) {
        String fixKey = CacheKeyCompressor.compressCacheKey(key);
        return new DexKitCacheProxy().getConstructorList(fixKey);
    }

    public static void putMethodList(String key, List<Method> methodList) {
        String fixKey = CacheKeyCompressor.compressCacheKey(key);
        new DexKitCacheProxy().putMethodList(fixKey, methodList);
    }

    public static List<Method> getMethodList(String key) {
        String fixKey = CacheKeyCompressor.compressCacheKey(key);
        return new DexKitCacheProxy().getMethodList(fixKey);
    }

    public static void putFieldList(String key, List<Field> fieldList) {
        String fixKey = CacheKeyCompressor.compressCacheKey(key);
        new DexKitCacheProxy().putFieldList(fixKey, fieldList);
    }

    public static List<Field> getFieldList(String key) {
        String fixKey = CacheKeyCompressor.compressCacheKey(key);
        return new DexKitCacheProxy().getFieldList(fixKey);
    }

    public static void putClassList(String key, List<Class<?>> classList) {
        String fixKey = CacheKeyCompressor.compressCacheKey(key);
        new DexKitCacheProxy().putClassList(fixKey, classList);
    }

    public static List<Class<?>> getClassList(String key) {
        String fixKey = CacheKeyCompressor.compressCacheKey(key);
        return new DexKitCacheProxy().getClassList(fixKey);
    }

    public static String getAllMethodString() {
        return new DexKitCacheProxy().toString();
    }

}
