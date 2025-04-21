import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import org.junit.Test;

import java.util.Map;

public class TestSubRangeMap {
    @Test
    public void test1() {
        // 创建一个 RangeMap 并添加一些条目
        RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
        rangeMap.put(Range.closed(1, 5), "A");
        rangeMap.put(Range.closed(6, 10), "B");
        rangeMap.put(Range.closed(11, 15), "C");

        // 定义要搜索的范围
        Range<Integer> searchRange = Range.closed(3, 8);

        // 使用 subRangeMap 获取与搜索范围重叠的子范围映射
        RangeMap<Integer, String> subRangeMap = rangeMap.subRangeMap(searchRange);

        // 打印子范围映射的内容
        for (Map.Entry<Range<Integer>, String> subRangeEntry : subRangeMap.asMapOfRanges().entrySet()) {
            String value = subRangeEntry.getValue();
            System.out.println("SubRange: " + subRangeEntry.getKey() + " -> Value: " + value);

        }
    }
}
