package top.sacz.xphelper.dexkit.cache;


import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class DexKitCache {

    public static void clearCache() {
        new DexKitCacheProxy().clearCache();
    }

    public static void checkCacheExpired(Context context) {
        new DexKitCacheProxy().checkCacheExpired(context);
    }

    public static boolean exist(String key) {
        return new DexKitCacheProxy().keys().contains(key);
    }

    public static void putConstructorList(String key, List<Constructor<?>> constructorList) {
        new DexKitCacheProxy().putConstructorList(key, constructorList);
    }

    public static List<Constructor<?>> getConstructorList(String key) {
        return new DexKitCacheProxy().getConstructorList(key);
    }

    public static void putMethodList(String key, List<Method> methodList) {
        new DexKitCacheProxy().putMethodList(key, methodList);
    }

    public static List<Method> getMethodList(String key) {
        return new DexKitCacheProxy().getMethodList(key);
    }

    public static void putFieldList(String key, List<Field> fieldList) {
        new DexKitCacheProxy().putFieldList(key, fieldList);
    }

    public static List<Field> getFieldList(String key) {
        return new DexKitCacheProxy().getFieldList(key);
    }

    public static void putClassList(String key, List<Class<?>> classList) {
        new DexKitCacheProxy().putClassList(key, classList);
    }

    public static List<Class<?>> getClassList(String key) {
        return new DexKitCacheProxy().getClassList(key);
    }

    public static String getAllMethodString() {
        return new DexKitCacheProxy().toString();
    }
}
