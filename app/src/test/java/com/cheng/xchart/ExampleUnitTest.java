package com.cheng.xchart;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private long value;

    @Test
    public void addition_isCorrect() throws Exception {
        sum();
    }

    private void sum() {
        long allSum = 0;
        for (int i = 0; i < 6; i++) {
            allSum += tempSum(value);
        }
        System.out.println(allSum);
    }

    private long tempSum(long v) {
        value = v * 10 + 2;
        return value;
    }

}