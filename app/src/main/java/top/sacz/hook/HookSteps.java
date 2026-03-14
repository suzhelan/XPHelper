package top.sacz.hook;

import android.content.Context;
import android.content.ContextWrapper;

import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle;

import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookSteps {

    /**
     * 获取初始的Hook方法
     *
     * @param loadPackageParam
     * @return
     */
    public Method getApplicationCreateMethod(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            String applicationName = loadPackageParam.appInfo.name;
            Class<?> clz = loadPackageParam.classLoader.loadClass(applicationName);
            try {
                return clz.getDeclaredMethod("attachBaseContext", Context.class);
            } catch (Throwable i) {
                try {
                    return clz.getDeclaredMethod("onCreate");
                } catch (Throwable e) {
                    try {
                        return clz.getSuperclass().getDeclaredMethod("attachBaseContext", Context.class);
                    } catch (Throwable m) {
                        return clz.getSuperclass().getDeclaredMethod("onCreate");
                    }
                }
            }
        } catch (Exception e) {
            try {
                return ContextWrapper.class.getDeclaredMethod("attachBaseContext", Context.class);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        }
    }

    private void initDialogX(Context context) {
        DialogX.init(context);
        DialogX.globalTheme = DialogX.THEME.AUTO;
        DialogX.globalStyle = new MaterialYouStyle();
    }

    /**
     * 开始测试
     * @param context
     */
    public void initHook(Context context) {
        initDialogX(context);
//        new DexkitTest().hook();
        new ActivityTest().hook();
    }
}
