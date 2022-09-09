package org.aktin.broker.query.aggregate.pythonscript;

import org.junit.Test;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TestExecutor {

	public static PythonSource getPythonScript() throws IOException{
		PythonSource q;
		try( InputStream in = TestExecutor.class. getResourceAsStream("/query-pythonscript.xml") ){
			q = JAXB.unmarshal(in,PythonSource.class);
		}
		return q;
	}
	public static PythonSource getRScriptWithError() throws IOException{
		PythonSource q;
		try( InputStream in = TestExecutor.class. getResourceAsStream("/query-pythonscript.xml") ){
			q = JAXB.unmarshal(in,PythonSource.class);
		}
		return q;
	}

	public static Map<String,String> getTestLookup(){
		Map<String, String> m = new HashMap<>();
		m.put("data.start", "2016-01-01");
		m.put("data.end", "2017-12-31");
		return m;
	}

	private static final String testDataDir = "/data/";
	private static final String[] testData1 = {"temp_encounters.txt"};
	private void copyInputFiles1(Path dest) throws IOException {
		// copy test data files
		for( int i=0; i<testData1.length; i++ ) {
			String name = testData1[i];
			try( InputStream in = TestExecutor.class.getResourceAsStream(testDataDir+name) ){
				Files.copy(in, dest.resolve(name));
			}
		}
	}
	private void deleteInputFiles1(Path base) throws IOException {
		for( int i=0; i<testData1.length; i++ ) {
			String name = testData1[i];
			Files.delete(base.resolve(name));
		}		
	}

	@Test
	public void testMoveResultFilesToSameDirectory() {
		// TODO
	}

	@Test
	public void testMoveResultFilesToOtherDirectory() {
		// TODO
	}
	@Test
	public void testMoveResultFilesToZipArchive() {
		// TODO
	}

	@Test
	public void executeRScript() throws IOException {
		PythonSource rs = getPythonScript();
		executeRScript(rs);
	}
	public void executeRScript(PythonSource rs) throws IOException {
		
		Execution exec = new Execution(rs);
		Path testDir = Files.createTempDirectory("aggregate-python");
		copyInputFiles1(testDir);

		exec.setPythonScriptExecutable(TestPythonScript.findPython());
		exec.setWorkingDir(testDir);
		exec.createFileResources();


		exec.runRscript();

		exec.removeFileResources();
		deleteInputFiles1(testDir);

		// move result files is tested in separate unit tests, see above
		
		exec.removeResultFiles();
		exec.removePycache();

		Files.delete(testDir);
	}
}
