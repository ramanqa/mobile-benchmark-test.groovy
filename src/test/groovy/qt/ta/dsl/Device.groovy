package qt.ta.dsl;


class Device {

    def testSession

    def Device(testSession){
        this.testSession = testSession;
    }

    def bluetoothEnabled() {
        def output = "adb shell dumpsys bluetooth_manager".split(" ").execute().text
        return output.split("enabled:")[1].split("\n")[0].trim().toBoolean()
    }
    
}
