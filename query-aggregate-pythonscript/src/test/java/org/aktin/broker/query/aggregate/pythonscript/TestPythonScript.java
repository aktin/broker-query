package org.aktin.broker.query.aggregate.pythonscript;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class TestPythonScript {
    private static final String PYTHONSCRIPT_BINARY = "pythonscript.binary";

    static final String[] pythonPathSearch = {
            "/usr/bin/python3"
            //todo add for windows and multiple python versions
    };

    public static Path findPython(){
        // try system property 'rscript.binary'
        Path p;
        String path = System.getProperty(PYTHONSCRIPT_BINARY);
        if( path != null ){
            p = Paths.get(path);
            if( !Files.isExecutable(p) ){
                Assert.fail("System property '"+ PYTHONSCRIPT_BINARY +"' defined, but target not found/executable.");
            }
            return p;
        }
        // try windows path
        for( String binary : pythonPathSearch){
            p = Paths.get(binary);
            if( Files.isExecutable(p) ){
                return p;
            }
        }
        Assert.fail("Path to Python Script not found. Please edit "+TestPythonScript.class.getName()+".java or define a (local) system property: "+ PYTHONSCRIPT_BINARY);
        return null;
    }

    @Test
    public void testRetrieveVersions() throws IOException {
        Path p = findPython();
        PythonScript ps = new PythonScript(p);
        String version = ps.getVersion();
        Assert.assertNotNull(version);
        Assert.assertNotEquals("", version);
        System.out.println("Python --version output: "+version);
        Map<String, String> packages = ps.getPackageVersions();

        Assert.assertNotNull(packages.get("numpy"));
        Assert.assertNotNull(packages.get("pandas"));
        //todo add required packages
        packages.forEach( (k,v) -> System.out.println("Python package "+k+": "+v) );
    }

    @Test
    public void verifyExecutionTimeout() throws IOException {
        Path p = findPython();
        PythonScript ps = new PythonScript(p);

        Path dir = Files.createTempDirectory("python-script-test");
        Path script = dir.resolve("main.py");
        try( BufferedWriter w = Files.newBufferedWriter(script, StandardOpenOption.CREATE_NEW) ){
            w.write("import time\ntime.sleep(10)\n");
        }
        // verify that we don't need to wait for the process to exit
        long start = System.currentTimeMillis();
        try {
            ps.setDebugPrintMode(true);
            ps.runPythonScript(dir, script.getFileName().toString(), 1000);
            Assert.fail("Process should not have been terminated regularly");
        } catch (TimeoutException e) {
            // this is what we expect!
            // fall through to outside of try
        } catch (AbnormalTerminationException e) {
            Assert.fail();
        }
        long elapsed = System.currentTimeMillis() - start;
        // remove directories
        try{
            Files.delete(script);
            Files.delete(dir);
        }catch( IOException e ) {
            System.err.println("Unable to delete temporary script because process is still alive");
        }
        // verify early termination
        Assert.assertTrue("Process should be terminated earlier", elapsed < 3000);
    }

    @Test
    public void verifyAbnormalTerminationStderr() throws IOException {
        Path p = findPython();
        PythonScript ps = new PythonScript(p);
        Path dir = Files.createTempDirectory("python-script-test");
        Path script = dir.resolve("main.py");
        try( BufferedWriter w = Files.newBufferedWriter(script, StandardOpenOption.CREATE_NEW) ){
            w.write("this.function.does.no.exist()\n");
        }
        try {
            ps.runPythonScript(dir, script.getFileName().toString(), null);
            Assert.fail("Process should not have been termianted regularly");
        } catch (TimeoutException e) {
            // no timeout expected
            Assert.fail();
        } catch (AbnormalTerminationException e) {
            // this is what we want!
            Assert.assertEquals(1, e.getExitCode());
            String eout = e.getErrorOutput();
            Assert.assertTrue(eout.contains("this.function.does.no.exist"));
        }
    }
}
