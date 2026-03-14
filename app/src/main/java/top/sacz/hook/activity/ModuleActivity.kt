package top.sacz.hook.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import top.sacz.hook.ui.theme.QStoryTheme
import top.sacz.xphelper.activity.BaseComposeActivity

class ModuleActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QStoryTheme {
                Scaffold { contentPadding ->
                    Column(modifier = Modifier.padding(contentPadding)) {
                        Text("Hello World!")
                    }
                }
            }
        }
    }
}
