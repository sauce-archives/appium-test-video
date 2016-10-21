import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testobject.api.TestObjectClient;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;
import org.testobject.rest.api.model.AppiumTestReport;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class CalculatorTest {

	@Rule
	public TestName testName = new TestName();

	private AppiumDriver driver;

	private final static String EXPECTED_RESULT_FOUR = "4";
	private final static String EXPECTED_RESULT_ERROR = "Error";

	@Before
	public void setUp() throws Exception {

		DesiredCapabilities capabilities = new DesiredCapabilities();

        /* These are the capabilities we must provide to run our test on TestObject. */
		capabilities.setCapability("testobject_api_key", System.getenv("TESTOBJECT_API_KEY")); // API key through env variable
		//capabilities.setCapability("testobject_api_key", "YOUR_API_KEY")); // API key hardcoded

		capabilities.setCapability("testobject_app_id", "1");

		capabilities.setCapability("testobject_device", System.getenv("TESTOBJECT_DEVICE_ID")); // device id through env variable
		//capabilities.setCapability("testobject_device", "Motorola_Moto_E_2nd_gen_real"); // device id hardcoded

		String appiumVersion = System.getenv("TESTOBJECT_APPIUM_VERSION");
		if (appiumVersion != null && appiumVersion.trim().isEmpty() == false) {
			capabilities.setCapability("testobject_appium_version", appiumVersion);
		}

		String cacheDevice = System.getenv("TESTOBJECT_CACHE_DEVICE");
		if (cacheDevice != null && cacheDevice.trim().isEmpty() == false) {
			capabilities.setCapability("testobject_cache_device", cacheDevice);
		}

		// We generate a random UUID for later lookup in logs for debugging. There's no practical purpose for this otherwise
		// so you can remove it if you'd like.
		String testUUID = UUID.randomUUID().toString();
		System.out.println("TestUUID: " + testUUID);
		capabilities.setCapability("testobject_testuuid", testUUID);

		driver = new AndroidDriver(getAppiumUrl(), capabilities);

		System.out.println(driver.getCapabilities().getCapability("testobject_test_report_url"));
		System.out.println(driver.getCapabilities().getCapability("testobject_test_live_view_url"));
	}

	@After
	public void tearDown() {
		// We need to close the Appium driver before the video will be available, but first we need information from capabilities
		// that the TestObject server will have added.

		Capabilities capabilities = driver.getCapabilities();
		String team = (String)capabilities.getCapability(TestObjectCapabilities.TESTOBJECT_USER_ID);
		String project = (String)capabilities.getCapability(TestObjectCapabilities.TESTOBJECT_PROJECT_ID);
		long reportId = (long)capabilities.getCapability(TestObjectCapabilities.TESTOBJECT_TEST_REPORT_ID);

		driver.quit();

		String filename = testName.getMethodName() + ".mp4";
		saveVideo(team, project, reportId, filename);
	}

	private static void saveVideo(String team, String project, long reportId, String filename) {
		String username = System.getenv("TESTOBJECT_USERNAME");
		String password = System.getenv("TESTOBJECT_PASSWORD");
		if (username != null && password != null) {
			System.out.println("Saving video to " + filename);

			TestObjectClient client = TestObjectClient.Factory.create(getApiBaseUrl());
			client.login(username, password);

			File video = new File(filename);
			String videoId = getVideoId(client, team, project, reportId);
			client.saveVideo(team, project, videoId, video);
			System.out.println("Saved test recording to " + filename);
		} else {
			System.out.println("No username/password set; not saving " + filename + " test recording.");
		}

	}

	// Once the test is finished (from TestObject's end this is when we call driver.quit()), the video does not instantly show up.
	// So we have a loop checking the test report for the videoId, after which point we can safely download it
	private static String getVideoId(TestObjectClient client, String team, String project, long reportId) {
		long timeout = System.currentTimeMillis() + 1000 * 60 * 10;
		while (System.currentTimeMillis() < timeout) {
			AppiumTestReport testReport = client.getTestReport(team, project, reportId);
			if (testReport.getVideoId() != null) {
				System.out.println("Got videoId");
				return testReport.getVideoId();
			} else {
				System.out.println("No videoId. Waiting...");
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
					throw new RuntimeException("Encountered exception while waiting to get video ID");
				}
			}
		}
		throw new RuntimeException("Timeout expired while waiting for videoId for " + team + "/" + project + "/" + reportId);
	}

	/* A simple addition, it expects the correct result to appear in the result field. */
	@Test
	public void twoPlusTwoOperation() {

        /* Get the elements. */
		MobileElement buttonTwo = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/digit2")));
		MobileElement buttonPlus = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/plus")));
		MobileElement buttonEquals = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/equal")));
		MobileElement resultField = (MobileElement) (driver.findElement(By.xpath("//android.widget.EditText[1]")));

        /* Add two and two. */
		buttonTwo.click();
		buttonPlus.click();
		buttonTwo.click();
		buttonEquals.click();

        /* Check if within given time the correct result appears in the designated field. */
		(new WebDriverWait(driver, 30)).until(ExpectedConditions.textToBePresentInElement(resultField, EXPECTED_RESULT_FOUR));

	}

	/* An invalid operation, it navigates to the advanced panel, selects factorial, then minus,
	 * then the equal button. The expected result is an error message in the result field. */
	@Test
	public void factorialMinusOperation() {

        /* In the main panel... */
		MobileElement menuButton = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/overflow_menu")));
		menuButton.click();

		MobileElement advancedPanelButton = (MobileElement) (new WebDriverWait(driver, 60))
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//android.widget.TextView[@text = 'Advanced panel']")));
		advancedPanelButton.click();

        /* In the advanced panel... */
		MobileElement factorialButton = (MobileElement) (new WebDriverWait(driver, 60))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("net.ludeke.calculator:id/factorial")));
		factorialButton.click();

        /* In the main panel again. */
		MobileElement minusButton = (MobileElement) (new WebDriverWait(driver, 60))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("net.ludeke.calculator:id/minus")));
		minusButton.click();

		MobileElement equalsButton = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/equal")));
		equalsButton.click();

		MobileElement resultField = (MobileElement) (driver.findElement(By.xpath("//android.widget.EditText[1]")));

		(new WebDriverWait(driver, 30)).until(ExpectedConditions.textToBePresentInElement(resultField, EXPECTED_RESULT_ERROR));

	}

	// We sometimes override the Appium URL for internal testing.
	private URL getAppiumUrl() throws MalformedURLException {
		String override = System.getenv("APPIUM_SERVER");
		return new URL(override != null ? override : "http://appium.testobject.org/wd/hub");
	}

	// Same thing for the API URL.
	private static String getApiBaseUrl() {
		String override = System.getenv("API_BASE_URL");
		return override != null ? override : "https://appium.testobject.com/api/rest";
	}
}
