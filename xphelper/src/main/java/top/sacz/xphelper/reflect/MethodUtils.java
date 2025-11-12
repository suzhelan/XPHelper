package top.sacz.xphelper.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import top.sacz.xphelper.base.BaseFinder;
import top.sacz.xphelper.util.CheckClassType;
import top.sacz.xphelper.util.DexMethodDescriptor;

public class MethodUtils extends BaseFinder<Method> {

    private String methodName;
    private Class<?> returnType;
    private Class<?>[] methodParams;
    private Integer paramCount;
    private boolean matchParentClass = false;

    public static Method getMethodByDescriptor(String desc) throws NoSuchMethodException {
        Method method = new DexMethodDescriptor(desc).getMethodInstance(ClassUtils.getClassLoader());
        method.setAccessible(true);
        return method;
    }

    public static String getDescriptor(Method method) {
        return new DexMethodDescriptor(method).getDescriptor();
    }

    public static Method getMethodByDescriptor(String desc, ClassLoader classLoader) throws NoSuchMethodException {
        Method method = new DexMethodDescriptor(desc).getMethodInstance(classLoader);
        method.setAccessible(true);
        return method;
    }

    public MethodUtils matchParentClass(boolean matchParentClass) {
        this.matchParentClass = matchParentClass;
        return this;
    }

    public static MethodUtils create(Object target) {
        return create(target.getClass());
    }

    public static MethodUtils create(Class<?> fromClass) {
        MethodUtils methodUtils = new MethodUtils();
        methodUtils.setDeclaringClass(fromClass);
        return methodUtils;
    }

    public static MethodUtils create(String formClassName) {
        return create(ClassUtils.findClass(formClassName));
    }

    public MethodUtils returnType(Class<?> returnType) {
        this.returnType = returnType;
        return this;
    }

    public MethodUtils methodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public MethodUtils params(Class<?>... methodParams) {
        this.methodParams = methodParams;
        this.paramCount = methodParams.length;
        return this;
    }

    public MethodUtils paramCount(int paramCount) {
        this.paramCount = paramCount;
        return this;
    }

    @Override
    public BaseFinder<Method> find() {
        List<Method> cache = findMethodCache();
        if (cache != null && !cache.isEmpty()) {
            result = cache;
            return this;
        }
        Method[] methods = getDeclaringClass().getDeclaredMethods();
        result.addAll(Arrays.asList(methods));
        result.removeIf(method -> methodName != null && !method.getName().equals(methodName));
        result.removeIf(method -> returnType != null && !CheckClassType.checkType(method.getReturnType(), returnType, matchParentClass));
        result.removeIf(method -> paramCount != null && method.getParameterCount() != paramCount);
        result.removeIf(method -> methodParams != null && !paramEquals(method.getParameterTypes()));
        writeToMethodCache(result);
        return this;
    }

    private boolean paramEquals(Class<?>[] methodParams) {
        for (int i = 0; i < methodParams.length; i++) {
            Class<?> type = methodParams[i];
            Class<?> findType = this.methodParams[i];
            if (findType == Ignore.class || CheckClassType.checkType(type, findType, matchParentClass)) {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    public String buildSign() {
        String build = "method:" +
                fromClassName +
                " " +
                returnType +
                " " +
                methodName +
                "(" +
                paramCount +
                Arrays.toString(methodParams) +
                ")";
        return build;
    }

    private <T> T tryInvoke(Method method, Object object, Object... args) {
        try {
            return (T) method.invoke(object, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void invokeNoReturn(Object runTimeObj, Object... args) {
        try {
            first().invoke(runTimeObj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T invokeFirst(Object runTimeObj, Object... args) {
        Method method = first();
        return tryInvoke(method, runTimeObj, args);
    }

    public <T> T invokeLast(Object runTimeObj, Object... args) {
        Method method = last();
        return tryInvoke(method, runTimeObj, args);
    }

    public <T> T invokeFirstStatic(Object... args) {
        Method method = first();
        return tryInvoke(method, null, args);
    }

    @Deprecated(since = "请使用invokeFirstStatic")
    public <T> T callFirstStatic(Object... args) {
        Method method = first();
        return tryInvoke(method, null, args);
    }

    @Deprecated(since = "请使用invokeFirst")
    public <T> T callFirst(Object runTimeObj, Object... args) {
        Method method = first();
        return tryInvoke(method, runTimeObj, args);
    }

    @Deprecated(since = "请使用invokeLast")
    public <T> T callLast(Object runtimeObj, Object... args) {
        Method method = last();
        return tryInvoke(method, runtimeObj, args);
    }
}
