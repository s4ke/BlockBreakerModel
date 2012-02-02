package de.hotware.blockbreaker.util.misc;

import java.io.Closeable;
import java.io.IOException;

public class StreamUtil {

	public static void closeQuietly(Closeable pCloseable) {
		if(pCloseable != null) {
			try {
				pCloseable.close();
			} catch (IOException e) {
				
			}
		}
	}
	
}
