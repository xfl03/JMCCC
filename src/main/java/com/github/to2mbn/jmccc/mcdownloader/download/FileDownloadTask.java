package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Objects;

/**
 * Describes a download-to-file task.
 * 
 * @author yushijinhun
 */
public class FileDownloadTask extends DownloadTask {

	private File target;

	/**
	 * Creates a FileDownloadTask.
	 * 
	 * @param url the url of the resource to download
	 * @param target where to save the file
	 * @throws NullPointerException if <code>url==null||target==null</code>
	 */
	public FileDownloadTask(URL url, File target) {
		super(url);
		Objects.requireNonNull(target);
		this.target = target;
	}

	/**
	 * Gets the save location.
	 * 
	 * @return the save location
	 */
	public File getTarget() {
		return target;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), target);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (super.equals(obj)) {
			FileDownloadTask another = (FileDownloadTask) obj;
			return target.equals(another.target);
		}
		return false;
	}

	@Override
	public OutputStream openStream() throws IOException {
		return new BufferedOutputStream(new FileOutputStream(target));
	}

}
