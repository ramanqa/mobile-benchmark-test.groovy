package qt.ta.dsl;

import io.appium.java_client.AppiumBy
import org.openqa.selenium.support.ui.ExpectedConditions

class NotificationPanel {
    
    def testSession
    
    def NotificationPanel(testSession){
        this.testSession = testSession
    }

    def show(){
        this.testSession.driver().openNotifications()
    }

    def hide(){
        this.testSession.driver().navigate().back()
        sleep(1000)
    }

    def enableBluetooth(){
        if(!this.testSession.device().bluetoothEnabled()){
            this.toggleBluetooth()
        }
    }

    def disableBluetooth(){
        if(this.testSession.device().bluetoothEnabled()){
            this.toggleBluetooth()
        }
    }

    def toggleBluetooth(){
        this.show()
        this.testSession.driverWait().until(
          ExpectedConditions.visibilityOfElementLocated(
            AppiumBy.accessibilityId('Bluetooth')))
        this.testSession.driver().findElement(
          AppiumBy.accessibilityId('Bluetooth')).click()
        this.hide()
    }

}
