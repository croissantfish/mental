package cn.edu.sjtu.songyuke.mental.test;

import cn.edu.sjtu.songyuke.mental.core.Run;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class CorrectTest {
    private String mxFile;

    public CorrectTest(String mxFile) {
        this.mxFile = mxFile;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws URISyntaxException {
        Collection<Object[]> parameters = new ArrayList<>();

        String mxFileFolderPath = new File(CorrectTest.class.getResource("/final/mx").toURI()).getAbsolutePath();
        String sFileFolderPath = mxFileFolderPath.replace("final/mx", "final/s");

        if (new File(sFileFolderPath).mkdirs()) {
            System.out.printf("Assembly output directory %s created sucessfully.%n", sFileFolderPath);
        }
        ;

        String outFileFolderPath = mxFileFolderPath.replace("final/mx", "final/out");
        if (new File(outFileFolderPath).mkdirs()) {
            System.out.printf("Program output directory %s created sucessfully.%n", outFileFolderPath);
        }
        ;

        for (File file : Objects.requireNonNull(new File(mxFileFolderPath).listFiles())) {
            if (file.isFile() && file.getName().endsWith(".mx")) {
                parameters.add(new Object[]{mxFileFolderPath + "/" + file.getName()});
            }
        }

        return parameters;
    }

    @Test
    public void testPass() throws URISyntaxException, IOException, InterruptedException {
        String sFile = mxFile.replace("mx/", "s/").replace(".mx", ".s");
        String inFile = mxFile.replace("mx/", "in/").replace(".mx", ".in");
        String outFile = mxFile.replace("mx/", "out/").replace(".mx", ".out");
        String ansFile = mxFile.replace("mx/", "ans/").replace(".mx", ".ans");
        String limFile = mxFile.replace("mx/", "limit/").replace(".mx", ".limit");
        String exceptionFile = new File(CorrectTest.class.getResource("/lib/exceptions.s").toURI()).getAbsolutePath();

        try {
            new Run().compile(new FileInputStream(mxFile), new FileOutputStream(sFile));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("compile completed.");

        URL spimUrl = null;
        if (System.getProperty("os.name").startsWith("Windows")) {
            spimUrl = CorrectTest.class.getResource("/lib/win/spim.exe");
        } else if (System.getProperty("os.name").startsWith("Mac OS X")) {
            spimUrl = CorrectTest.class.getResource("/lib/mac/spim");
        } else if (System.getProperty("os.name").startsWith("Linux")) {
            spimUrl = CorrectTest.class.getResource("/lib/linux/spim");
        }

        String spimPath = new File(spimUrl.toURI()).getAbsolutePath();
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(spimPath, "-exception_file", exceptionFile, "-stat", "-file", sFile);
        Process process = pb.start();
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
            PrintStream output = new PrintStream(process.getOutputStream());
            for (String line; (line = input.readLine()) != null; output.println(line)) ;
            output.flush();
        } catch (FileNotFoundException e) {
            // This test case does not require any input.
        }

        process.waitFor();

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            PrintStream output = new PrintStream(new FileOutputStream(outFile));
            for (String line; (line = input.readLine()) != null; ) {
                if (!line.startsWith("Loaded")) {
                    output.println(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int limit = Integer.MAX_VALUE;
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(limFile)));
            limit = Integer.parseInt(input.readLine());
        } catch (FileNotFoundException e) {
            System.out.println("The limit file (" + limFile + ") does not exist.");
        }

        int all = Integer.MAX_VALUE;
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        for (String line; (line = input.readLine()) != null; ) {
            if (line.startsWith("[Statistics]\t")) {
                line = line.replace("[Statistics]\tAll:\t", "").replaceAll("\t", "\n");
                all = Integer.parseInt(line.substring(0, line.indexOf("\n")));
            }
        }

        try (BufferedReader output = new BufferedReader(new InputStreamReader(new FileInputStream(outFile)))) {
            try (BufferedReader answer = new BufferedReader(new InputStreamReader(new FileInputStream(ansFile)))) {

                while (true) {
                    String line1 = output.readLine();
                    String line2 = answer.readLine();
                    if (line1 == null && line2 == null) {
                        break;
                    }
                    if (line1 == null || !line1.equals(line2)) {
                        System.err.println("output: " + line1);
                        System.err.println("answer: " + line2);
                        fail("Wrong answer!");
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("No answer file!");
            }
        } catch (FileNotFoundException e) {
            System.err.println("No output file!");
        }
        System.out.printf("Runtime Instruction: %d, limited with %d%n", all, limit);
        if (all > limit) {
            fail("Limit exceeded!");
        }
    }
}