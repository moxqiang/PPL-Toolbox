package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GenerateSimulatedDataset {
    public static void main(String[] args) {
        String workingDirectory = System.getProperty("user.dir");
        System.out.println("INFO:Current working directory : " + workingDirectory);
        if (args.length < 2) {
            System.out.println("Usage: java -cp pore-c_tool.jar utils.GenerateSimulatedDataset  <config.properties>");
            System.exit(0);
        }

        //java如何通过配置文件读入参数
        Properties config = new Properties();
        try (InputStream is = new FileInputStream("./src/main/resources/testFile/simulatedConfig.properties")) {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String param1 = config.getProperty("param1");
        String param2 = config.getProperty("param2");

        for (String configName:
        config.stringPropertyNames()) {
            System.out.println("INFO:"+configName+"\t"+config.getProperty(configName));
        }

        System.out.println("INFO:This option is unfinished!");

    }
}
