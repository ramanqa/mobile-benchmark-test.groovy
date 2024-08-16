package qt.ta.dsl;

import io.appium.java_client.AppiumBy
import org.openqa.selenium.support.ui.ExpectedConditions

class PairNewDevice {
    
    def testSession
    
    def PairNewDevice(testSession){
        this.testSession = testSession
    }

    def notificationPanel(){
        return new NotificationPanel(this.testSession)
    }

    def availableDevices(){
        this.testSession.driverWait().until(
            ExpectedConditions.visibilityOfElementLocated(
              AppiumBy.xpath('//androidx.recyclerview.widget.RecyclerView/android.widget.LinearLayout/android.widget.LinearLayout')))
        def bluetoothDeviceContainers = this.testSession.driver().findElements(
            AppiumBy.xpath('//androidx.recyclerview.widget.RecyclerView/android.widget.LinearLayout/android.widget.LinearLayout'))
        bluetoothDeviceContainers.remove(bluetoothDeviceContainers.size()-1)
        def bluetoothDevices = []
        bluetoothDeviceContainers.each{
            bluetoothDevices.add(it.findElement(AppiumBy.id('android:id/title')))
        }
        return bluetoothDevices
    }

    def availableDeviceNames(){
        def names = []
        this.availableDevices().each{
            names.push(it.getText())
        }
        return names
    }
}
