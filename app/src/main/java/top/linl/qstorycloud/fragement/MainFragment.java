package top.linl.qstorycloud.fragement;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import top.linl.qstorycloud.db.helper.CommonDBHelper;
import top.linl.qstorycloud.log.QSLog;
import top.linl.qstorycloud.util.TaskManager;
import top.suzhelan.entity.QSResult;
import top.suzhelan.entity.UpdateInfo;
import top.suzhelan.qstorycloud.BuildConfig;
import top.suzhelan.qstorycloud.R;


public class MainFragment extends Fragment {
    public static final String SAFE_MODE = "safe_mode";
    public static final String CLEAN_DATA = "clean_data";
    private TextView tvAppLabel;
    private CardView cardViewGithub;
    private CardView cardTutorial;
    private CheckBox cbSafeMode;

    private CommonDBHelper dbHelper;
    private CheckBox cbCleanData;
    private CardView cardAddTelegramChannel;
    private TextView tvBuildVersion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        initDB();
        initView(rootView);
        initUI();
        initListener();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initUI();
    }

    private void initDB() {
        dbHelper = CommonDBHelper.getInstance(getContext());
        String safeMode = dbHelper.query(SAFE_MODE);
        if (safeMode == null) {
            dbHelper.insert(SAFE_MODE, String.valueOf(false));
        }

    }

    private void initView(View root) {
        tvAppLabel = root.findViewById(R.id.tv_app_label);
        cardViewGithub = root.findViewById(R.id.card_view_github);
        cardTutorial = root.findViewById(R.id.card_tutorial);
        cbSafeMode = root.findViewById(R.id.cb_safe_mode);
        cbCleanData = root.findViewById(R.id.cb_clean_data);
        cardAddTelegramChannel = root.findViewById(R.id.card_add_telegram_channel);
        tvBuildVersion = root.findViewById(R.id.tv_build_version);
    }

    private void initUI() {
        String buildVersion = String.format(getString(R.string.build_version), BuildConfig.VERSION_NAME, "正在获取");
        tvBuildVersion.setText(buildVersion);
        requestLatestVersionName();
        cbSafeMode.setChecked(Boolean.parseBoolean(dbHelper.query(SAFE_MODE)));
        String cleanData = dbHelper.query(CLEAN_DATA);
        cbCleanData.setChecked(cleanData != null);
    }

    /**
     * 请求和刷新版本信息
     */
    private void requestLatestVersionName() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        FormBody formBody = new FormBody.Builder()
                .add("version", "0")
                .build();
        Request request = new Request
                .Builder()
                .url("https://qstory.sacz.top/update/getUpdateLog")
                .post(formBody)
                .addHeader("User-Agent", "Android")
                .addHeader("Content-Type", "text/plain")
                .addHeader("Accept", "*/*")
                .addHeader("Connection", "keep-alive")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                QSLog.e("MainFragment", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                TaskManager.addTask(() -> {
                            try {
                                TypeReference<QSResult<List<UpdateInfo>>> typeReference = new TypeReference<>() {
                                };
                                UpdateInfo updateInfo = JSON.parseObject(response.body().string(), typeReference).getData().get(0);
                                Context context = getContext();
                                if (context == null) return;
                                String buildVersionFormat = context.getString(R.string.build_version);
                                tvBuildVersion.setText(String.format(buildVersionFormat, BuildConfig.VERSION_NAME, updateInfo.getVersionName()));
                            } catch (IOException e) {
                                QSLog.e("MainFragment", e);
                            }
                        }
                );

            }
        });
    }

    private void initListener() {
        cardViewGithub.setOnClickListener(v -> {
            //跳转到浏览器打开
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse("https://github.com/Suzhelan/QStoryCloud"));
            startActivity(intent);
        });
        cardAddTelegramChannel.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse("https://t.me/WhenFlowersAreInBloom"));
            startActivity(intent);
        });
        cbSafeMode.setOnCheckedChangeListener((buttonView, isChecked) -> dbHelper.update(SAFE_MODE, String.valueOf(isChecked)));
        cbCleanData.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                //插入清空数据的标记
                dbHelper.insert(CLEAN_DATA, "true");
            } else {
                dbHelper.delete(CLEAN_DATA);
            }
        });
    }
}
