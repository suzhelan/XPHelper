package top.sacz.hook.ext

import com.kongzue.dialogx.dialogs.PopTip

fun String.showToast() {
    PopTip.show(this)
}