package com.example.pc.lbs.module;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Author: Hadeslock
 * Created on 2022/5/9 14:02
 * Email: hadeslock@126.com
 * Desc: push、pop、max、min等操作均摊时间复杂度为O(1)的极值队列
 * reference:<a href="https://leetcode.cn/problems/dui-lie-de-zui-da-zhi-lcof/solution/mian-shi-ti-59-ii-dui-lie-de-zui-da-zhi-by-leetcod/">https://leetcode.cn/problems/dui-lie-de-zui-da-zhi-lcof/solution/mian-shi-ti-59-ii-dui-lie-de-zui-da-zhi-by-leetcod/</a>
 */

public class extremumQueue<T extends Comparable<T>> {
    Queue<T> q; //所有值
    Deque<T> maxD; //最大值队列
    Deque<T> minD; //最小值队列

    public extremumQueue() {
        q = new LinkedList<>();
        maxD = new LinkedList<>();
        minD = new LinkedList<>();
    }

    public int size() {
        return q.size();
    }

    /*
     * 从队列后面插入
     * @author hadeslock
     * @date 2022/5/9 14:18
     * @param val 要插入的值
     * @return void
     */
    public void pushLast(T val) {
        //处理最大值队列
        while (!maxD.isEmpty() && maxD.peekLast().compareTo(val) < 0) {
            maxD.pollLast();
        }
        //处理最小值队列
        while (!minD.isEmpty() && minD.peekLast().compareTo(val) > 0) {
            minD.pollLast();
        }
        //入队
        q.offer(val);
        maxD.offerLast(val);
        minD.offerLast(val);
    }

    /*
     * 从队头弹出
     * @author hadeslock
     * @date 2022/5/9 14:22
     * @return T 弹出的值，如果队列为空，返回null
     */
    public T popFirst() {
        if (q.isEmpty()) {
            return null;
        }
        T ans = q.poll();
        if (ans.equals(maxD.peekFirst())) {
            maxD.pollFirst();
        }
        if (ans.equals(minD.peekFirst())) {
            minD.pollFirst();
        }
        return ans;
    }

    public T max() {
        return maxD.peekFirst();
    }

    public T min() {
        return minD.peekFirst();
    }
}
