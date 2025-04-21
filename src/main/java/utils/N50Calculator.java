package utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class N50Calculator {

    public static int calculateN50(List<Integer> readLengths) {
        Calendar rightNow = Calendar.getInstance();
        System.out.println(rightNow.getTime()+"\tCalulating N50 length (list size: "+readLengths.size());
        // 对 read 长度列表进行排序（从长到短）
        Collections.sort(readLengths, Collections.reverseOrder());

        // 计算总长度
        long totalLength = 0;
        for (int length : readLengths) {
            totalLength += length;
        }

        // 初始化 N50
        int n50 = 0;
        long cumulativeLength = 0;

        for (int length : readLengths) {
            cumulativeLength += length;
            if (cumulativeLength >= totalLength / 2) {
                n50 = length;
                break;
            }
        }

        return n50;
    }


    public static void main(String[] args) {
        // 示例 read 长度列表
        List<Integer> readLengths = new ArrayList<>();
        readLengths.add(100);
        readLengths.add(200);
        readLengths.add(150);
        readLengths.add(1);
        readLengths.add(50);
        readLengths.add(75);
        readLengths.add(125);
        readLengths.add(125);
        readLengths.add(125);
        readLengths.add(125);
        readLengths.add(125);

        // 调用函数计算 N50
        int n50 = calculateN50(readLengths);
        System.out.println("N50: " + n50);
    }
}
