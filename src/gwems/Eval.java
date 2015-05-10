package gwems;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/** DELETEME 
 * A thing that can be either a runnable. or a string , or nested maps
 * 
 * @author awootton
 *
 */
public class Eval {

	public Object msg = "none";

	public static final Eval dummy = new Eval();

	public Eval() {

	}

	public Eval(String m) {
		msg = m;
	}

	public Eval(Runnable m) {
		msg = m;
	}

	public Eval(Map<String, Object> m) {
		msg = m;
	}

	private static final ObjectMapper MAPPERwoAt = new ObjectMapper();
	static {

		MAPPERwoAt.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		MAPPERwoAt.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

	}

	// custom
	@SuppressWarnings("serial")
	public static class MySerializer extends StdSerializer<Eval> {

		public MySerializer() {
			super(Eval.class);
		}

		@Override
		public void serializeWithType(Eval value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
			serialize(value, gen, serializers);
			// Class<?> clz = handledType();
			// if (clz == null) {
			// clz = value.getClass();
			// }
			// throw new UnsupportedOperationException("Type id handling not implemented for type "+clz.getName());
		}

		@Override
		public void serialize(Eval value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
			System.out.println(" eeeeee  serialize val = " + value.msg);
			jgen.writeString((String) value.msg);
			return;

			// jgen.writeStartObject();
			// jgen.writeStringField("@", value.getClass().getName());
			// if (value.msg instanceof String) {
			// jgen.writeString((String) value.msg);
			// } else if (value.msg instanceof Runnable) {
			// jgen.writeObject( value.msg);
			// } else {
			// // write without @ types
			// String tmp = MAPPERwoAt.writeValueAsString(value);
			// jgen.writeRawValue(tmp);
			// }
			// jgen.writeEndObject();
		}
	}

	@SuppressWarnings("serial")
	public static class MyDeserializer extends StdDeserializer<Eval> {

		public MyDeserializer() {
			super(Eval.class);
		}

		@Override
		public Eval deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
			return deserialize(jp, ctxt);
		}

		@Override
		public Eval deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			Eval res = new Eval();
			String fname = "";// p.();
			System.out.println(" eeeeeee  888888888888" + fname);
			if (fname.equals("@")) {
				// it's a runnable
			}

			return res;
		}
	}
}
