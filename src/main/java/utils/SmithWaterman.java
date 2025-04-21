package utils;

import utils.entity.ComparatorPair;
import utils.entity.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SmithWaterman {
    private static int matchScore = 1;
    private static int mismatchScore = -1;
    private static int gapPenalty = -1;

    public static int getMatchScore() {
        return matchScore;
    }

    public static void setMatchScore(int matchScore) {
        SmithWaterman.matchScore = matchScore;
    }

    public static int getMismatchScore() {
        return mismatchScore;
    }

    public static void setMismatchScore(int mismatchScore) {
        SmithWaterman.mismatchScore = mismatchScore;
    }

    public static int getGapPenalty() {
        return gapPenalty;
    }

    public static void setGapPenalty(int gapPenalty) {
        SmithWaterman.gapPenalty = gapPenalty;
    }

    public static void main(String[] args) {
        String seq1 = "ACGTACGTA";
        String seq2 = "CGTATTGTA";

        int[][] scoreMatrix = computeScoreMatrix(seq1, seq2, matchScore, mismatchScore, gapPenalty);
        printScoreMatrix(scoreMatrix);

        int[] maxIndices = findMaxScoreIndices(scoreMatrix);
        int maxScore = scoreMatrix[maxIndices[0]][maxIndices[1]];

        String[] alignedSeqs = traceback(seq1, seq2, scoreMatrix, maxIndices);
        String alignedSeq1 = alignedSeqs[0];
        String alignedSeq2 = alignedSeqs[1];

        System.out.println("Alignment score: " + maxScore);
        System.out.println("Aligned sequences:");
        System.out.println(alignedSeq1);
        System.out.println(alignedSeq2);
    }

    public static int[][] computeScoreMatrix(String seq1, String seq2, int matchScore, int mismatchScore, int gapPenalty) {
        int[][] scoreMatrix = new int[seq1.length() + 1][seq2.length() + 1];

        // Initialize first row and column of score matrix to 0
        for (int i = 0; i <= seq1.length(); i++) {
            scoreMatrix[i][0] = 0;
        }
        for (int j = 0; j <= seq2.length(); j++) {
            scoreMatrix[0][j] = 0;
        }

        // Fill in the rest of the score matrix
        for (int i = 1; i <= seq1.length(); i++) {
            for (int j = 1; j <= seq2.length(); j++) {
                int match = scoreMatrix[i - 1][j - 1] + (seq1.charAt(i - 1) == seq2.charAt(j - 1) ? matchScore : mismatchScore);
                int gap1 = scoreMatrix[i - 1][j] + gapPenalty;
                int gap2 = scoreMatrix[i][j - 1] + gapPenalty;
                scoreMatrix[i][j] = Math.max(0, Math.max(match, Math.max(gap1, gap2)));
            }
        }

        return scoreMatrix;
    }

    public static int[][] computeScoreMatrix(String seq1, String seq2) {
        int[][] scoreMatrix = null;
        try {
            scoreMatrix = new int[seq1.length() + 1][seq2.length() + 1];

            // Initialize first row and column of score matrix to 0
            for (int i = 0; i <= seq1.length(); i++) {
                scoreMatrix[i][0] = 0;
            }
            for (int j = 0; j <= seq2.length(); j++) {
                scoreMatrix[0][j] = 0;
            }

            // Fill in the rest of the score matrix
            for (int i = 1; i <= seq1.length(); i++) {
                for (int j = 1; j <= seq2.length(); j++) {
                    int match = scoreMatrix[i - 1][j - 1] + (seq1.charAt(i - 1) == seq2.charAt(j - 1) ? matchScore : mismatchScore);
                    int gap1 = scoreMatrix[i - 1][j] + gapPenalty;
                    int gap2 = scoreMatrix[i][j - 1] + gapPenalty;
                    scoreMatrix[i][j] = Math.max(0, Math.max(match, Math.max(gap1, gap2)));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("seq1 = " + seq1);
            System.out.println("seq2 = " + seq2);
        }


        return scoreMatrix;
    }

    public static int[] findMaxScoreIndices(int[][] scoreMatrix) {
        int maxScore = Integer.MIN_VALUE;
        int[] maxIndices = new int[2];

        for (int i = 0; i < scoreMatrix.length; i++) {
            for (int j = 0; j < scoreMatrix[0].length; j++) {
                if (scoreMatrix[i][j] > maxScore) {
                    maxScore = scoreMatrix[i][j];
                    maxIndices[0] = i;
                    maxIndices[1] = j;
                }
            }
        }

        return maxIndices;
    }

    public static int[] findMaxScoreIndicesLimitRow(int[][] scoreMatrix, int startRow, int endRow) {
        int maxScore = Integer.MIN_VALUE;
        int[] maxIndices = new int[2];

        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < scoreMatrix[0].length; j++) {
                if (scoreMatrix[i][j] > maxScore) {
                    maxScore = scoreMatrix[i][j];
                    maxIndices[0] = i;
                    maxIndices[1] = j;
                }
            }
        }

        return maxIndices;
    }

    public static int[] findMaxScoreIndices(int[][] scoreMatrix, int start, int end) {
        int maxScore = Integer.MIN_VALUE;
        int[] maxIndices = new int[2];

        for (int i = start; i < scoreMatrix.length; i++) {
            for (int j = end; j < scoreMatrix[0].length; j++) {
                if (scoreMatrix[i][j] > maxScore) {
                    maxScore = scoreMatrix[i][j];
                    maxIndices[0] = i;
                    maxIndices[1] = j;
                }
            }
        }

        return maxIndices;
    }

    public static int[][] arraySlice(int[][] arr, int rowStart, int rowEnd, int colStart, int colEnd) {
        int numRows = rowEnd - rowStart + 1;
        int numCols = colEnd - colStart + 1;
        int[][] slice = new int[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                slice[i][j] = arr[rowStart + i][colStart + j];
            }
        }

        return slice;
    }


    //非拷贝方法
    public static int[][] arraySliceByRow(int[][] arr, int rowStart, int rowEnd) {
        int numRows = rowEnd - rowStart;
        int[][] slice = new int[numRows][];

        for (int i = 0; i < numRows; i++) {
                slice[i] = arr[rowStart + i];
        }

        return slice;
    }

    public static List<Pair> findAllLinker(String seq1, String seq2, int[][] scoreMatrix, int cuttoff){
        List<Pair> allStartEnd = new ArrayList<>();
        findAllStartEnd(
                allStartEnd,
                seq1,
                seq2,
                scoreMatrix,
                0,
                scoreMatrix.length,
                cuttoff
        );
        findAllStartEnd(
                allStartEnd,
                seq1,
                MyUtil.reverseComplement(seq2.toUpperCase()),
                scoreMatrix,
                0,
                scoreMatrix.length,
                cuttoff
        );
        if (allStartEnd.isEmpty()){
            return allStartEnd;
        }

        allStartEnd = filterOverlappedLinker(allStartEnd);

        return allStartEnd;
    }

    public static boolean isOverlap(Pair p1, Pair p2){
        return p1.getEnd() >= p2.getStart() && p2.getEnd() >= p1.getStart();
    }

    public static List<Pair> filterOverlappedLinker(List<Pair> allStartEnd){
        List<Pair> allStartEndFiltered = new ArrayList<>();
        ComparatorPair comparatorPair = new ComparatorPair();
        allStartEnd.sort(comparatorPair);
        Iterator<Pair> pairIterator = allStartEnd.iterator();
        Pair pair1 = null;
        Pair pair2 = null;
        if (pairIterator.hasNext()) {
            pair1 = pairIterator.next();
        } else {
            return allStartEndFiltered;
        }
        while (pairIterator.hasNext()){
            pair2 = pairIterator.next();
            if (isOverlap(pair1, pair2)){
                //当发生重叠，丢掉分低的，让分高的成为pair1，继续比较
                if (pair1.getScore() < pair2.getScore()){
                    pair1=pair2;
                }
            }else {
                allStartEndFiltered.add(pair1);
                pair1=pair2;
            }
        }
        //不要忘记最后的pair1
        allStartEndFiltered.add(pair1);
        return allStartEndFiltered;
    }

    public static void findAllStartEnd(
            List<Pair> allStartEnd,
            String seq1,
            String seq2,
            int[][] scoreMatrix,
            int startRow,
            int endRow,
            int cuttoff)
    {
        int flag = 0;
        //如果矩阵少于两行,结束
        if (scoreMatrix.length <= 1 ||
            endRow - startRow <= 0
        ){
            return ;
        }
        int[] indices1 = findMaxScoreIndicesLimitRow(scoreMatrix, startRow, endRow);
        //如果找到的最大值小于列表,结束
        if (scoreMatrix[indices1[0]][indices1[1]] <= cuttoff ||
            indices1[0] <= 0 ||
            indices1[1] <=0
        ){
            return ;
        }
        //将linker位于read的起止位置写入列表
        int[] indices2 = tracebackMy(seq1, seq2, scoreMatrix, indices1);
        Pair pair = new Pair(indices2[0], indices1[0]);
        pair.setEndLinker(indices1[1]);
        pair.setScore(scoreMatrix
                [indices1[0]]
                [indices1[1]]
        );
        allStartEnd.add(pair);

//        System.out.println("{"+startRow+","+endRow+"}");
//        System.out.println("{"+indices1[0]+","+indices1[1]+"}");
//        System.out.println("{"+indices2[0]+","+indices2[1]+"}");
        findAllStartEnd(
                allStartEnd,
                seq1,seq2,
                scoreMatrix,
                startRow,
                indices2[0] + 1,
                cuttoff
        );
        findAllStartEnd(
                allStartEnd,
                seq1,seq2,
                scoreMatrix,
                indices1[0] + 1,
                endRow,
                cuttoff
        );
        return ;
    }

    public static String[] traceback(String seq1, String seq2, int[][] scoreMatrix, int[] maxIndices) {
        int i = maxIndices[0];
        int j = maxIndices[1];
        StringBuilder alignedSeq1 = new StringBuilder();
        StringBuilder alignedSeq2 = new StringBuilder();

        while (scoreMatrix[i][j] != 0) {
            if (scoreMatrix[i][j] == scoreMatrix[i - 1][j - 1] + (seq1.charAt(i - 1) == seq2.charAt(j - 1) ? matchScore : mismatchScore)) {
                alignedSeq1.insert(0, seq1.charAt(i - 1));
                alignedSeq2.insert(0, seq2.charAt(j - 1));
                i--;
                j--;
            } else if (scoreMatrix[i][j] == scoreMatrix[i - 1][j] + gapPenalty) {
                alignedSeq1.insert(0, seq1.charAt(i - 1));
                alignedSeq2.insert(0, "-");
                i--;
            } else {
                alignedSeq1.insert(0, "-");
                alignedSeq2.insert(0, seq2.charAt(j - 1));
                j--;
            }
        }

        return new String[] {alignedSeq1.toString(), alignedSeq2.toString()};
    }

    public static int[] tracebackMy(String seq1, String seq2, int[][] scoreMatrix, int[] maxIndices) {
        int i = maxIndices[0];
        int j = maxIndices[1];
//        StringBuilder alignedSeq1 = new StringBuilder();
//        StringBuilder alignedSeq2 = new StringBuilder();

        try {
            while (scoreMatrix[i][j] != 0) {
                if (scoreMatrix[i][j] == scoreMatrix[i - 1][j - 1] + (seq1.charAt(i - 1) == seq2.charAt(j - 1) ? matchScore : mismatchScore)) {
//                    alignedSeq1.insert(0, seq1.charAt(i - 1));
//                    alignedSeq2.insert(0, seq2.charAt(j - 1));
                    i--;
                    j--;
                } else if (scoreMatrix[i][j] == scoreMatrix[i - 1][j] + gapPenalty) {
//                    alignedSeq1.insert(0, seq1.charAt(i - 1));
//                    alignedSeq2.insert(0, "-");
                    i--;
                } else {
//                    alignedSeq1.insert(0, "-");
//                    alignedSeq2.insert(0, seq2.charAt(j - 1));
                    j--;
                }
            }
        }catch (Exception exception){
            exception.printStackTrace();
            System.out.println(seq1);
            System.out.println("seq1 = " + seq1.length());
            System.out.println("seq2 = " + seq2.length());
            System.out.println("seq1:"+i+"-"+maxIndices[0]);
            System.out.println("seq2:"+j+"-"+maxIndices[1]);
            System.out.println("score:"+scoreMatrix[maxIndices[0]][maxIndices[1]]);

            System.exit(1);
        }
//        System.out.println("seq1:"+i+"-"+maxIndices[0]);
//        System.out.println("seq2:"+j+"-"+maxIndices[1]);
//        System.out.println("score:"+scoreMatrix[maxIndices[0]][maxIndices[1]]);
//        System.out.println(alignedSeq1);
//        System.out.println(alignedSeq2);

        return new int[]{i,j};
    }

    public static void printScoreMatrix(int[][] scoreMatrix) {
        for (int i = 0; i < scoreMatrix.length; i++) {
            for (int j = 0; j < scoreMatrix[0].length; j++) {
                System.out.print(scoreMatrix[i][j] + "\t");
            }
            System.out.println();
        }
    }

}