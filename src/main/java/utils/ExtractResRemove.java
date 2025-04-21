package utils;

public class ExtractResRemove {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.ExtractResRemove <aligntable> <resRemove>");
            System.exit(0);
        }
    }
}
