package qt.ta.dsl;

import groovy.json.JsonSlurper

class Pi {

    def testSession

    def Pi(testSession){
        this.testSession = testSession;
    }

    def config(){
        return new JsonSlurper().parse(
            new File(this.getClass().getClassLoader()
            .getResource('pi.config.json').toURI()))
    }

    def name(){
        return this.config().name
    }

    def runSsh(command){
        return "ssh ${this.config().ip} ${command}".split(" ").execute().text
    }

    def bluetoothDiscoverable() {
        return this.runSsh("bluetoothctl discoverable on")
    }

    def bluetoothUndiscoverable() {
        return this.runSsh("bluetoothctl discoverable off")
    }
    
}
