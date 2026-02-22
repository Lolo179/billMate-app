start cmd /k "set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot && set PATH=%JAVA_HOME%\bin;%PATH% && cd ../billing-service && mvn clean install"
start cmd /k "set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot && set PATH=%JAVA_HOME%\bin;%PATH% && cd ../auth-service && mvn clean install"
start cmd /k "set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot && set PATH=%JAVA_HOME%\bin;%PATH% && cd ../api-gateway && mvn clean install"
start cmd /k "set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot && set PATH=%JAVA_HOME%\bin;%PATH% && cd ../frontend-service && mvn clean install"
