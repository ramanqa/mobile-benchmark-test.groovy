package qt.ta;

import io.javalin.Javalin
import java.util.Map
import java.util.HashMap

import io.javalin.http.staticfiles.Location

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import groovy.io.FileType

public class ReportServer{

    def server;
    def thread;
    def sessionData;
    def clients;
    def frameClients;
    def testSessionName = "UNDEFINED";

    public static void main(String[] args){
        ReportServer.start();
    }

    public ReportServer(){
        this.clients = [];
        this.frameClients = [];
        this.sessionData = [];
        this.thread = Thread.start {
            println("pwd".execute().text)
            this.server = Javalin.create()
            .get("/", context -> context.result("Hello World"))
            .get("/api/session/start/{name}", context -> {
                this.testSessionName = context.pathParam("name")
                this.sessionData = []
                //this.clients = []
                //this.frameClients = []
                context.result("RESET")
            })
            .get("/api/session/save/{name}/{timestamp}", context -> {
                def json = JsonOutput.toJson(this.sessionData)
                new File('build/' + context.pathParam('name') + '.data.json').write(this.sessionData.toString())
                this.testSessionName = context.pathParam("name")
                this.sessionData = []
                //this.clients = []
                context.result("OK")
            })
            .get("/api/session/{name}", context -> {
                context.json(new JsonSlurper().parse(
                    new File('build/' + context.pathParam("name") + '.data.json')))
            })
            .get("/api/sessions", context -> {
                def tests = []
                new File("build/").listFiles().each{
                    if(it.getName().endsWith('.data.json')){
                        tests.add(it.getName().split('.data')[0])
                    }
                }
                context.json(JsonOutput.toJson(tests))
            })
            .get("/api/session/{name}/frame/thumbs", context -> {
                def list = []
                def dir = new File("build/${context.pathParam('name')}")
                dir.eachFileRecurse (FileType.FILES) { file ->
                    if(file.name.startsWith('thumbnail_')){
                        list << file.name
                    }
                }
                context.json(JsonOutput.toJson(list.sort()))
            })
            .get("/api/session/{name}/frame/{file}", context -> {
                def file = new File("build/${context.pathParam('name')}/${context.pathParam('file')}").bytes
                context.result(file)
            })
            .get("/session/{name}", context -> {
                context.render(
                  "/templates/thymeleaf/session.html",
                  ["sessionName":"Test Session Dashboard - " + context.pathParam("name")])
            })
            .get("/live", context -> {
                context.render("/templates/thymeleaf/live.html")
            })
            .get("/api/device/cpu_usage", context -> context.result(this.deviceCpuUsage))
            .ws("/{path}", ws -> {
                ws.onConnect(context -> {
                    if(context.pathParam("path").equals('data')){
                      this.clients.add(context)
                      this.sessionData.each {
                          try{
                              context.send(it)
                          }catch(Exception e){
                          }
                      }
                    }
                    if(context.pathParam("path").equals('frame')){
                      this.frameClients.add(context)
                    }
                })
                ws.onClose(context -> {
                    if(this.clients.contains(context)){
                      try{
                        this.clients.remove(this.clients.findIndexOf{it == context})
                      }catch(Exception e){}
                      try{
                        this.frameClients.remove(this.frameClients.findIndexOf{it == context})
                      }catch(Exception e){}
                    }
                })
                ws.onMessage(context -> {
                      println("Recieved.. ")
                    if(context.pathParam('path').equals('data')){
                      this.sessionData.add(context.message())
                      this.clients.each {
                          try{
                              it.send(context.message())
                          }catch(Exception e){
                              this.clients.remove(it)
                          }
                      }
                    }
                    if(context.pathParam('path').equals('frame')){
                      this.frameClients.each {
                          try{
                              it.send(context.message())
                          }catch(Exception e){
                              this.frameClients.remove(it)
                          }
                      }
                    }
                })
            })
            .start(4500)
        }
        sleep(2000);
    }

    public static ReportServer start(){
        return new ReportServer()
    }

    public void quit(){
        this.server.stop()
    }
}
