package org.gwems.servers;

import java.io.FilePermission;
import java.security.Permission;

public class MySecurityManager extends SecurityManager {

	public MySecurityManager() {
		super();
	}

	@Override
	public void checkPermission(Permission perm) {

		if (perm instanceof FilePermission) {
			FilePermission fp = (FilePermission) perm;
			String aaa = fp.getActions();
			String nnn = fp.getName();
			if (aaa.equals("read") && nnn.endsWith("org/gwems/servers/ExecutionContext.class")) {
				// we can allow a read 
				return;
			}
		}
		// it recurses on getContext! and blows up!!
		if (Global.getContext().isJs) {
			super.checkPermission(perm);
		}
	}

	@Override
	public void checkPermission(Permission perm, Object context) {
		if (Global.getContext().isJs) {
			super.checkPermission(perm, context);
		}
	}

}
