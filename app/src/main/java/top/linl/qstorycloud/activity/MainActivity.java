package top.linl.qstorycloud.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import top.linl.qstorycloud.activity.base.BaseActivity;
import top.linl.qstorycloud.fragement.MainFragment;
import top.suzhelan.qstorycloud.R;

public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestTranslucentStatusBar();
        setContentView(R.layout.activity_main);
        titleBarAdaptsToStatusBar(findViewById(R.id.title_bar));
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = new MainFragment();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
