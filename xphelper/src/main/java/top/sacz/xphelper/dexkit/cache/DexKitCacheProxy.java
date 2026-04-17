package top.sacz.xphelper.dexkit.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import top.sacz.xphelper.util.ConfigUtils;

public class DexKitCacheProxy {
    private static final String TAG = "DexKitCacheProxy";

    ConfigUtils configUtils = new ConfigUtils("DexKitCache");

    public void checkCacheExpired(Context context) {
        //获取应用的版本号
        try {
            String key = "version";
            String packageName = context.getPackageName();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            String versionName = packageInfo.versionName;
            long versionCode = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = packageInfo.getLongVersionCode();
            } else {
                versionCode = packageInfo.versionCode;
            }
            String versionFlag = versionName + "_" + versionCode;
            String configFlag = configUtils.getString(key, "");
            if (configFlag.equals(versionFlag)) {
                return;
            }
            clearCache();
            configUtils.put(key, versionFlag);
            Log.d(TAG, "checkCacheExpired: Host version updated Cache cleaned old:" + configFlag + " new:" + versionFlag);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "checkCacheExpired: " + Log.getStackTraceString(e));
        }
    }

    public Set<String> keys() {
        return configUtils.getAllKeys();
    }

    public void clearCache() {
        configUtils.clearAll();
    }

    public void putConstructorList(String key, List<Constructor<?>> constructorList) {
        ArrayList<String> infoList = new ArrayList<>();
        for (Constructor<?> constructor : constructorList) {
            infoList.add(getConstructorInfoJSON(constructor));
        }
        configUtils.putStringList(key, infoList);
    }

    public List<Constructor<?>> getConstructorList(String key) {
        if (!configUtils.containsKey(key)) {
            return null;
        }
        ArrayList<Constructor<?>> result = new ArrayList<>();
        List<String> constructorInfoList = configUtils.getStringList(key);
        for (String constructorInfo : constructorInfoList) {
            result.add(findConstructorByJSONString(constructorInfo));
        }
        return result;
    }

    private String getConstructorInfoJSON(Constructor<?> constructor) {
        return DexKitCacheJson.encodeConstructor(constructor);
    }

    private Constructor<?> findConstructorByJSONString(String constructorInfoStrJSON) {
        return DexKitCacheJson.decodeConstructor(constructorInfoStrJSON);
    }

    public void putMethodList(String key, List<Method> methodList) {
        ArrayList<String> infoList = new ArrayList<>();
        for (Method method : methodList) {
            infoList.add(getMethodInfoJSON(method));
        }
        configUtils.putStringList(key, infoList);
    }

    public List<Method> getMethodList(String key) {
        if (!configUtils.containsKey(key)) {
            return null;
        }
        ArrayList<Method> result = new ArrayList<>();
        List<String> methodInfoList = configUtils.getStringList(key);
        for (String methodInfo : methodInfoList) {
            result.add(findMethodByJSONString(methodInfo));
        }
        return result;
    }

    private Method findMethodByJSONString(String methodInfoStrJSON) {
        return DexKitCacheJson.decodeMethod(methodInfoStrJSON);
    }

    private String getMethodInfoJSON(Method method) {
        return DexKitCacheJson.encodeMethod(method);
    }

    public void putFieldList(String key, List<Field> fieldList) {
        ArrayList<String> infoList = new ArrayList<>();
        for (Field field : fieldList) {
            infoList.add(getFieldInfoJSON(field));
        }
        configUtils.putStringList(key, infoList);
    }

    public List<Field> getFieldList(String key) {
        if (!configUtils.containsKey(key)) {
            return null;
        }
        ArrayList<Field> result = new ArrayList<>();
        List<String> fieldInfoList = configUtils.getStringList(key);
        for (String fieldInfo : fieldInfoList) {
            result.add(findFieldByJSONString(fieldInfo));
        }
        return result;
    }

    private Field findFieldByJSONString(String fieldInfoStrJSON) {
        return DexKitCacheJson.decodeField(fieldInfoStrJSON);
    }

    private String getFieldInfoJSON(Field field) {
        return DexKitCacheJson.encodeField(field);
    }

    public void putClassList(String key, List<Class<?>> classList) {
        ArrayList<String> infoList = new ArrayList<>();
        for (Class<?> clazz : classList) {
            infoList.add(getClassInfoJSON(clazz));
        }
        configUtils.putStringList(key, infoList);
    }

    public List<Class<?>> getClassList(String key) {
        if (!configUtils.containsKey(key)) {
            return null;
        }
        ArrayList<Class<?>> result = new ArrayList<>();
        List<String> classInfoList = configUtils.getStringList(key);
        for (String classInfo : classInfoList) {
            result.add(findClassByJSONString(classInfo));
        }
        return result;
    }

    public Class<?> findClassByJSONString(String classInfoJSON) {
        return DexKitCacheJson.decodeClass(classInfoJSON);
    }

    private String getClassInfoJSON(Class<?> clazz) {
        return DexKitCacheJson.encodeClass(clazz);
    }

    @Override
    @NonNull
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : keys()) {
            stringBuilder.append(key).append(":").append(getMethodList(key)).append("\n");
        }
        return stringBuilder.toString();
    }
}
