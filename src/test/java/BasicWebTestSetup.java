import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BasicWebTestSetup {

    private AppiumDriver driver;

    /* This is the setup that will be run before the test. */
    @Before
    public void setUp() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("testobject_app_id", "1");
        capabilities.setCapability("testobject_api_key", System.getenv("TESTOBJECT_API_KEY_WEB"));
        capabilities.setCapability("testobject_device", System.getenv("TESTOBJECT_DEVICE_ID"));

        String appiumVersion = System.getenv("TESTOBJECT_APPIUM_VERSION");
        if(appiumVersion != null && appiumVersion.trim().isEmpty() == false){
            capabilities.setCapability("testobject_appium_version", appiumVersion);
        }

        // We generate a random UUID for later lookup in logs for debugging
        String testUUID = UUID.randomUUID().toString();
        System.out.println("TestUUID: " + testUUID);
        capabilities.setCapability("testobject_testuuid", testUUID);

        driver = new AndroidDriver(new URL(System.getenv("APPIUM_SERVER")), capabilities);

        System.out.println(driver.getCapabilities().getCapability("testobject_test_report_url"));
        System.out.println(driver.getCapabilities().getCapability("testobject_test_live_view_url"));
    }

    @After
    public void tearDown(){
        if(driver != null){
            driver.quit();
        }
    }

    @Test
    public void openWebpageAndTakeScreenshot() {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        String url = "https://www.google.com";

        driver.get(url);
        takeScreenshot();
    }

    private void takeScreenshot() {
        try {
            driver.getScreenshotAs(OutputType.FILE);
        } catch (Exception e) {
            System.out.println("Exception while saving the file " + e);
        }
    }
}
