package provided.examples;

import gwems.Push2Client;

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
import org.gwems.servers.ClusterState;
import org.gwems.servers.Global;
import org.messageweb.dynamo.LastTimeItem;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * An example Amazon Elastic Beanstalk Worker Tier application. This example requires a Java 7 (or higher) compiler.
 * 
 * atw - it needs to stay in the default package unless the web.xml is changed.
 * 
 * dynamo = new AmazonDynamoDBClient(credentialsProvider);
 * 
 */
@WebServlet(urlPatterns = { "/Serve" }, loadOnStartup = 1)
public class WorkerServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(WorkerServlet.class);

	private static final long serialVersionUID = 1L;
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	Global global = null;

	public WorkerServlet() {

		logger.debug(" . . . . . . . . . . . . . . . . . . . . . . . . . . . . . Starting");

		GlobalRunner rrr = new GlobalRunner();
		Thread tt = new Thread(rrr);
		tt.start();
		while (global == null) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}

		logger.debug("-* -*  -* -*  -* -*  -* -*  -* -*  -* -*  -* -*  -* -*  -* -*  -* -*  -* -* Started " + global);

		TimePusher pusher = new TimePusher();
		pusher.global = global;
		Thread t = new Thread(pusher);
		t.start();
	}


	private class GlobalRunner implements Runnable {

		@Override
		public void run() {
			ClusterState clusterState = new ClusterState();
			clusterState.rootMode = true;// root mode.
			global = new Global(8081, clusterState);// starts a ws server
			//Main.main(new String[0]);
		}
	}

	// Do this over in js and add it to the db

	private class TimePusher implements Runnable {
		Global global;

		@Override
		public void run() {
			// publish the time every 10 sec.
			long time_10 = System.currentTimeMillis() + 10 * 1000;
			long time_60 = System.currentTimeMillis() + 22 * 1000;
			while (true) {
				long time = System.currentTimeMillis();
				if (time > time_10) {
					time_10 += 10 * 1000;
					global.publish("WWC#TimeEveryTenSeconds", new Push2Client("" + new Date()));
					global.publish("WWC#10secs", new Push2Client("" + new Date()));
					// logger.info("sent time to #TimeEveryTenSeconds");
				}
				if (time > time_60) {
					time_60 += 60 * 1000;
					global.publish("WWC#TimeEveryMinute", new Push2Client("" + new Date()));
					// logger.info("sent time to #TimeEveryMinute");
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * A client to use to access Amazon S3. Pulls credentials from the {@code AwsCredentials.properties} file if found
	 * on the classpath, otherwise will attempt to obtain credentials based on the IAM Instance Profile associated with
	 * the EC2 instance on which it is run.
	 */
	private final AmazonS3Client s3 = new AmazonS3Client(new AWSCredentialsProviderChain(new InstanceProfileCredentialsProvider(),
			new ClasspathPropertiesFileCredentialsProvider()));

	/**
	 * This method is invoked to handle POST requests from the local SQS daemon when a work item is pulled off of the
	 * queue. The body of the request contains the message pulled off the queue.
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		try {

			// Parse the work to be done from the POST request body.

			WorkRequest workRequest = WorkRequest.fromJson(request.getInputStream());

			// Simulate doing some work.

			Thread.sleep(10 * 1000);

			// Write the "result" of the work into Amazon S3.

			byte[] message = workRequest.getMessage().getBytes(UTF_8);

			// don't actually do that ! s3.putObject(workRequest.getBucket(), workRequest.getKey(), new
			// ByteArrayInputStream(message), new ObjectMetadata());

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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getParameter("path");//
		System.out.println("Str is " + path);

		AmazonDynamoDBClient dynamo = new AmazonDynamoDBClient(new AWSCredentialsProviderChain(new InstanceProfileCredentialsProvider(),
				new ClasspathPropertiesFileCredentialsProvider()));

		dynamo.setRegion(Region.getRegion(Regions.US_WEST_2));

		DynamoDBMapper mapper = new DynamoDBMapper(dynamo);

		String last = "none";
		String now = "" + new Date();

		LastTimeItem b = mapper.load(new LastTimeItem("WorkerServletLastKey"));
		if (b != null) {
			System.out.println(b.getWhen());
			last = b.getWhen();
		} else
			b = new LastTimeItem("WorkerServletLastKey");
		b.setWhen("" + new Date());
		mapper.save(b);

		// an example of doing it the hard way.
		// Map<String, AttributeValue> keyMap = new HashMap<String, AttributeValue>();
		// keyMap.put("id", new AttributeValue("dummyKey123"));
		//
		// GetItemResult result = null;
		// try {
		// result = dynamo.getItem("AtwTable2", keyMap);
		// } catch (AmazonServiceException e) {
		// e.printStackTrace();
		// } catch (AmazonClientException e) {
		// e.printStackTrace();
		// }
		// String last = "none";
		// String now = "" + new Date();
		// if (result != null) {
		// AttributeValue aval = result.getItem().get("when");
		// if (aval != null) {
		// last = "" + aval;
		// }
		// }
		// keyMap.put("when", new AttributeValue(now));
		// PutItemResult res;
		// try {
		// res = dynamo.putItem("AtwTable2", keyMap);
		// System.out.println(" got result " + res.toString());
		// } catch (AmazonServiceException e) {
		// e.printStackTrace();
		// } catch (AmazonClientException e) {
		// e.printStackTrace();
		// }

		response.getWriter().println("WorkerServlet static assets path =" + path + " last val was " + last + " current val is " + now);
	}

}
