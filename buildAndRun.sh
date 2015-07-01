#!/bin/bash

echo $CLASSPATH 

classes=

collectClasses() {
	for f in $1; 
	do   echo "Processing $f file.."
		classes=$classes$f
		classes="$classes:"
	done
}

# was /usr/lib/jvm/jre/bin/java -DJDBC_CONNECTION_STRING= -Xms256m -Xmx256m -XX:MaxPermSize=64m 
# -classpath :/usr/share/tomcat8/bin/bootstrap.jar:/usr/share/tomcat8/bin/tomcat-juli.jar:
# /usr/share/java/commons-daemon.jar -Dcatalina.base=/usr/share/tomcat8 #
# -Dcatalina.home=/usr/share/tomcat8 -Djava.awt.headless=true -
# Djava.endorsed.dirs= -Djava.io.tmpdir=/var/cache/tomcat8/temp 
# -Djava.util.logging.config.file=/usr/share/tomcat8/conf/logging.properties 
# -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager 
# org.apache.catalina.startup.Bootstrap start


# http://central.maven.org/maven2/com/amazonaws/aws-java-sdk/1.10.2/aws-java-sdk-1.10.2.jar

if [ -d "/Users/awootton" ]; then
  
	collectClasses "WebContent/WEB-INF/lib/*.jar"
 
	AWS="/Users/awootton/aws-java-sdk/1.9.25" 
  
else

	AWS="../aws/aws-java-sdk-1.10.2"

	collectClasses "WebContent/WEB-INF/lib/*.jar"
	
	collectClasses "/usr/share/java/*.jar"
	
fi

collectClasses "$AWS/lib/*.jar"

collectClasses "$AWS/third-party/commons-logging-1.1.3/*.jar"
 
collectClasses "$AWS/third-party/httpcomponents-client-4.3/*.jar"
 

CLASSPATH=$classes$CLASSPATH
echo $CLASSPATH

echo
echo

command="javac -classpath $CLASSPATH -d build/classes -sourcepath src  src/org/gwems/util/ProductionMain.java src/org/gwems/util/PublishLogAppender.java"   

##echo $command

$command

command="java -Xms2000m -Xmx2000m -XX:MaxPermSize=64m -classpath build/classes:$CLASSPATH org/gwems/util/ProductionMain"

$command



