package gwems;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Push to client.
 * 
 * {"@":"gwems.Push2Client","msg":"none"}
 * 
 * 
 * @author awootton
 *
 */

public class Push2Client implements Runnable {

	public static Logger logger = Logger.getLogger(Push2Client.class);

	@JsonSerialize(using = JsType.MySerializer.class)
	@JsonDeserialize(using = JsType.MyDeserializer.class)
	Object msg = "none";// Eval.dummy;// "none";

	/**
	 * Object will need to be serializable by jackson.
	 * 
	 * @param message
	 */
	// public Push2Client(TreeNode message) {
	// super();
	// this.msg = new Eval();
	// this.msg = message;
	// }

	public Push2Client(Runnable message) {
		super();
		this.msg = new Eval();
		this.msg = message;
	}

	public Push2Client(String stringMessage) {
		super();
		this.msg = new Eval();
		this.msg = stringMessage;
	}

	public Push2Client() {// for jackson
	}

	@Override
	public void run() {
		// meant to run in a session agent.
		ExecutionContext ec = Global.getContext();
		if (ec.agent.isPresent() && ec.agent.get() instanceof SessionAgent) {
			SessionAgent session = (SessionAgent) ec.agent.get();
			if (logger.isTraceEnabled()) {
				logger.trace("Sending message2client " + msg + " to " + session);
			}
			try {
				session.writeAndFlush(Global.serialize(msg));
			} catch (JsonProcessingException e) {
				logger.error(e);
			}
		} else {
			// what?
			logger.debug("non session message? " + msg + " agent = " + ec.agent);
		}
	}

	private static final ObjectMapper MAPPERwoAt = new ObjectMapper();
	static {

		MAPPERwoAt.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		MAPPERwoAt.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

	}

//	// custom
//	@SuppressWarnings("serial")
//	public static class MySerializer extends StdSerializer<Push2Client> {
//
//		public MySerializer() {
//			super(Push2Client.class);
//		}
//
//		@Override
//		public void serialize(Push2Client value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
//			System.out.println("val = " + value);
//			jgen.writeStartObject();
//			jgen.writeStringField("@", value.getClass().getName());
//			// if (value.msg instanceof Eval) {
//			// jgen.writeStringField("msg", (String) value.msg);
//			// } else
//			if (value.msg instanceof Runnable) {
//				jgen.writeObjectField("msg", value.msg);
//			} else {
//				// write without @ types
//				jgen.writeFieldName("msg");
//				String tmp = MAPPERwoAt.writeValueAsString(value);
//				jgen.writeRawValue(tmp);
//			}
//			jgen.writeEndObject();
//		}
//	}
//
//	@SuppressWarnings("serial")
//	public static class MyDeserializer extends StdDeserializer<Push2Client> {
//
//		public MyDeserializer() {
//			super(Push2Client.class);
//		}
//
//		@Override
//		public Push2Client deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
//			Push2Client res = new Push2Client();
//			String fname = "";// p.();
//			System.out.println("888888888888" + fname);
//			if (fname.equals("@")) {
//				// it's a runnable
//			}
//
//			return res;
//		}
//	}
}
