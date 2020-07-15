package com.example.administrator.douyin;
import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import fragment.TabFragment;
import widget.FullViewPager;
import widget.ScaleScrollView;
import widget.TitleLayout;


public class PersonInfo extends AppCompatActivity implements ScaleScrollView.OnScrollChangeListener {

    private TabLayout tab1, tab2;
    private TitleLayout titleLayout;
    private int colorPrimary;
    private ArgbEvaluator evaluator;
    private View statusView;
    private Button editinfo;
    private TextView sy;
    private int getStatusBarHeight() {
        int height = 0;
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            height = getResources().getDimensionPixelSize(resId);
        }
        return height;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personinfo);
        colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);

        sy = findViewById(R.id.sy);
        sy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asdasd();
            }
        });

        editinfo = findViewById(R.id.editinfo);
        editinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditInfo();
            }
        });
        initView();
    }

    private void EditInfo() {
        Intent intent = new Intent(this, EditMyinfo.class);
        startActivity(intent);
    }
    public void asdasd() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initView() {
        //设置状态栏和导航栏
        statusView = findViewById(R.id.statusView);
        LinearLayoutCompat.LayoutParams lp = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, getStatusBarHeight());
        statusView.setLayoutParams(lp);
        statusView.setBackgroundColor(Color.TRANSPARENT);
        statusView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setNavigationBarColor(colorPrimary);

        AppCompatImageView banner = findViewById(R.id.banner);
        ScaleScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.setTargetView(banner);
        scrollView.setOnScrollChangeListener(this);
        tab1 = findViewById(R.id.tab1);
        tab2 = findViewById(R.id.tab2);
        titleLayout = findViewById(R.id.title_layout);
        FullViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new adapter.TabAdapter(this, getSupportFragmentManager(), getTabs()));
        tab1.setupWithViewPager(viewPager);
        tab2.setupWithViewPager(viewPager);
    }

    private List<model.TabItemModel> getTabs() {
        List<model.TabItemModel> tabs = new ArrayList<>();
        tabs.add(new model.TabItemModel("作品30", TabFragment.class.getName(), null));
        tabs.add(new model.TabItemModel("动态30", TabFragment.class.getName(), null));
        tabs.add(new model.TabItemModel("喜欢30", TabFragment.class.getName(), null));
        return tabs;
    }

    @Override
    public void onScrollChange(NestedScrollView v, int x, int y, int ox, int oy) {
        if (null != tab1 && null != tab2 && null != titleLayout && null != statusView) {
            int distance = tab1.getTop() - titleLayout.getHeight() - statusView.getHeight();
            float ratio = v.getScaleY() * 1f / distance;
            if (distance <= v.getScrollY()) {
                ratio = 1;
                if (tab2.getVisibility() != View.VISIBLE) {
                    tab2.setVisibility(View.VISIBLE);
                    statusView.setBackgroundColor(colorPrimary);
                }
            } else {
                if (tab2.getVisibility() == View.VISIBLE) {
                    tab2.setVisibility(View.INVISIBLE);
                    statusView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
            if (null == evaluator) {
                evaluator = new ArgbEvaluator();
            }
            titleLayout.setBackgroundColor((int) evaluator.evaluate(ratio, Color.TRANSPARENT, colorPrimary));
            titleLayout.setTitleColor((int) evaluator.evaluate(ratio, Color.TRANSPARENT, Color.WHITE));
        }
    }
}