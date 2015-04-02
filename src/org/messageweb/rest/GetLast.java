package org.messageweb.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.messageweb.dynamo.DynamoHelper;
import org.messageweb.dynamo.LastTimeItem;

import provided.examples.WorkRequest;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

/**
 * An example of using DynamoDB and also S3
 * 
 * @author awootton
 *
 */
@WebServlet(urlPatterns = { "/getLast" })
public class GetLast extends HttpServlet {

	private static Logger logger = Logger.getLogger(GetLast.class);

	private static final long serialVersionUID = 1L;
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	public GetLast() {
	}

	/**
	 * A client to use to access Amazon S3. Pulls credentials from the {@code AwsCredentials.properties} file if found on the classpath, otherwise will attempt
	 * to obtain credentials based on the IAM Instance Profile associated with the EC2 instance on which it is run.
	 */
	private final AmazonS3Client s3 = new AmazonS3Client(new AWSCredentialsProviderChain(new InstanceProfileCredentialsProvider(),
			new ClasspathPropertiesFileCredentialsProvider()));

	/**
	 * This method is invoked to handle POST requests from the local SQS daemon when a work item is pulled off of the queue. The body of the request contains
	 * the message pulled off the queue. Delete me.
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		try {

			logger.debug("post");

			// Parse the work to be done from the POST request body.

			WorkRequest workRequest = WorkRequest.fromJson(request.getInputStream());

			// Simulate doing some work.

			Thread.sleep(10 * 1000);

			// Write the "result" of the work into Amazon S3.

			byte[] message = workRequest.getMessage().getBytes(UTF_8);

			s3.putObject(workRequest.getBucket(), workRequest.getKey(), new ByteArrayInputStream(message), new ObjectMetadata());

			// Signal to beanstalk that processing was successful so this work
			// item should not be retried.

			response.setStatus(200);

		} catch (RuntimeException | InterruptedException exception) {

			// Signal to beanstalk that something went wrong while processing
			// the request. The work request will be retried several times in
			// case the failure was transient (eg a temporary network issue
			// when writing to Amazon S3).

			response.setStatus(500);
			try (PrintWriter writer = new PrintWriter(response.getOutputStream())) {
				exception.printStackTrace(writer);
			}
		}
	}

	/**
	 * An Ajax test and a DynamoDb test
	 * 
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getParameter("path");//
		System.out.println("Str is " + path);

//		AWSCredentialsProviderChain chain = new AWSCredentialsProviderChain(new InstanceProfileCredentialsProvider(),
//				new ClasspathPropertiesFileCredentialsProvider());
//
//		chain = new DefaultAWSCredentialsProviderChain();
//
//		AmazonDynamoDBClient dynamo = new AmazonDynamoDBClient(chain);
//
//		dynamo.setRegion(Region.getRegion(Regions.US_WEST_2));
//
//		DynamoDBMapper mapper = new DynamoDBMapper(dynamo);
		
		DynamoHelper helper = new  DynamoHelper();
		DynamoDBMapper mapper = helper.getMapper();

		String last = "none";

		LastTimeItem b = mapper.load(new LastTimeItem("GetLastUniversalKeyString"));
		if (b != null) {
			System.out.println(b.getWhen());
			last = b.getWhen();
		} else
			b = new LastTimeItem("GetLastUniversalKeyString");
		b.setWhen("" + new Date());
		mapper.save(b);

		response.getWriter().write(last);
	}

}