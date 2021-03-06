package com.ywl5320.wlmusic;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ywl5320.bean.TimeBean;
import com.ywl5320.libmusic.MyMusic;
import com.ywl5320.listener.OnCompleteListener;
import com.ywl5320.listener.OnErrorListener;
import com.ywl5320.listener.OnInfoListener;
import com.ywl5320.listener.OnLoadListener;
import com.ywl5320.listener.OnParparedListener;
import com.ywl5320.listener.OnPauseResumeListener;
import com.ywl5320.wlmusic.config.EventType;
import com.ywl5320.wlmusic.beans.EventBusBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by ywl on 2018/1/12.
 */

public class MusicService extends Service{


    private MyMusic myMusic;
    private String url;
    private EventBusBean timeEventBean;
    private EventBusBean errorEventBean;
    private EventBusBean loadEventBean;
    private EventBusBean completeEventBean;
    private EventBusBean pauseResumtEventBean;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        myMusic = new MyMusic();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(myMusic != null)
        {
            myMusic.stop();
        }
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        url = intent.getStringExtra("url");
        myMusic = new MyMusic();
        myMusic.setSource(url);

        myMusic.setOnParparedListener(new OnParparedListener() {
            @Override
            public void onParpared() {
                myMusic.start();
            }
        });

        myMusic.setOnInfoListener(new OnInfoListener() {
            @Override
            public void onInfo(TimeBean timeBean) {
                if(timeEventBean == null)
                {
                    timeEventBean = new EventBusBean(EventType.MUSIC_TIME_INFO, timeBean);
                }
                else
                {
                    timeEventBean.setObject(timeBean);
                    timeEventBean.setType(EventType.MUSIC_TIME_INFO);
                }
                EventBus.getDefault().post(timeEventBean);
            }
        });

        myMusic.setOnErrorListener(new OnErrorListener() {
            @Override
            public void onError(int code, String msg) {
                if(errorEventBean == null)
                {
                    errorEventBean = new EventBusBean(EventType.MUSIC_ERROR, msg);
                }
                else
                {
                    errorEventBean.setType(EventType.MUSIC_ERROR);
                    errorEventBean.setObject(msg);
                }
                EventBus.getDefault().post(errorEventBean);
                url = "";
            }
        });

        myMusic.setOnLoadListener(new OnLoadListener() {
            @Override
            public void onLoad(boolean load) {
                if(loadEventBean == null)
                {
                    loadEventBean = new EventBusBean(EventType.MUSIC_LOAD, load);
                }
                else
                {
                    loadEventBean.setType(EventType.MUSIC_LOAD);
                    loadEventBean.setObject(load);
                }
                EventBus.getDefault().post(loadEventBean);
            }
        });

        myMusic.setOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete() {
                if(completeEventBean == null)
                {
                    completeEventBean = new EventBusBean(EventType.MUSIC_COMPLETE, true);
                }
                else
                {
                    completeEventBean.setType(EventType.MUSIC_COMPLETE);
                    completeEventBean.setObject(true);
                }
                EventBus.getDefault().post(completeEventBean);
                url = "";
            }
        });

        myMusic.setOnPauseResumeListener(new OnPauseResumeListener() {
            @Override
            public void onPause(boolean pause) {
                if(pauseResumtEventBean == null)
                {
                    pauseResumtEventBean = new EventBusBean(EventType.MUSIC_PAUSE_RESUME_RESULT, pause);
                }
                else
                {
                    pauseResumtEventBean.setType(EventType.MUSIC_PAUSE_RESUME_RESULT);
                    pauseResumtEventBean.setObject(pause);
                }
                EventBus.getDefault().post(pauseResumtEventBean);
            }
        });

        myMusic.parpared();

        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventMsg(final EventBusBean messBean) {
        if(messBean.getType() == EventType.MUSIC_PAUSE_RESUME)
        {
            boolean pause = (boolean) messBean.getObject();
            if(pause)
            {
                myMusic.pause();
            }
            else
            {
                myMusic.resume();
            }
        }
        else if(messBean.getType() == EventType.MUSIC_NEXT)
        {
            if(myMusic != null)
            {
                String u = (String) messBean.getObject();
                if(!url.equals(u))
                {
                    url = u;
                    myMusic.playNext(url);
                }
            }
        }
        else if(messBean.getType() == EventType.MUSIC_SEEK_TIME)
        {
            if(myMusic != null)
            {
                int position = (int) messBean.getObject();
                myMusic.seek(position);
            }
        }
    }
}
