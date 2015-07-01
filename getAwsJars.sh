#!/bin/bash



# yum search java | grep openjdk
# sudo yum install java-1.8.0-openjdk-devel.x86_64


mkdir ../aws
curl http://sdk-for-java.amazonwebservices.com/latest/aws-java-sdk.zip > ../aws/aws-java-sdk.zip

unzip -d ../aws ../aws/aws-java-sdk.zip

