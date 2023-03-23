package com.yebuxiu.helper;

import org.springframework.util.CollectionUtils;

import java.util.ArrayDeque;
import java.util.Deque;


/**
 * redis动态切换数据库帮助器
 */
public class RedisDatabaseThreadLocalHelper {

    private static final ThreadLocal<Deque<Integer>> THREAD_DB = new ThreadLocal<>();

    /**
     * 更改当前线程 RedisTemplate db
     *
     * @param db set current redis db
     */
    public static void set(int db) {
        Deque<Integer> deque = THREAD_DB.get();
        if (deque == null) {
            deque = new ArrayDeque<>();
        }
        deque.addFirst(db);
        THREAD_DB.set(deque);
    }

    /**
     * @return get current redis db
     */
    public static Integer get() {
        Deque<Integer> deque = THREAD_DB.get();
        if (CollectionUtils.isEmpty(deque)) {
            return null;
        }
        return deque.getFirst();
    }

    /**
     * 清理
     */
    public static void clear() {
        Deque<Integer> deque = THREAD_DB.get();
        if (deque == null || deque.size() <= 1) {
            THREAD_DB.remove();
            return;
        }
        deque.removeFirst();
        THREAD_DB.set(deque);
    }
}
