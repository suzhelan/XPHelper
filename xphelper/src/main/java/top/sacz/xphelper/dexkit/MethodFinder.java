package top.sacz.xphelper.dexkit;

import org.jetbrains.annotations.NotNull;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.MatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import org.luckypray.dexkit.result.MethodDataList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import top.sacz.xphelper.base.BaseDexQuery;
import top.sacz.xphelper.dexkit.cache.DexKitCache;
import top.sacz.xphelper.reflect.ClassUtils;

public class MethodFinder extends BaseDexQuery {

    private Class<?> declaredClass;//方法声明类
    private final List<Class<?>> parameters = new ArrayList<>();//方法的参数列表
    private String methodName;//方法名称
    private Class<?> returnType;//方法的返回值类型
    private final List<String> usedString = new ArrayList<>();//方法中使用的字符串列表
    private final List<Method> invokeMethods = new ArrayList<>();//方法中调用的方法列表
    private final List<Method> callMethods = new ArrayList<>();//调用了该方法的方法列表
    private final List<Long> usingNumbers = new ArrayList<>();//方法中使用的数字列表
    private int paramCount = -1;//参数数量
    private int modifiers = -1;//修饰符

    private MatchType matchType = MatchType.Contains;
    private final List<String> searchPackages = new ArrayList<>();
    private final List<String> excludePackages = new ArrayList<>();

    private final List<FieldFinder> usedFields = new ArrayList<>();

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
        methodFinder.parameters.addAll(Arrays.asList(method.getParameterTypes()));
        methodFinder.methodName = method.getName();
        methodFinder.returnType = method.getReturnType();
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
    public MethodFinder usedFields(FieldFinder... fieldFinders) {
        usedFields.addAll(Arrays.asList(fieldFinders));
        return this;
    }

    /**
     * 设置方法中调用的字段列表
     *
     * @param fields
     * @return
     */
    public MethodFinder usedFields(Field... fields) {
        for (Field field : fields) {
            usedFields.add(FieldFinder.from(field));
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
        this.parameters.addAll(Arrays.asList(parameters));
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
        this.invokeMethods.addAll(Arrays.asList(methods));
        return this;
    }

    /**
     * 设置调用了该方法的方法列表(也就是此方法被哪些方法调用)
     *
     * @param methods
     * @return
     */
    public MethodFinder callMethods(Method... methods) {
        this.callMethods.addAll(Arrays.asList(methods));
        return this;
    }

    /**
     * 设置方法中使用的数字列表
     *
     * @param numbers
     * @return
     */
    public MethodFinder usingNumbers(long... numbers) {
        for (long number : numbers) {
            this.usingNumbers.add(number);
        }
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
        return this;
    }

    /**
     * 设置方法中使用的字符串列表
     *
     * @param strings
     * @return
     */
    public MethodFinder usedString(String... strings) {
        this.usedString.addAll(Arrays.asList(strings));
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
        this.searchPackages.addAll(Arrays.asList(strings));
        return this;
    }

    /**
     * 设置排除的包名列表
     *
     * @param strings
     * @return
     */
    public MethodFinder excludePackages(String... strings) {
        this.excludePackages.addAll(Arrays.asList(strings));
        return this;
    }

    private FindMethod buildFindMethod() {
        FindMethod findMethod = FindMethod.create();
        if (!searchPackages.isEmpty()) {
            findMethod.searchPackages(searchPackages.toArray(new String[0]));
        }
        if (!excludePackages.isEmpty()) {
            findMethod.excludePackages(excludePackages.toArray(new String[0]));
        }
        return findMethod.matcher(buildMethodMatcher());
    }

    /**
     * 构造dexkit method方法的匹配条件
     *
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
        if (!usedString.isEmpty()) {
            methodMatcher.usingStrings(usedString.toArray(new String[0]));
        }

        if (!parameters.isEmpty()) {
            for (Class<?> parameterClass : parameters) {
                methodMatcher.addParamType(parameterClass);
            }
        }
        if (!usedFields.isEmpty()) {
            for (FieldFinder usedField : usedFields) {
                methodMatcher.addUsingField(usedField.buildFieldMatcher());
            }
        }
        if (!invokeMethods.isEmpty()) {
            for (Method invokeMethod : invokeMethods) {
                methodMatcher.addInvoke(MethodMatcher.create(invokeMethod));
            }
        }
        if (!callMethods.isEmpty()) {
            for (Method callMethod : callMethods) {
                methodMatcher.addCaller(MethodMatcher.create(callMethod));
            }
        }
        if (!usingNumbers.isEmpty()) {
            for (long usingNumber : usingNumbers) {
                methodMatcher.addUsingNumber(usingNumber);
            }
        }
        if (paramCount != -1) {
            methodMatcher.paramCount(paramCount);
        }
        if (modifiers != -1) {
            methodMatcher.modifiers(modifiers, matchType);
        }
        return methodMatcher;
    }

    /**
     * 查找方法 返回结果列表
     *
     * @return 返回列表
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
                if (methodData.isMethod()) {
                    Method method = methodData.getMethodInstance(ClassUtils.getClassLoader());
                    method.setAccessible(true);
                    methods.add(method);
                }
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


    /**
     * 查找构造方法
     *
     * @return 构造方法列表
     */
    public List<Constructor<?>> findConstructor() {
        try {
            List<Constructor<?>> cache = DexKitCache.getConstructorList(toString());
            if (cache != null) {
                return cache;
            }
            ArrayList<Constructor<?>> constructors = new ArrayList<>();
            MethodDataList methodDataList = DexFinder.getDexKitBridge().findMethod(buildFindMethod());
            if (methodDataList.isEmpty()) {
                DexKitCache.putConstructorList(toString(), constructors);
                return constructors;
            }
            for (MethodData methodData : methodDataList) {
                if (methodData.isConstructor()) {
                    Constructor<?> method = methodData.getConstructorInstance(ClassUtils.getClassLoader());
                    method.setAccessible(true);
                    constructors.add(method);
                }
            }
            //写入缓存
            DexKitCache.putConstructorList(toString(), constructors);
            return constructors;
        } catch (NoSuchMethodException e) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取构造方法 如果不存在则返回null
     *
     * @return 构造方法
     */
    public Constructor<?> firstConstructorOrNull() {
        List<Constructor<?>> constructors = findConstructor();
        if (constructors.isEmpty()) {
            return null;
        }
        return constructors.get(0);
    }

    /**
     * 获取构造方法 如果不存在则抛出异常
     *
     * @return 构造方法
     */
    public Constructor<?> firstConstructor() throws Exception {
        List<Constructor<?>> constructors = findConstructor();
        if (constructors.isEmpty()) {
            throw new NoSuchMethodException("No constructor found :" + this);
        }
        return constructors.get(0);
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("mf");
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
        if (!parameters.isEmpty()) {
            builder.append((parameters));
        }
        if (!invokeMethods.isEmpty()) {
            builder.append((invokeMethods));
        }
        if (!callMethods.isEmpty()) {
            builder.append((callMethods));
        }
        if (!usedFields.isEmpty()) {
            builder.append((usedFields));
        }
        if (!usingNumbers.isEmpty()) {
            builder.append((usingNumbers));
        }
        if (paramCount != -1) {
            builder.append(paramCount);
        }
        if (modifiers != -1) {
            builder.append(modifiers);
        }
        if (!usedString.isEmpty()) {
            builder.append((usedString));
        }
        if (!searchPackages.isEmpty()) {
            builder.append((searchPackages));
        }
        if (!excludePackages.isEmpty()) {
            builder.append((excludePackages));
        }
        return builder.toString();
    }

}
