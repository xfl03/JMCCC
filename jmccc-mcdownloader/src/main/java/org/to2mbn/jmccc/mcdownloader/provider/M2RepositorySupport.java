package org.to2mbn.jmccc.mcdownloader.provider;

import java.io.CharArrayReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.to2mbn.jmccc.mcdownloader.download.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class M2RepositorySupport {

	public static CombinedDownloadTask<String> snapshotPostfix(String groupId, String artifactId, String version, String repo) {
		if (!isSnapshotVersion(version)) {
			throw new IllegalArgumentException("Not a snapshot version: " + version);
		}
		final String url = repo + groupId.replace('.', '/') + '/' + artifactId + '/' + version + "/maven-metadata.xml";
		return CombinedDownloadTask.single(new MemoryDownloadTask(url)).andThen(new ResultProcessor<byte[], String>() {

			@Override
			public String process(byte[] arg) throws Exception {
				String str = new String(arg, "UTF-8");
				try {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new CharArrayReader(str.toCharArray())));
					XPath xpath = XPathFactory.newInstance().newXPath();
					String timestamp = xpath.evaluate("metadata/versioning/snapshot/timestamp", doc);
					String buildNumber = xpath.evaluate("metadata/versioning/snapshot/buildNumber", doc);
					if (timestamp == null || buildNumber == null) {
						throw new IllegalArgumentException("Missing timestamp/buildNumber");
					}
					return timestamp + '-' + buildNumber;
				} catch (SAXException | XPathExpressionException | ParserConfigurationException | IllegalArgumentException e) {
					throw new IllegalArgumentException("Couldn't parse [" + url + "]\n" + str, e);
				}
			}
		});
	}

	public static boolean isSnapshotVersion(String version) {
		return version.endsWith("-SNAPSHOT");
	}

	public static String fillInSnapshotPostfix(String snapshotVersion, String postfix) {
		if (!isSnapshotVersion(snapshotVersion)) {
			throw new IllegalArgumentException("Not a snapshot version: " + snapshotVersion);
		}
		return snapshotVersion.substring(0, snapshotVersion.length() - "SNAPSHOT".length()) + postfix;
	}

	public static String toPath(String g, String a, String v, String type) {
		return g.replace('.', '/') + '/' + a + '/' + v + '/' + a + '-' + v + type;
	}

	public static String toPath(String g, String a, String v, String snapshotPostfix, String type) {
		return g.replace('.', '/') + '/' + a + '/' + v + '/' + a + '-' + fillInSnapshotPostfix(v, snapshotPostfix) + type;
	}

	private M2RepositorySupport() {
	}

}
