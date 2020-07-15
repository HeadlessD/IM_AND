package com.qbao.newim.util;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by shiyunjie on 17/3/2.
 */
public class DataObserver {
    private static final String TAG = "DataObserver";

    private static ConcurrentLinkedQueue<IDataObserver> observer_link_queue = new ConcurrentLinkedQueue<>();

    public static void Register(IDataObserver data_observer) {
        if (data_observer == null) {
            Logger.error(TAG, "data_observer is null");
            return;
        }

        observer_link_queue.add(data_observer);
    }

    public static void Cancel(IDataObserver data_observer) {
        if (data_observer == null) {
            Logger.error(TAG, "data_observer is null");
            return;
        }

        observer_link_queue.remove(data_observer);
    }

    public static void Notify(int param1, Object param2, Object param3) {
        if (observer_link_queue.isEmpty()) {
            return;
        }

        Iterator iter = observer_link_queue.iterator();
        while (iter.hasNext()) {
            ((IDataObserver) iter.next()).OnChange(param1, param2, param3);
        }
    }
}
