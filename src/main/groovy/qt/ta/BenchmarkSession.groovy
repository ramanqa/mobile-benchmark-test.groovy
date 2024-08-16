package qt.ta;

import groovy.json.JsonOutput
import kong.unirest.Unirest

public class BenchmarkSession {

    def id;
    def packages = [];
    def wsClient;
    def frameClient;
    def recording = true;

    def deviceTopProcess;
    def packagesTopProcess;
    def deviceBatteryStatsProcess;

    public BenchmarkSession(config){
        this.id = config['name']
        if(config.containsKey('packages')){
            this.packages = config['packages']
        }
        // start new session
        Unirest.get("http://localhost:4500/api/session/start/" + this.id)
            .asString()

        this.wsClient = new WSClient('ws://localhost:4500/data')
        this.wsClient.connect()
        this.frameClient = new WSClient('ws://localhost:4500/frame')
        this.frameClient.connect()
        sleep(2000)
        this.startRecording()
        sleep(5000)
    }

    public static BenchmarkSession start(config){
        return new BenchmarkSession(config)
    }

    public void quit(){
        sleep(2000)
        this.recording = false;
        this.deviceTopProcess.destroyForcibly()
        def record = [:]
        def timestamp = new Date().getTime()
        record['scope'] = 'end'
        record['state'] = 'end'
        record['timestamp'] = timestamp
        record['value'] = ['url':'http://localhost:4500/session/' + this.id]
        this.wsClient.send(JsonOutput.toJson(record))
        this.wsClient.close()
        this.frameClient.close()
        // save session
        Unirest.get("http://localhost:4500/api/session/save/" + this.id)
            .asString()
    }

    public void startTest(String testDescription){
        def record = [:]
        def timestamp = new Date().getTime()
        record['scope'] = 'test'
        record['state'] = 'start'
        record['timestamp'] = timestamp
        record['value'] = ['description':testDescription]
        this.wsClient.send(JsonOutput.toJson(record))
    }

    public void endTest(){
        this.endTest("")
    }

    public void endTest(String testDescription){
        def record = [:]
        def timestamp = new Date().getTime()
        record['scope'] = 'test'
        record['state'] = 'end'
        record['timestamp'] = timestamp
        record['value'] = ['description':testDescription]
        this.wsClient.send(JsonOutput.toJson(record))
    }

    public void startRecording(){
        def deviceBluetoothInfoThread = new Thread(new RecorderThread(this.deviceBluetoothInfo))
        deviceBluetoothInfoThread.start()
        def deviceTopRecorderThread = new Thread(new RecorderThread(this.deviceTop))
        deviceTopRecorderThread.start()
        def deviceBatteryStatsRecorderThread = new Thread(new RecorderThread(this.deviceBatteryStats))
        deviceBatteryStatsRecorderThread.start()
        if(!this.packages.empty){
            def packagesTopRecorderThread = new Thread(new RecorderThread(this.packagesTop))
            packagesTopRecorderThread.start()
        }
        def deviceFrameThread = new Thread(new RecorderThread(this.frameStream))
        deviceFrameThread.start()
    }

    def deviceTop = {
        this.deviceTopProcess = "adb shell top -d 0.5 -m 1".split(" ").execute()
        try{
            this.deviceTopProcess.in.newReader().eachLine { line ->
                if(line.contains(" Mem: ")){
                    def record = [:]
                    def timestamp = new Date().getTime()
                    record['total'] = line.split(" Mem: ")[1].split(" total,")[0].replace("K", "000").trim().toLong();
                    record['used'] = line.split(" Mem: ")[1].split(" used,")[0].split("total,")[1].replace("K", "000").trim().toLong();
                    record['free'] = line.split(" Mem: ")[1].split(" free,")[0].split("used,")[1].replace("K", "000").trim().toLong();
                    def l = ['scope':'device', 'record':'memory', 'timestamp':timestamp, 'value':record]
                    this.wsClient.send(JsonOutput.toJson(l))
                }
                if(line.contains("%cpu")){
                    def record = [:]
                    def timestamp = new Date().getTime()
                    line.split(" ").each {item -> 
                      if(item.trim() != ""){
                        record[item.split("%")[1]] = item.split("%")[0]
                      }
                    }
                    def l = ['scope':'device', 'record':'cpu', 'timestamp':timestamp, 'value':record]
                    this.wsClient.send(JsonOutput.toJson(l))
                }
            }
        }catch(Exception e){}
    }

    def packagesTop = {
        while(this.recording){
            try{
                def output = "adb shell top -o %CPU -o %MEM -o CMDLINE -m 100 -n 1 -q".split(" ").execute().text
                def timestamp = new Date().getTime()
                def value = [] 
                this.packages.each { packageName -> { 
                        output.split("\n").each{ line ->
                            if(line.contains(packageName)){
                                def packageRecord = ['name':packageName]
                                packageRecord['%memory'] = line.split(" ")[4].trim().toFloat();
                                packageRecord['%cpu'] = line.split(" ")[1].trim().toFloat();
                                value.add(packageRecord)
                            }
                        }
                    }
                }
                if(!value.empty){
                    def l = ['scope':'package', 'timestamp':timestamp, 'record':'memory, cpu', 'value':value]
                    this.wsClient.send(JsonOutput.toJson(l))
                }
                sleep(1000)
            }catch(Exception e){
                sleep(1000)
            }
        }
    }

    def deviceBatteryStats = {
        while(this.recording){
            try{
                def output = "adb shell dumpsys battery".split(" ").execute().text
                def record = [:]
                def timestamp = new Date().getTime()
                record['level'] = output.split("level:")[1].split("\n")[0].trim()
                record['charge'] = output.split("Charge counter:")[1].split("\n")[0].trim()
                def l = ['scope':'device', 'record':'battery', 'timestamp': timestamp, 'value':record]
                this.wsClient.send(JsonOutput.toJson(l))
                sleep(2000)
            }catch(Exception e){
                sleep(2000)
            }
        }
    }
     
    def deviceBluetoothInfo = {
        while(this.recording){
            try{
                def output = "adb shell dumpsys bluetooth_manager".split(" ").execute().text
                def record = [:]
                def timestamp = new Date().getTime()
                record['enabled'] = output.split("enabled:")[1].split("\n")[0].trim().toBoolean()
                def l = ['scope':'device', 'record':'bluetooth', 'timestamp': timestamp, 'value':record]
                this.wsClient.send(JsonOutput.toJson(l))
                sleep(1000)
            }catch(Exception e){
                sleep(1000)
            }
        }
    }

    def frameStream = {
        "rm -Rf build/${this.id}".split(" ").execute().text
        "mkdir build/${this.id}".split(" ").execute().text
        while(this.recording){
            try{
                def timestamp = new Date().getTime()
                ['bash', '-c', "adb shell screencap -p > build/${this.id}/frame.jpg"].execute().text
                "convert build/${this.id}/frame.jpg -resize 200 build/${this.id}/${timestamp}.jpg".split(" ").execute().text
                "convert build/${this.id}/${timestamp}.jpg -resize 20 build/${this.id}/thumbnail_${timestamp}.jpg".split(" ").execute().text
                def frame = "base64 build/${this.id}/${timestamp}.jpg".split(" ").execute().text
                this.frameClient.send(frame)
                sleep(1)
            }catch(Exception e){
                sleep(1)
            }
        }
    }
}

class RecorderThread implements Runnable {
    private def method;

    public RecorderThread(method) {
        this.method = method;
    }

    @Override
    public void run() {
        this.method.call()
    }
}
