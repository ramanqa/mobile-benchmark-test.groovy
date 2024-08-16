package qt.ta.dsl;

import io.appium.java_client.AppiumBy
import org.openqa.selenium.support.ui.ExpectedConditions

class ConnectedDevices {
    
    def testSession
    
    def ConnectedDevices(testSession){
        this.testSession = testSession
    }

    def notificationPanel(){
        return new NotificationPanel(this.testSession)
    }

    def pairNewDevice(){
        this.testSession.driverWait().until(
            ExpectedConditions.visibilityOfAllElementsLocatedBy(
              AppiumBy.id('android:id/title')))
        def elements = this.testSession.driver().findElements(
            AppiumBy.id('android:id/title'))
        for(element in elements){
            if(element.getText().equals('Pair new device')){
                element.click()
                break
            }
        }
        sleep(5000)
        return new PairNewDevice(this.testSession)
    }
}
