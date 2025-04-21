package utils;

import utils.N50Calculator;

import java.util.Arrays;
import java.util.List;

public class N50CalculatorTest {

    public static void main(String[] args) {
        N50CalculatorTest tester = new N50CalculatorTest();
        tester.testN50Calculation();
    }

    public void testN50Calculation() {
        List<Integer> readLengths1 = Arrays.asList(100, 200, 150, 300, 50, 75, 125);
        int n50_1 = N50Calculator.calculateN50(readLengths1);
        System.out.println("Test 1 - N50: " + n50_1);  // 150

        List<Integer> readLengths2 = Arrays.asList(50, 50, 50, 50, 50);
        int n50_2 = N50Calculator.calculateN50(readLengths2);
        System.out.println("Test 2 - N50: " + n50_2);  // 50

        List<Integer> readLengths3 = Arrays.asList(100, 200, 150, 300, 50, 75, 125, 400);
        int n50_3 = N50Calculator.calculateN50(readLengths3);
        System.out.println("Test 3 - N50: " + n50_3);  // 150

        List<Integer> readLengths4 = Arrays.asList(50, 50, 100, 100, 200, 200, 400, 400);
        int n50_4 = N50Calculator.calculateN50(readLengths4);
        System.out.println("Test 4 - N50: " + n50_4);  // 200

        List<Integer> readLengths5 = Arrays.asList(10, 20, 30, 40);
        int n50_5 = N50Calculator.calculateN50(readLengths5);
        System.out.println("Test 5 - N50: " + n50_5);  // 25
    }
}
