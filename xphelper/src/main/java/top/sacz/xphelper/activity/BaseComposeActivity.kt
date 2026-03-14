package top.sacz.xphelper.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import top.sacz.xphelper.XpHelper
import top.sacz.xphelper.reflect.ClassUtils

abstract class BaseComposeActivity : ComponentActivity() {
    private val mLoader = BaseActivityClassLoader(BaseComposeActivity::class.java.classLoader!!)

    override fun getClassLoader(): ClassLoader {
        return mLoader
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val windowState = savedInstanceState.getBundle("android:viewHierarchyState")
        if (windowState != null) {
            windowState.classLoader = mLoader
        }
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XpHelper.injectResourcesToContext(this)
    }


    class BaseActivityClassLoader(private val mBaseReferencer: ClassLoader) : ClassLoader() {
        private val mHostReferencer: ClassLoader = ClassUtils.getClassLoader()

        @Throws(ClassNotFoundException::class)
        override fun loadClass(name: String, resolve: Boolean): Class<*>? {
            try {
                if (name.startsWith("androidx.compose") || name.startsWith("androidx.navigation") || name.startsWith(
                        "androidx.activity"
                    )
                ) {
                    return mBaseReferencer.loadClass(name)
                }
                return Context::class.java.classLoader!!.loadClass(name)
            } catch (_: ClassNotFoundException) {
            }
            try {
                //start: overloaded
                if (name == "androidx.lifecycle.LifecycleOwner" || name == "androidx.lifecycle.ReportFragment" || name == "androidx.lifecycle.ViewModelStoreOwner" || name == "androidx.savedstate.SavedStateRegistryOwner") {
                    return mHostReferencer.loadClass(name)
                }
            } catch (_: ClassNotFoundException) {
            }
            //with ClassNotFoundException
            return mBaseReferencer.loadClass(name)
        }
    }
}