package top.sacz.hook.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import top.sacz.hook.R;
import top.sacz.xphelper.activity.BaseActivity;

public class ModuleActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);
    }
}
