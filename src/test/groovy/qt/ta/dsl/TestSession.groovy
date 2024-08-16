package qt.ta.dsl;

import groovy.json.JsonSlurper

import org.openqa.selenium.remote.DesiredCapabilities
import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.support.ui.WebDriverWait

import java.time.Duration

public class TestSession {

    def driver;
    def config;

    public TestSession(config){
        this.config = [:];
        this.config['appium:deviceName'] = 'Pixel2'
        this.config['appium:autoGrantPermissions'] = true
        this.config['appium:fullContextList'] = true
        this.config['appium:newCommandTimeout'] = 120
        this.config['platformName'] = 'android'
        this.config['appium:noReset'] = true
        this.config['appium:fullReset'] = false
        
        this.config = this.config + config;

        this.driver = new AndroidDriver(
            new URL(appiumConfig()['hostUrl']),
            new DesiredCapabilities(this.config))

    }

    def driver(){
        return this.driver
    }

    def device(){
        return new Device(this)
    }

    def pi(){
        return new Pi(this)
    }

    def config(){
        return this.config
    }

    static def forApp(appName){
        def appConfig = new JsonSlurper()
          .parse(new File(TestSession.class.getClassLoader()
          .getResource('app.capabilities.json').toURI()))
        return new TestSession(appConfig[appName]);
    }

    def appiumConfig(){
        return new JsonSlurper().parse(
            new File(this.getClass().getClassLoader()
            .getResource('appium.config.json').toURI()))
    }

    def driverWait(){
        return this.driverWait(30);
    }

    def driverWait(Integer timeoutInSeconds){
        return new WebDriverWait(this.driver(), Duration.ofSeconds(timeoutInSeconds));
    }

}
