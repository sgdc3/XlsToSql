package com.github.sgdc3.xlstosql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Main {

    public static void main(String args[]) {
        System.out.println("Xls to SqlDump converter by sgdc3 (Gabriele C.) V1.1");
        if (args.length != 2) {
            System.out.println("Arguments: SourceFile TargetFile");
            return;
        }

        File sourceFile = new File(args[0]);
        File targetFile = new File(args[1]);

        try (InputStream inputStream = new FileInputStream((sourceFile))) {
            String result = XlsToSqlConverter.convert(inputStream);
            Files.write(targetFile.toPath(), result.getBytes());
        } catch (IOException e) {
            System.out.println("An error occurred! Please check the stacktrace:");
            e.printStackTrace();
        }

        System.out.println("Completed!");
    }
}
