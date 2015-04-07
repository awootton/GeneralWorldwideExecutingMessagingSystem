package org.gwems;

import org.gwems.servers.Global;
import org.junit.Assert;
import org.junit.Test;
import org.messageweb.dynamo.AnotherAtwTableItem;
import org.messageweb.dynamo.LastTimeItem;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DynamoTests {

	@Test
	public void t1() {

		AWSCredentialsProviderChain chain = new AWSCredentialsProviderChain(new InstanceProfileCredentialsProvider(),
				new ClasspathPropertiesFileCredentialsProvider());

		chain = new DefaultAWSCredentialsProviderChain();

		AmazonDynamoDBClient dynamo = new AmazonDynamoDBClient(chain);

		dynamo.setRegion(Region.getRegion(Regions.US_WEST_2));

		DynamoDBMapper mapper = new DynamoDBMapper(dynamo);

		LastTimeItem item1 = new LastTimeItem("dumyTimeItem8287dhdkdld");
		item1.setWhen("bscdfe");
		mapper.save(item1);

		LastTimeItem item2 = mapper.load(new LastTimeItem("dumyTimeItem8287dhdkdld"));
		
		Assert.assertEquals(item1, item2);

	}
	
	@Test
	public void t2() {
		try {

		AWSCredentialsProviderChain chain = new AWSCredentialsProviderChain(new InstanceProfileCredentialsProvider(),
				new ClasspathPropertiesFileCredentialsProvider());

		chain = new DefaultAWSCredentialsProviderChain();

		AmazonDynamoDBClient dynamo = new AmazonDynamoDBClient(chain);

		dynamo.setRegion(Region.getRegion(Regions.US_WEST_2));

		DynamoDBMapper mapper = new DynamoDBMapper(dynamo);

		AnotherAtwTableItem item1 = new AnotherAtwTableItem("dumyAnTimeItem8287dhdkdld");
		item1.setValue("soflnejbsrsr");
		item1.addName("name1");
		item1.addName("name2");
		item1.addName("name3");
		
		mapper.save(item1);

		AnotherAtwTableItem item2 = mapper.load(new AnotherAtwTableItem("dumyAnTimeItem8287dhdkdld"));

		Assert.assertEquals(item1, item2);
		
		// really? 
	
			String s = Global.serialize(item2);
			System.out.println(s);
		
		
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}


	public static void main(String[] args) {

		DynamoTests test = new DynamoTests();

		test.t1();
		test.t2();

	}

}
