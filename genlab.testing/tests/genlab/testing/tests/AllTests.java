package genlab.testing.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses(
		{ 
			genlab.igraph.AllTests.class,
			
			}
		)
public class AllTests {

	public AllTests() {
	}

}
