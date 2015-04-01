package org.messageweb.dynamo;

import org.apache.log4j.Logger;
import org.messageweb.agents.Agent;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class DynamoHelper {
	
	public static Logger logger = Logger.getLogger(DynamoHelper.class);

	private AmazonDynamoDBClient dynamo;
	private DynamoDBMapper mapper;

	public DynamoHelper() {

		AWSCredentialsProviderChain chain = new AWSCredentialsProviderChain(new InstanceProfileCredentialsProvider(),
				new ClasspathPropertiesFileCredentialsProvider());

		chain = new DefaultAWSCredentialsProviderChain();

		chain = new AWSCredentialsProviderChain(new DefaultAWSCredentialsProviderChain(), new ProfileCredentialsProvider("awootton"));
		
		chain.setReuseLastProvider(true);

		dynamo = new AmazonDynamoDBClient(chain);

		dynamo.setRegion(Region.getRegion(Regions.US_WEST_2));

		mapper = new DynamoDBMapper(dynamo);
		
		//AmazonEC2Client ec2    = new AmazonEC2Client(chain);
		//AmazonS3Client s3     = new AmazonS3Client(chain);
		
	}

	public void save(AtwTableBase object) {
		mapper.save(object);
	}

	public AtwTableBase read(AtwTableBase object) {
		return mapper.load(object);
	}

	public void delete(AtwTableBase object) {
		mapper.delete(object);
	}

	public void save(Agent object) {
		mapper.save(object);
	}

	public Agent read(Agent object) {
		return mapper.load(object);
	}

	public void delete(Agent object) {
		mapper.delete(object);
	}


}
