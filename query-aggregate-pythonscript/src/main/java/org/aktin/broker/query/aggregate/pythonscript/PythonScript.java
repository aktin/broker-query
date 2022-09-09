package org.aktin.broker.query.aggregate.pythonscript;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PythonScript {
    private static final Logger log = Logger.getLogger(PythonScript.class.getName());

    /** executable path of the Rscript command */
    private Path pythonScriptExecutable;

    private boolean isDebugPrintMode = false;

    public PythonScript(Path executable) {
        this.pythonScriptExecutable = executable;
    }

    public String getVersion() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(pythonScriptExecutable.toString(), "--version");
        Path temp = Files.createTempDirectory("python-version");
        pb.directory(temp.toFile());
        Process process = pb.start();
        int exitCode;
        try {
            if( !process.waitFor(500, TimeUnit.MILLISECONDS) ) {
                // process did not terminate within the specified time
                process.destroyForcibly();
                throw new IOException("Python process did not terminate in the specified time");
            }else {
                exitCode = process.exitValue();
            }
        } catch (InterruptedException e) {
            throw new IOException("Interrupted during Python execution", e);
        }
        InputStream output = process.getInputStream();
        InputStream error = process.getErrorStream();
        String value = convertStreamToString(error);
        if( value == null || value.length() == 0 ) {
            // use stdout
            value = convertStreamToString(output);
        }
        output.close();
        error.close();
        try {
            Files.delete(temp);
        }catch( IOException e ) {
            log.log(Level.WARNING, "Unable to delete temp directory "+temp, e);
        }
        if( exitCode != 0 ) {
            throw new IOException("Non-zero exit code");
        }
        return  value;
    }

    private static final String RSCRIPT_OUTPUT_PACKAGE_VERSIONS="write.table(x=subset(x=data.frame(installed.packages(noCache=TRUE)),select=Version,is.na(Priority) | Priority != 'base' | Package=='base'),quote=FALSE,sep='\\t',col.names=FALSE,file='versions.txt')";
    private static final String VERSIONS_SCRIPT_NAME = " -m pip freeze > requirements.txt";

    // TODO change to asynchronous implementation via process.onExit
    // method should then return a CompletableFuture<Map<String,String>>
    public Map<String, String> getPackageVersions() throws IOException{
        ProcessBuilder pb = new ProcessBuilder(pythonScriptExecutable.toString(), "-m", "pip", "freeze");
        Path temp = Files.createTempDirectory("python-version");
        pb.directory(temp.toFile());
        log.info(pb.command().toString());
        Process process = pb.start();
        int exitCode;
        try {
            if( !process.waitFor(10, TimeUnit.SECONDS) ) {
                // process did not terminate within the specified time
                process.destroyForcibly();
                throw new IOException("Python process did not terminate in the specified time");
            }else {
                exitCode = process.exitValue();
            }
        } catch (InterruptedException e) {
            throw new IOException("Interrupted during Python execution", e);
        }

        InputStream output = process.getInputStream();
        InputStream error = process.getErrorStream();
        String value = convertStreamToString(error);
        if( value == null || value.length() == 0 ) {
            // use stdout
            value = convertStreamToString(output);
        }
        output.close();
        error.close();
        Map<String, String> map = null;
        if( exitCode == 0 ) {
            // read versions output
            String[] lines = value.split("\n");
            map = new HashMap<String,String>();
            for (String line : lines) {
                int t = line.indexOf("==");
                if( t == -1 ){
                    continue;
                }
                map.put(line.substring(0, t), line.substring(t+1, line.length()));
            }
        }
        try {
            Files.delete(temp);
        }catch( IOException e ) {
            log.log(Level.WARNING, "Unable to delete temp directory "+temp, e);
        }
        if( exitCode != 0 ) {
            log.warning("Python stderr: "+value);
            throw new IOException("Non-zero exit code "+exitCode);
        }
        return map;
    }

    private <E extends Exception> void destroyForciblyWaitAndThrow(Process process, E e) throws E {
        Process waiter = process.destroyForcibly();
        boolean finished = false;
        try {
            finished = waiter.waitFor(700, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e1) {
            e.addSuppressed(e1);
        }
        if( finished == false ) {
            log.warning("Unable to destroy the process forcibly in the specified time");
        }
        throw e;
    }
    /**
     * Run the Rscript executable. Working directory will be set as specified in the arguments,
     * as well as the main script.
     * @param workingDir Working directory for the Rscript executable. It must contain at least the file specified in the next argument.
     * @param mainScript File name for the main script to be executed in the working directory. The directory must contain the specified file name.
     * @param waitMillis Time to wait for the process to finish. If {@code null} the process will wait without limit.
     * @throws IOException IO error, e.g. with processing the process' output
     * @throws TimeoutException the specified timeout elapsed before the process exited
     * @throws AbnormalTerminationException  process terminated abnormally. the provided script did not execute successfully.
     *   For exit code and STDERR output see {@link AbnormalTerminationException}.
     */
    public void runPythonScript(Path workingDir, String mainScript, Integer waitMillis) throws IOException, TimeoutException, AbnormalTerminationException {
        ProcessBuilder pb = new ProcessBuilder(pythonScriptExecutable.toString(), mainScript);
        pb.directory(workingDir.toFile());
        log.info(pb.command().toString());

        Process process = pb.start();
        // get the error stream of the process and print it
        InputStream error = process.getErrorStream();
        // get the output stream of the process and print it
        InputStream output = process.getInputStream();

        int exitCode = -1;
        try {
            if( waitMillis != null ) {
                boolean finished = process.waitFor(waitMillis, TimeUnit.MILLISECONDS);
                if(finished) {
                    // process finished, get exit value
                    exitCode = process.exitValue();
                }else {
                    log.warning("Process did not finish in the specified time, trying to kill it..");
                    // timeout elapsed. Kill process
                    destroyForciblyWaitAndThrow(process, new TimeoutException("Timeout elapsed for Python script execution"));
                }
            }else{
                // wait without timeout
                exitCode = process.waitFor();
            }
        } catch (InterruptedException e) {
            destroyForciblyWaitAndThrow(process, new IOException("Interrupted during Python execution", e));
        }
        if( exitCode != 0 ){
            // Rscript did not terminate successfully
            // something went wrong
            String stderr = null;
            if (error.available() > 0) {
                stderr = convertStreamToString(error);
            }
            throw new AbnormalTerminationException(exitCode, stderr, isDebugPrintMode);
        }
        error.close();

        if (output.available() > 0) {
            log.warning("non-empty Python stdout: "+convertStreamToString(output));
        }
        // read output stream

        output.close();
    }

    // debugging only
    public String convertStreamToString(InputStream is) throws IOException {
        // To convert the InputStream to String we use the
        // Reader.read(char[] buffer) method. We iterate until the
        // Reader return -1 which means there's no more data to
        // read. We use the StringWriter class to produce the string.
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        }
        return "";
    }

    /**
     * Activate/Deactive printing full error log of AbnormalTerminationException
     */
    public void setDebugPrintMode(boolean debugPrintMode) {
        isDebugPrintMode = debugPrintMode;
    }
}
