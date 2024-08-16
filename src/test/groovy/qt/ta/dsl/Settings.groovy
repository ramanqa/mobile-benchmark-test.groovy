package qt.ta.dsl;

import io.appium.java_client.AppiumBy
import org.openqa.selenium.support.ui.ExpectedConditions

class Settings {
    
    def testSession
    
    def Settings(testSession){
        this.testSession = testSession
    }

    def notificationPanel(){
        return new NotificationPanel(this.testSession)
    }

    def connectedDevices(){
        this.testSession.driverWait().until(
            ExpectedConditions.visibilityOfAllElementsLocatedBy(
              AppiumBy.id('android:id/title')))
        def elements = this.testSession.driver().findElements(
            AppiumBy.id('android:id/title'))
        for(element in elements){
            if(element.getText().equals('Connected devices')){
                element.click()
                break
            }
        }
        return new ConnectedDevices(this.testSession)
    }
}
