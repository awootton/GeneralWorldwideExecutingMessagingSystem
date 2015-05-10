package gwems;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.gwems.servers.Global;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class JsType {

	public static Logger logger = Logger.getLogger(JsType.class);

	private static final ObjectMapper MAPPERjst = new ObjectMapper();
	static {

		MAPPERjst.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		MAPPERjst.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
	}

	// custom
	@SuppressWarnings("serial")
	public static class MySerializer extends StdSerializer<Object> {

		public MySerializer() {
			super(Object.class);
		}

		@Override
		public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
			serialize(value, gen, serializers);
		}

		@Override
		public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
			// System.out.println(" kkkkkk  serialize val = " + value);
			if (value instanceof String) {
				jgen.writeString("" + value);
			} else if (value instanceof Runnable) {
				jgen.writeObject(value);
			} else { // without types mapped
				String tmp = MAPPERjst.writeValueAsString(value);
				jgen.writeRawValue(tmp);
			}
			return;
		}
	}

	@SuppressWarnings("serial")
	public static class MyDeserializer extends StdDeserializer<Object> {

		public MyDeserializer() {
			super(Object.class);
		}

		@Override
		public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
			return deserialize(jp, ctxt);
		}

		@Override
		public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			Object res = new Object();
			JsonToken token = p.getCurrentToken();
			if (token == JsonToken.VALUE_STRING) {
				res = p.getText();
			} else {
				// else it should start with START_OBJECT
				if (token == JsonToken.START_OBJECT) {
					TreeNode tree = p.readValueAsTree();
					res = tree;
					if (tree.get("@") != null) {
						// want to serialize normally ??
						// Object obj = tree.traverse().readValueAs(Runnable.class);
						// Object obj = p.readValueAs(Runnable.class);
						// fuck
						// the hard way
						String sss = MAPPERjst.writeValueAsString(tree);
						Object obj = Global.deserialize(sss);
						res = obj;
					}
				} else {
					// an Array?? use logger, not syso
					logger.warn("Found unknown token = " + token);
					TreeNode tree = p.readValueAsTree();
					res = tree;
				}
			}
			return res;
		}
	}

}
