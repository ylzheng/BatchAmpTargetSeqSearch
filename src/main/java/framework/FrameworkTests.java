package framework;

public class FrameworkTests extends TestSuite
{public FrameworkTests()
	{tests = new Test[3];
	tests[0] = new GoodTest();
	tests[1] = new FailTest();
	tests[2] = new AbortTest();};}
