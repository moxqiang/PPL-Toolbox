package utils;

import process.contact.ReadAT;

import java.util.Arrays;
import java.util.List;

public class Judgement {
    List<String> list = Arrays.asList(
            "passed",
            "singleton",
            "singleton1",
            "singleton2",
            "null"
    );

    List<String> listC = Arrays.asList(
        "passed",
        "singleton",
        "singleton1",
        "singleton2",
        "null"
    );

    List<String> listCC = Arrays.asList(
            "passed",
            "null"
    );

    List<String> listUnmapped = Arrays.asList(
            "unmapped"
    );

    List<String> listRealFrags = Arrays.asList(
            "passed",
            "singleton",
            "singleton1",
            "singleton2",
            "not_confident",
            "adjacent_contacts",
            "close_contacts",
            "isolated_contacts",
            "null"
    );

    List<String> listConfidentFrags = Arrays.asList(
            "passed",
            "unmapped",
            "singleton",
            "singleton1",
            "singleton2",
            "adjacent_contacts",
            "close_contacts",
            "isolated_contacts",
            "null"
    );

//    List<String> listChr = Arrays.asList(
//            "chr1"
//    );

    public boolean judge(ReadAT at){
        if ( listC.contains(at.status) )
            return true;
        else
            return false;
    }

    public boolean judge(String[] fields){
        if ( listC.contains(fields[10]) )
            return true;
        else
            return false;
    }

    public boolean judge(String status){
        if ( listCC.contains(status) )
            return true;
        else
            return false;
    }

    public boolean judgeS(ReadAT at){
        if ( listCC.contains(at.status) )
            return true;
        else
            return false;
    }

    public boolean judgeS(String[] fields){
        if ( listCC.contains(fields[10]) )
            return true;
        else
            return false;
    }

    public boolean judgeS(String status){
        if ( listCC.contains(status) )
            return true;
        else
            return false;
    }

    public boolean judgeRealFrags(ReadAT at){
        if ( listRealFrags.contains(at.status) )
            return true;
        else
            return false;
    }

    public boolean judgeRealFrags(String[] fields){
        if ( listRealFrags.contains(fields[10]) )
            return true;
        else
            return false;
    }

    public boolean judgeConfidentFrags(ReadAT at){
        if ( listConfidentFrags.contains(at.status) )
            return true;
        else
            return false;
    }

    public boolean judgeConfidentFrags(String[] fields){
        if ( listConfidentFrags.contains(fields[10]) )
            return true;
        else {
            return false    ;
        }
    }

    public boolean judgeUnmapped(ReadAT at){
        if ( listUnmapped.contains(at.status) )
            return true;
        else
            return false;
    }

    public boolean judgeUnmapped(String[] fields){
        if ( listUnmapped.contains(fields[10]) )
            return true;
        else
            return false;
    }
}
