package org.messageweb.experiments;

import io.netty.example.http.file.HttpStaticFileServer;

public final class FileServer {

	// An example of https working - atw

	public static void main(String[] args) throws Exception {

		System.setProperty("ssl", "true");

		HttpStaticFileServer.main(new String[0]);

	}
}