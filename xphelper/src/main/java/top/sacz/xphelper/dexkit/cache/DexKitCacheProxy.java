package top.sacz.xphelper.dexkit.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import top.sacz.xphelper.reflect.ClassUtils;
import top.sacz.xphelper.reflect.ConstructorUtils;
import top.sacz.xphelper.reflect.FieldUtils;
import top.sacz.xphelper.reflect.MethodUtils;
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
        configUtils.put(key, infoList);
    }

    public List<Constructor<?>> getConstructorList(String key) {
        if (!configUtils.containsKey(key)) {
            return null;
        }
        ArrayList<Constructor<?>> result = new ArrayList<>();
        ArrayList<String> constructorInfoList = configUtils.getObject(key, new TypeReference<>() {
        });
        if (constructorInfoList != null) {
            for (String constructorInfo : constructorInfoList) {
                result.add(findConstructorByJSONString(constructorInfo));
            }
        }
        return result;
    }

    private String getConstructorInfoJSON(Constructor<?> constructor) {
        JSONObject result = new JSONObject();
        String declareClass = constructor.getDeclaringClass().getName();
        Class<?>[] methodParams = constructor.getParameterTypes();
        JSONArray params = new JSONArray();
        for (Class<?> type : methodParams) {
            params.add(type.getName());
        }
        result.put("DeclareClass", declareClass);
        result.put("Params", params);
        return result.toString();
    }

    private Constructor<?> findConstructorByJSONString(String constructorInfoStrJSON) {
        JSONObject constructorInfo = JSONObject.parseObject(constructorInfoStrJSON);
        String declareClass = constructorInfo.getString("DeclareClass");
        JSONArray methodParams = constructorInfo.getJSONArray("Params");
        Class<?>[] params = new Class[methodParams.size()];
        for (int i = 0; i < params.length; i++) {
            params[i] = ClassUtils.findClass(methodParams.getString(i));
        }
        return ConstructorUtils.create(declareClass)
                .paramTypes(params)
                .first();
    }

    public void putMethodList(String key, List<Method> methodList) {
        ArrayList<String> infoList = new ArrayList<>();
        for (Method method : methodList) {
            infoList.add(getMethodInfoJSON(method));
        }
        configUtils.put(key, infoList);
    }

    public List<Method> getMethodList(String key) {
        if (!configUtils.containsKey(key)) {
            return null;
        }
        ArrayList<Method> result = new ArrayList<>();
        ArrayList<String> methodInfoList = configUtils.getObject(key, new TypeReference<>() {
        });
        if (methodInfoList != null) {
            for (String methodInfo : methodInfoList) {
                result.add(findMethodByJSONString(methodInfo));
            }
        }
        return result;
    }

    private Method findMethodByJSONString(String methodInfoStrJSON) {
        JSONObject methodInfo = JSONObject.parseObject(methodInfoStrJSON);
        String methodName = methodInfo.getString("MethodName");
        String declareClass = methodInfo.getString("DeclareClass");
        String ReturnType = methodInfo.getString("ReturnType");
        JSONArray methodParams = methodInfo.getJSONArray("Params");
        Class<?>[] params = new Class[methodParams.size()];
        for (int i = 0; i < params.length; i++) {
            params[i] = ClassUtils.findClass(methodParams.getString(i));
        }
        return MethodUtils.create(declareClass)
                .methodName(methodName)
                .returnType(ClassUtils.findClass(ReturnType))
                .params(params)
                .first();
    }

    private String getMethodInfoJSON(Method method) {
        method.setAccessible(true);
        JSONObject result = new JSONObject();
        String methodName = method.getName();
        String declareClass = method.getDeclaringClass().getName();
        Class<?>[] methodParams = method.getParameterTypes();
        JSONArray params = new JSONArray();
        for (Class<?> type : methodParams) {
            params.add(type.getName());
        }
        result.put("DeclareClass", declareClass);
        result.put("MethodName", methodName);
        result.put("Params", params);
        result.put("ReturnType", method.getReturnType().getName());
        return result.toString();
    }

    public void putFieldList(String key, List<Field> fieldList) {
        ArrayList<String> infoList = new ArrayList<>();
        for (Field field : fieldList) {
            infoList.add(getFieldInfoJSON(field));
        }
        configUtils.put(key, infoList);
    }

    public List<Field> getFieldList(String key) {
        if (!configUtils.containsKey(key)) {
            return null;
        }
        ArrayList<Field> result = new ArrayList<>();
        ArrayList<String> fieldInfoList = configUtils.getObject(key, new TypeReference<>() {
        });
        if (fieldInfoList != null) {
            for (String fieldInfo : fieldInfoList) {
                result.add(findFieldByJSONString(fieldInfo));
            }
        }
        return result;
    }

    private Field findFieldByJSONString(String fieldInfoStrJSON) {
        JSONObject fieldInfo = JSONObject.parseObject(fieldInfoStrJSON);
        String fieldName = fieldInfo.getString("FieldName");
        String declareClass = fieldInfo.getString("DeclareClass");
        String fieldType = fieldInfo.getString("FieldType");
        return FieldUtils.create(declareClass)
                .fieldName(fieldName)
                .fieldType(ClassUtils.findClass(fieldType))
                .first();
    }

    private String getFieldInfoJSON(Field field) {
        field.setAccessible(true);
        JSONObject result = new JSONObject();
        String fieldName = field.getName();
        String declareClass = field.getDeclaringClass().getName();
        result.put("DeclareClass", declareClass);
        result.put("FieldName", fieldName);
        result.put("FieldType", field.getType().getName());
        return result.toString();
    }

    public void putClassList(String key, List<Class<?>> classList) {
        ArrayList<String> infoList = new ArrayList<>();
        for (Class<?> clazz : classList) {
            infoList.add(getClassInfoJSON(clazz));
        }
        configUtils.put(key, infoList);
    }

    public List<Class<?>> getClassList(String key) {
        if (!configUtils.containsKey(key)) {
            return null;
        }
        ArrayList<Class<?>> result = new ArrayList<>();
        ArrayList<String> classInfoList = configUtils.getObject(key, new TypeReference<>() {
        });
        if (classInfoList != null) {
            for (String classInfo : classInfoList) {
                result.add(findClassByJSONString(classInfo));
            }
        }
        return result;
    }

    public Class<?> findClassByJSONString(String classInfoJSON) {
        JSONObject classInfo = JSONObject.parseObject(classInfoJSON);
        String className = classInfo.getString("ClassName");
        return ClassUtils.findClass(className);
    }

    private String getClassInfoJSON(Class<?> clazz) {
        JSONObject result = new JSONObject();
        result.put("ClassName", clazz.getName());
        return result.toString();
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
