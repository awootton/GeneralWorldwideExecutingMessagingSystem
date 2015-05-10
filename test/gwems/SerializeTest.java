package gwems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.gwems.servers.Global;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

public class SerializeTest {

	String aString = "hello";
	int someInt = 123;

	JsonNode aNode;

	public SerializeTest() {
		try {
			aNode = MAPPER.readTree("{\"key\":\"val\"}");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void test(Push2Client tmp) throws IOException {

		String str = Global.serialize(tmp);
		System.out.println(str);
		Push2Client got = (Push2Client) Global.deserialize(str);

		if (got.msg instanceof String || got.msg instanceof Runnable)
			Assert.assertEquals(tmp.msg, got.msg);

		String str2 = Global.serialize(got);

		Assert.assertEquals(str, str2);
	}

	@Test
	public void p2c() throws IOException {

		test(new Push2Client("none"));// a string

		test(new Push2Client(new Js()));// a runnable

		Push2Client p2ctest = new Push2Client();

		Map<String, Object> mnode = new HashMap<String, Object>();
		Map<String, Object> posn = new HashMap<String, Object>();
		posn.put("x", 1.0);
		posn.put("y", 123);
		posn.put("z", -1.0);
		mnode.put("position", posn);
		mnode.put("list", new ArrayList<Object>());
		p2ctest.msg = mnode;

		String json = Global.serializePretty(p2ctest);
		System.out.println(json);

		Object obj = Global.deserialize(json);
		System.out.println(obj);

		test(p2ctest);// a tree and array thing

	}

	// same as Global.java
	private static final ObjectMapper MAPPER = new ObjectMapper();
	static {
		MAPPER.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		MAPPER.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, "@");
	}

	public static void main(String[] args) throws IOException {

		SerializeTest tst = new SerializeTest();

		tst.p2c();

		// String str = MAPPER.writeValueAsString(tst);
		// System.out.println(str);// {"@":"org.gwems.util.SerializeTest","aString":"hello","someInt":123}
		//
		// Object val = MAPPER.readValue(str, Object.class);
		//
		// System.out.println(val);
		// System.out.println(MAPPER.writeValueAsString(val));
		//
		// tst = new SerializeTest();
		// tst.aNode = MAPPER.readTree("just string");

	}

}
