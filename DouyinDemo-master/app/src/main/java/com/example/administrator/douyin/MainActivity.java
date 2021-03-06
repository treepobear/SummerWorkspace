package com.example.administrator.douyin;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Controller.Constant;
import Controller.HttpUtil;
import adapter.DetailAdapter;
import adapter.DetailViewHolder;
import model.RefreshItemEvent;
import model.VideoCase;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import view.ShareDialog;


public class MainActivity extends AppCompatActivity implements DetailAdapter.RemoveItemListener {
    private static final String TAG = "douyin";

    private SmartRefreshLayout refreshView;
    private RecyclerView mRecyclerView;
    private DetailAdapter mAdapter;
    private ImageView imgPlay;
    private ImageView imgThumb;
    private FullWindowVideoView fullVideoView;
    private ProgressBar loadVideoProgressBar;
    MyLayoutManager myLayoutManager;

    private int refreshNum = 0;

    private boolean error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("jiuming","mainactivity"+Constant.currentUser.toString());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制竖屏
        if(EventBus.getDefault().isRegistered(MainActivity.this))
            EventBus.getDefault().register(MainActivity.this);
        ImageButton shootButton = (ImageButton) findViewById(R.id.shoot);
        shootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PlayVideoActivity.class);
                startActivity(intent);
            }
        });
        ImageButton searchButton = (ImageButton) findViewById(R.id.tosearch);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
        //个人信息界面
        TextView myInfoTextView = (TextView)findViewById(R.id.myInfo);
        myInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("jiuming","转个人信息页");
                Intent intent = new Intent(MainActivity.this, PersonInfo.class);
                startActivity(intent);
            }
        });
        //消息界面
        TextView Tomessage = (TextView)findViewById(R.id.message);
        Tomessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        getVideoData(1);//开启子线程获取数据
        initState();
        initView();

    }

    private void initState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @SuppressLint("WrongConstant")
    private void initView() {
        refreshView = findViewById(R.id.refresh);
        refreshView.setEnableScrollContentWhenLoaded(false);
        mRecyclerView = findViewById(R.id.recycler);
        myLayoutManager = new MyLayoutManager(this, OrientationHelper.VERTICAL, false);
        mRecyclerView.setLayoutManager(myLayoutManager);
        initListener();
    }

    //加载适配器
    private void loadAdapter(List<VideoCase> data){
        mAdapter = new DetailAdapter(this, data);
        mAdapter.setRemoveItemListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initListener() {
        myLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete() {

            }

            @Override
            public void onPageRelease(boolean isNext, int position) {
                Log.e(TAG, "释放位置:" + position + " 下一页:" + isNext);
                int index = 0;
                if (isNext) {
                    index = 0;
                } else {
                    index = 1;
                }
                releaseVideo(index);
            }

            @Override
            public void onPageSelected(int position, boolean bottom) {
                Log.e(TAG, "选择位置:" + position + " 下一页:" + bottom);

                playVideo(position);
            }
        });

        refreshView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                getVideoData(2);
            }
        });

        refreshView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                getVideoData(3);
            }
        });
    }

    private void releaseVideo(int index) {
        View itemView = mRecyclerView.getChildAt(index);
        final VideoView videoView = itemView.findViewById(R.id.video_view);
        //final ImageView imgThumb = itemView.findViewById(R.id.img_thumb);
        final ImageView imgPlay = itemView.findViewById(R.id.img_play);
        videoView.stopPlayback();
        imgPlay.animate().alpha(0f).start();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void playVideo(int position) {
        if (null != fullVideoView && fullVideoView.isPlaying()) return;
        DetailViewHolder holder=(DetailViewHolder) mRecyclerView.findViewHolderForLayoutPosition(position);
        if (null == holder || null == holder.itemView) return;
        fullVideoView = holder.getView(R.id.video_view);
        if (null == fullVideoView) return;
        fullVideoView.requestFocus();
        imgPlay=holder.getView(R.id.img_play);
        imgThumb=holder.getView(R.id.img_thumb);
        loadVideoProgressBar=holder.getView(R.id.load_video_progressBar);
        loadVideoProgressBar.setVisibility(View.VISIBLE);
        TextView commentView = holder.getView(R.id.comment_num);
        TextView shareView = holder.getView(R.id.share);

        commentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommentDialog commentDialog = new CommentDialog();
                //传递videoID和位置信息
                Bundle bundle=new Bundle();
                bundle.putString("videoID",holder.getVideoID());
                bundle.putInt("position", position);
                commentDialog.setArguments(bundle);
                commentDialog.show(getSupportFragmentManager(), "");
            }
        });

        shareView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareDialog shareDialog = new ShareDialog();
                shareDialog.show(getSupportFragmentManager(), holder.getVideoID());
            }
        });

        startVideoPlay();

    }

    private void startVideoPlay() {
        if (null == fullVideoView) return;

        fullVideoView.start();

        fullVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                mp.setLooping(true);
                imgThumb.animate().alpha(0).setDuration(200).start();
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    //如果没在播放，则手动播放
                    if (!fullVideoView.isPlaying()) {
                        mp.start();
                    }
                    loadVideoProgressBar.setVisibility(View.INVISIBLE);
                }
                else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    loadVideoProgressBar.setVisibility(View.VISIBLE);
                }

                return false;
            }
        });

        fullVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                if (null == arg0 || arg0.isPlaying()) return;
                if (!error) {
                    fullVideoView.seekTo(0);
                    fullVideoView.start();
                }
            }
        });

        fullVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                error = true;
                return false;
            }
        });

        imgPlay.setOnClickListener(new View.OnClickListener() {
            boolean isPlaying = true;
            @Override
            public void onClick(View v) {
                if (fullVideoView.isPlaying()) {
                    imgPlay.animate().alpha(0.7f).start();
                    fullVideoView.pause();
                    isPlaying = false;
                } else {
                    imgPlay.animate().alpha(0f).start();
                    fullVideoView.start();
                    isPlaying = true;
                }
            }
        });
    }

    //响应刷新Item的事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    private void onRefreshItemEvent(RefreshItemEvent refreshItemEvent){
        mAdapter.notifyItemChanged(refreshItemEvent.getPosition());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != fullVideoView) fullVideoView.pause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (null == fullVideoView || fullVideoView.isPlaying())
            return;
        fullVideoView.start();
        imgPlay.animate().alpha(0f).start();//fullVideoView不为null的话imgPlay也不为null
    }

    public void removeItem(final int position) {
        Constant.videoDatas.remove(position);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != fullVideoView)
            fullVideoView.stopPlayback();
        EventBus.getDefault().unregister(MainActivity.this);
    }

    private void getVideoData(int state){
        HttpUtil.getRecommendVideo(Constant.currentUser.getAccount(), refreshNum, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                refreshNum++;
                final String responseData = response.body().string();
                List<VideoCase>newAddVideo=new ArrayList<>();
                JSONObject resultJSON=JSONObject.parseObject(responseData);
                JSONArray videoJsonArray =resultJSON.getJSONArray("videos");
                for(int i=0;i<videoJsonArray.size();i++){
                    JSONObject videoJSON=videoJsonArray.getJSONObject(i);
                    VideoCase v=new VideoCase(videoJSON);
                    newAddVideo.add(v);
                }
                int oldVideoDataNum = Constant.videoDatas.size();
                int newAddVideoDataNum = newAddVideo.size();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (state){
                            case 1:
                                Constant.videoDatas.addAll(newAddVideo);
                                loadAdapter(Constant.videoDatas);
                                break;
                            case 2:
                                refreshView.finishRefresh();
                                if(newAddVideoDataNum>0){
                                    Constant.videoDatas.clear();
                                    Constant.videoDatas.addAll(newAddVideo);
                                    mAdapter.notifyDataSetChanged();
                                }
                                else {
                                    Toast.makeText(MainActivity.this,"没有更多视频了！",Toast.LENGTH_LONG).show();
                                }
                                break;
                            case 3:
                                refreshView.finishLoadMore();
                                if(newAddVideoDataNum>0){
                                    Constant.videoDatas.addAll(newAddVideo);
                                    mAdapter.notifyItemRangeInserted(oldVideoDataNum, newAddVideoDataNum);
                                }
                                else {
                                    Toast.makeText(MainActivity.this,"没有更多视频了！",Toast.LENGTH_LONG).show();
                                }
                                break;
                        }
                    }
                });
            }
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"连接失败！！！",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

}