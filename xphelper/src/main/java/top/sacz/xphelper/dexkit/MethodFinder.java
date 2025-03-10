package top.sacz.xphelper.dexkit;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.MatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import org.luckypray.dexkit.result.MethodDataList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import top.sacz.xphelper.dexkit.cache.DexKitCache;
import top.sacz.xphelper.reflect.ClassUtils;

public class MethodFinder {

    private Class<?> declaredClass;//方法声明类
    private Class<?>[] parameters;//方法的参数列表
    private String methodName;//方法名称
    private Class<?> returnType;//方法的返回值类型
    private String[] usedString;//方法中使用的字符串列表
    private Method[] invokeMethods;//方法中调用的方法列表
    private Method[] callMethods;//调用了该方法的方法列表
    private long[] usingNumbers;//方法中使用的数字列表
    private int paramCount;//参数数量
    private boolean isParamCount = false;
    private int modifiers;//修饰符
    private boolean isModifiers = false;
    private MatchType matchType;
    private String[] searchPackages;
    private String[] excludePackages;

    private FieldFinder[] usedFields;

    /**
     * 构造实例
     *
     * @return
     */
    public static MethodFinder build() {
        return new MethodFinder();
    }

    /**
     * Method转Dexkit的MethodMatcher
     *
     * @param method
     * @return
     */
    public static MethodMatcher toMethodMatcher(Method method) {
        return MethodMatcher.create(method);
    }

    /**
     * 通过Method构造实例
     *
     * @param method
     * @return
     */
    public static MethodFinder from(Method method) {
        MethodFinder methodFinder = new MethodFinder();
        methodFinder.declaredClass = method.getDeclaringClass();
        methodFinder.parameters = method.getParameterTypes();
        methodFinder.methodName = method.getName();
        methodFinder.returnType = method.getReturnType();
        methodFinder.isModifiers = true;
        methodFinder.modifiers = method.getModifiers();
        methodFinder.matchType = MatchType.Equals;
        return methodFinder;
    }

    /**
     * 设置方法中调用的字段列表
     *
     * @param fieldFinders
     * @return
     */
    public MethodFinder usedField(FieldFinder... fieldFinders) {
        usedFields = fieldFinders;
        return this;
    }

    /**
     * 设置方法中调用的字段列表
     *
     * @param fields
     * @return
     */
    public MethodFinder usedField(Field... fields) {
        usedFields = new FieldFinder[fields.length];
        for (int i = 0; i < fields.length; i++) {
            usedFields[i] = FieldFinder.from(fields[i]);
        }
        return this;
    }

    /**
     * 设置方法声明类
     *
     * @param declaredClass
     * @return
     */
    public MethodFinder declaredClass(Class<?> declaredClass) {
        this.declaredClass = declaredClass;
        return this;
    }

