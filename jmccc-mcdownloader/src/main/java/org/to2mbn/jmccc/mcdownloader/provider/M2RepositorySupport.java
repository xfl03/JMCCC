package org.to2mbn.jmccc.mcdownloader.provider;

import java.io.CharArrayReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.to2mbn.jmccc.mcdownloader.download.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public final class M2RepositorySupport {

	public static CombinedDownloadTask<String> snapshotPostfix(String groupId, String artifactId, String version, String repo) {
		if (!isSnapshotVersion(version)) {
			throw new IllegalArgumentException("Not a snapshot version: " + version);
		}
		return CombinedDownloadTask.single(new MemoryDownloadTask(repo + groupId.replace('.', '/') + '/' + artifactId + '/' + version + "/maven-metadata.xml")).andThen(new ResultProcessor<byte[], String>() {

			@Override
			public String process(byte[] arg) throws Exception {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new CharArrayReader(new String(arg, "UTF-8").toCharArray())));
				return (String) XPathFactory.newInstance().newXPath().evaluate("metadata/versioning/snapshot/timestamp", doc, XPathConstants.STRING)
						+ '-'
						+ (String) XPathFactory.newInstance().newXPath().evaluate("metadata/versioning/snapshot/buildNumber", doc, XPathConstants.STRING);
			}
		});
	}

	public static boolean isSnapshotVersion(String version) {
		return version.endsWith("-SNAPSHOT");
	}

	public static String fillInTimestamp(String snapshotVersion, String timestamp) {
		if (!isSnapshotVersion(snapshotVersion)) {
			throw new IllegalArgumentException("Not a snapshot version: " + snapshotVersion);
		}
		return snapshotVersion.substring(0, snapshotVersion.length() - "SNAPSHOT".length()) + timestamp;
	}

	private M2RepositorySupport() {
	}

}
