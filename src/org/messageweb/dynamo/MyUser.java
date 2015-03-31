package org.messageweb.dynamo;

public class MyUser extends AtwTableBase {

	public MyUser(String key) {
		super("AllUsers:" + key);
	}

}
