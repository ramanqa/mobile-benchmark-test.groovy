# Requirements
 - JDk 11 +
 - Gradle
 - Appium server connect to mobile device. Configure appium hoturl in `src/test/resources/appium.config.json`
 - Raspberry pi/linux ssh host. Configure host settings in `src/test/resources/pi.config.json`


# Steps
1. Run Benchmark Server using gradle
   ```
     $> gradle runReportServer
   ```
3. Run test
   ```
     $> gradle test
   ```
 4. Point browser to: http://localhost:4500/live
    
