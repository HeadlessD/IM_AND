package com.qbao.newim.helper;

/**
 * Created by chenjian on 2017/3/27.
 */

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 延迟删除map
 *
 * @author GalaxyBruce
 * @since qianbao1.1
 */
public class DelayRemoveMap<K, V>
{
    private Map<K, V> datas = new ConcurrentHashMap<K, V>();
    private DelayRemoveThread mDelayRemoveThread;

    public DelayRemoveMap()
    {
    }

    public V remove(K key)
    {
        V v = datas.remove(key);
        return v;
    }

    public V put(K key, V value)
    {
        return datas.put(key, value);
    }

    public V get(K key)
    {
        return datas.get(key);
    }

    public boolean containsKey(K key)
    {
        return datas.containsKey(key);
    }

    public void clear()
    {
        datas.clear();
    }

    public Map<K, V> getDatas()
    {
        return datas;
    }

    public void requestRemove(K key)
    {
        ensurePlayThread();
        mDelayRemoveThread.requestRemove(key);
    }

    private void ensurePlayThread()
    {
        if (mDelayRemoveThread == null)
        {
            mDelayRemoveThread = new DelayRemoveThread();
            mDelayRemoveThread.start();
        }
    }

    private class DelayRemoveThread extends HandlerThread implements Handler.Callback
    {

        private final static int DELAY_REMOVE = 999;

        private Handler mPlayThreadHandler;

        public DelayRemoveThread()
        {
            super("DELAY_REMOVE_MAP_THREAD");
        }

        public void ensureHandler()
        {
            if (mPlayThreadHandler == null)
            {
                mPlayThreadHandler = new Handler(getLooper(), this);
            }
        }

        private void requestRemove(K key)
        {
            ensureHandler();
            mPlayThreadHandler.sendMessageDelayed(mPlayThreadHandler.obtainMessage(DELAY_REMOVE, key), 3000);
        }

        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case DELAY_REMOVE:
                    try
                    {
                        remove((K)msg.obj);
                    }
                    catch(ClassCastException e)
                    {
                    }
                    break;
            }
            return true;
        }

    }
}