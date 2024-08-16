package qt.ta

import spock.lang.Specification
import spock.lang.Shared

import qt.ta.dsl.TestSession;
import qt.ta.dsl.Settings;

class FirstSpec extends Specification {

    @Shared benchmark

    def setupSpec(){
        benchmark = BenchmarkSession.start(['name':'BT_Test_1', 'packages':['com.google.android.apps.maps', 'com.android.settings']])
    }

    def cleanupSpec(){
        benchmark.quit()
    }

    def "test1"(iterationName){
        
        given:
        benchmark.startTest(iterationName);
        def testSession = TestSession.forApp("settings")
        def settings = new Settings(testSession)
        testSession.pi().bluetoothUndiscoverable()

        when:
        // start device bluetooth
        settings.notificationPanel().enableBluetooth()

        then:
        // check device pairing does not have pi
        def pairNewDevice = settings
          .connectedDevices()
          .pairNewDevice()
        
        println(pairNewDevice.availableDeviceNames())

        // make pi bluetooth discoverable
        testSession.pi().bluetoothDiscoverable()
        sleep(5000)
        // check device pairing shows pi
        println(pairNewDevice.availableDeviceNames())

        cleanup:
        settings.notificationPanel().disableBluetooth()
        testSession.pi().bluetoothUndiscoverable()
        testSession.driver.quit()
        benchmark.endTest();

        where:
        iterationName|_
        "BT Test 1"|_
        "BT Test 2"|_
        
    }
  /*
    def "test2"() {
        setup:
        def session = BenchmarkSession.start(['name':'test3', 'packages':['io.appium.uiautomator2.server','com.google.android.apps.maps','io.appium.settings']])

        when:
        def i = 1;
        while(i<5){
            session.startTest("Iteration " + i++);
            sleep(5000)
            session.endTest();
        }

        then:
        true != false

        session.quit()
    }
  */
}
