package de.hotware.blockbreaker.model.generator;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.hotware.blockbreaker.model.Level;

public class LevelSerializer {

	public static void saveLevel(Level pLevel, String pPath) {
		//write an level without any listeners!
		Level save = pLevel.clone();
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(pPath);
			out = new ObjectOutputStream(fos);
			out.writeObject(save);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeQuietly(fos);
			closeQuietly(out);
		}
	}

	public static Level readLevel(InputStream is) throws ClassNotFoundException{
		ObjectInputStream in = null;
		Level ret = null;
		try {
			in = new ObjectInputStream(is);
			ret = (Level) in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeQuietly(in);
		}
		return ret;
	}
	
	public static void closeQuietly(Closeable pCloseable) {
		if(pCloseable != null) {
			try {
				pCloseable.close();
			} catch (IOException e) {
				
			}
		}
	}
}