    /**
     * 设置方法的参数列表
     *
     * @param parameters
     * @return
     */
    public MethodFinder parameters(Class<?>... parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * 设置方法名称
     *
     * @param name
     * @return
     */
    public MethodFinder methodName(String name) {
        methodName = name;
        return this;
    }

    /**
     * 设置方法的返回值类型
     *
     * @param returnTypeClass
     * @return
     */
    public MethodFinder returnType(Class<?> returnTypeClass) {
        returnType = returnTypeClass;
        return this;
    }

    /**
     * 设置方法中调用的方法列表
     *
     * @param methods
     * @return
     */
    public MethodFinder invokeMethods(Method... methods) {
        invokeMethods = methods;
        return this;
    }

    /**
     * 设置调用了该方法的方法列表(也就是此方法被哪些方法调用)
     *
     * @param methods
     * @return
     */
    public MethodFinder callMethods(Method... methods) {
        this.callMethods = methods;
        return this;
    }

    /**
     * 设置方法中使用的数字列表
     *
     * @param numbers
     * @return
     */
    public MethodFinder usingNumbers(long... numbers) {
        this.usingNumbers = numbers;
        return this;
    }

    /**
     * 设置参数数量
     *
     * @param count
     * @return
     */
    public MethodFinder paramCount(int count) {
        this.paramCount = count;
        this.isParamCount = true;
        return this;
    }

    /**
     * 设置方法中使用的字符串列表
     *
     * @param strings
     * @return
     */
    public MethodFinder useString(String... strings) {
        this.usedString = strings;
        return this;
    }

    /**
     * 设置方法的修饰符
     *
     * @param modifiers
     * @param matchType
     * @return
     */
    public MethodFinder modifiers(int modifiers, MatchType matchType) {
        this.modifiers = modifiers;
        this.isModifiers = true;
        this.matchType = matchType;
        return this;
    }

    /**
     * 设置搜索的包名列表
     *
     * @param strings
     * @return
     */
    public MethodFinder searchPackages(String... strings) {
        this.searchPackages = strings;
        return this;
    }

    /**
     * 设置排除的包名列表
     *
     * @param strings
     * @return
     */
    public MethodFinder excludePackages(String... strings) {
        this.excludePackages = strings;
        return this;
    }


    private FindMethod buildFindMethod() {
        FindMethod findMethod = FindMethod.create();
        if (searchPackages != null) {
            findMethod.searchPackages(searchPackages);
        }
        if (excludePackages != null) {
            findMethod.excludePackages(excludePackages);
        }
        return findMethod.matcher(buildMethodMatcher());
    }


    /**
     * 构造dexkit method方法的匹配条件
     * @return
     */
    public MethodMatcher buildMethodMatcher() {
        MethodMatcher methodMatcher = MethodMatcher.create();
        if (declaredClass != null) {
            methodMatcher.declaredClass(declaredClass);
        }
        if (methodName != null && !methodName.isEmpty()) {
            methodMatcher.name(methodName);
        }
        if (returnType != null) {
            methodMatcher.returnType(returnType);
        }
        if (usedString != null && usedString.length != 0) {
            methodMatcher.usingStrings(usedString);
        }
        if (parameters != null) {
            for (Class<?> parameterClass : parameters) {
                methodMatcher.addParamType(parameterClass);
            }
        }
        if (usedFields != null) {
            for (FieldFinder usedField : usedFields) {
                methodMatcher.addUsingField(usedField.buildFieldMatcher());
            }
        }
        if (invokeMethods != null) {
            for (Method invokeMethod : invokeMethods) {
                methodMatcher.addInvoke(MethodMatcher.create(invokeMethod));
            }
        }
        if (callMethods != null) {
            for (Method callMethod : callMethods) {
                methodMatcher.addCaller(MethodMatcher.create(callMethod));
            }
        }
        if (usingNumbers != null) {
            for (long usingNumber : usingNumbers) {
                methodMatcher.addUsingNumber(usingNumber);
            }
        }
        if (isParamCount) {
            methodMatcher.paramCount(paramCount);
        }
        if (isModifiers) {
            methodMatcher.modifiers(modifiers, matchType);
        }
        return methodMatcher;
    }

    /**
     * 查找方法 返回结果列表
     *
     * @return
     * @throws NoSuchMethodException
     */
    public List<Method> find() {
        try {
            //先查缓存
            List<Method> cache = DexKitCache.getMethodList(toString());
            if (cache != null) {
                return cache;
            }
            ArrayList<Method> methods = new ArrayList<>();
            //使用dexkit查找方法
            MethodDataList methodDataList = DexFinder.getDexKitBridge().findMethod(buildFindMethod());
            if (methodDataList.isEmpty()) {
                DexKitCache.putMethodList(toString(), methods);
                return methods;
            }
            for (MethodData methodData : methodDataList) {
                Method method = methodData.getMethodInstance(ClassUtils.getClassLoader());
                method.setAccessible(true);
                methods.add(method);
            }
            //写入缓存
            DexKitCache.putMethodList(toString(), methods);
            return methods;
        } catch (NoSuchMethodException e) {
            return new ArrayList<>();
        }
    }

    /**
     * 查找方法 返回第一个方法 如果不存在则返回null
     */
    public Method firstOrNull() {
        List<Method> methods = find();
        if (methods.isEmpty()) {
            return null;
        }
        return methods.get(0);
    }

    /**
     * 查找方法 返回第一个方法 如果不存在则抛出异常
     */
    public Method first() throws Exception {
        List<Method> methods = find();
        if (methods.isEmpty()) {
            throw new NoSuchMethodException("No method found :" + this);
        }
        return methods.get(0);
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        //拼入字段值 无需拼入字段名 如果为空则不拼入
        if (declaredClass != null) {
            builder.append(declaredClass.getName());
        }
        if (methodName != null && !methodName.isEmpty()) {
            builder.append(methodName);
        }
        if (returnType != null) {
            builder.append(returnType.getName());
        }
        if (parameters != null) {
            builder.append(Arrays.toString(parameters));
        }
        if (invokeMethods != null) {
            builder.append(Arrays.toString(invokeMethods));
        }
        if (callMethods != null) {
            builder.append(Arrays.toString(callMethods));
        }
        if (usingNumbers != null) {
            builder.append(Arrays.toString(usingNumbers));
        }
        if (isParamCount) {
            builder.append(paramCount);
        }
        if (isModifiers) {
            builder.append(modifiers);
        }
        if (usedString != null && usedString.length != 0) {
            builder.append(Arrays.toString(usedString));
        }
        if (searchPackages != null) {
            builder.append(Arrays.toString(searchPackages));
        }
        if (excludePackages != null) {
            builder.append(Arrays.toString(excludePackages));
        }
        return builder.toString();
    }

}
