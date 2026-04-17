package top.sacz.hook.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.sacz.hook.entity.ConfigTestCategoryResult
import top.sacz.hook.entity.ConfigTestCategoryType
import top.sacz.hook.entity.ConfigTestTaskResult
import top.sacz.hook.ui.theme.QStoryTheme
import top.sacz.hook.viewmodel.TestViewModel

class TestActivity : ComponentActivity() {
    private val viewModel: TestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QStoryTheme {
                TestScreen(viewModel = viewModel, appContext = applicationContext)
            }
        }
    }
}

@Composable
private fun TestScreen(
    viewModel: TestViewModel,
    appContext: Context,
    modifier: Modifier = Modifier,
) {
    val uiState = viewModel.uiState

    Scaffold(modifier = modifier) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
        ) {
            HeaderSection(
                isRunning = uiState.isRunning,
                runVersion = uiState.runVersion,
                totalTasks = uiState.totalTasks,
                passedTasks = uiState.passedTasks,
                failedTasks = uiState.failedTasks,
                elapsedTotal = uiState.elapsedTotal,
                onStart = { viewModel.startTest(appContext) }
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (uiState.runVersion == 0) EmptyState()
                uiState.categories.forEach { category ->
                    CategorySection(category)
                }
                if (uiState.isRunning || uiState.categories.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    isRunning: Boolean,
    runVersion: Int,
    totalTasks: Int,
    passedTasks: Int,
    failedTasks: Int,
    elapsedTotal: Long,
    onStart: () -> Unit,
) {
    Surface(shadowElevation = 2.dp, tonalElevation = 1.dp) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 12.dp
            )
        ) {
            Text(
                "ConfigUtils 功能验证",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (totalTasks > 0) {
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                                append("$passedTasks")
                            }
                            withStyle(
                                SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            ) {
                                append("/$totalTasks 通过")
                            }
                        },
                        color = if (failedTasks == 0) SuccessGreen else FailureRed
                    )
                    if (elapsedTotal > 0) {
                        Text(
                            "${elapsedTotal}ms",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                if (isRunning) {
                    Text(
                        "执行中...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    OutlinedButton(
                        onClick = onStart,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (runVersion == 0) "开始测试" else "重新测试", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier
        .fillMaxWidth()
        .padding(top = 80.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🧪", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "点击「开始测试」验证 ConfigUtils 全部功能",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun CategorySection(category: ConfigTestCategoryResult) {
    val color = categoryColor(category.type)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.10f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(color)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    category.type.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = color
                )
                Spacer(Modifier.weight(1f))
                val count = category.tasks.count { it.passed }
                Text(
                    "$count/${category.tasks.size}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        category.tasks.forEach { task ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 4 }
            ) {
                TaskCard(task)
            }
        }
    }
}

@Composable
private fun TaskCard(task: ConfigTestTaskResult) {
    val borderColor = if (task.passed) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    } else {
        FailureRed.copy(alpha = 0.35f)
    }
    val bgColor = if (task.passed) {
        MaterialTheme.colorScheme.surface
    } else {
        FailureRed.copy(alpha = 0.05f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (task.passed) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (task.passed) SuccessGreen else FailureRed,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        task.name,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${task.elapsedMs}ms",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = WriteColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    ) { append("W ") }
                    withStyle(
                        SpanStyle(
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    ) { append(task.writeValue) }
                })
                Text(buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = ReadColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    ) { append("R ") }
                    withStyle(
                        SpanStyle(
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    ) { append(task.readValue) }
                })
                if (task.error != null) {
                    Text(
                        task.error,
                        color = FailureRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun categoryColor(type: ConfigTestCategoryType): Color {
    return when (type) {
        ConfigTestCategoryType.SAFETY -> CateBlue
        ConfigTestCategoryType.BASIC_TYPES -> CateGreen
        ConfigTestCategoryType.DEFAULTS -> CateOrange
        ConfigTestCategoryType.COLLECTIONS -> CatePurple
        ConfigTestCategoryType.LIFECYCLE -> CateTeal
        ConfigTestCategoryType.ENCRYPTION -> CatePink
    }
}

private val SuccessGreen = Color(0xFF1F6F43)
private val FailureRed = Color(0xFFB42318)
private val WriteColor = Color(0xFF1565C0)
private val ReadColor = Color(0xFF6A1B9A)

private val CateBlue = Color(0xFF1565C0)
private val CateGreen = Color(0xFF2E7D32)
private val CateOrange = Color(0xFFE65100)
private val CatePurple = Color(0xFF6A1B9A)
private val CateTeal = Color(0xFF00695C)
private val CatePink = Color(0xFFAD1457)
