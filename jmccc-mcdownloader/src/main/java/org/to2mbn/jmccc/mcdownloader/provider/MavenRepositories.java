package org.to2mbn.jmccc.mcdownloader.provider;

import org.to2mbn.jmccc.mcdownloader.download.cache.CacheNames;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.CharArrayReader;

public final class MavenRepositories {

    private MavenRepositories() {
    }

    public static CombinedDownloadTask<String> snapshotPostfix(String groupId, String artifactId, String version, String repo) {
        if (!version.endsWith("-SNAPSHOT")) {
            throw new IllegalArgumentException("Not a snapshot version: " + version);
        }
        final String url = repo + groupId.replace('.', '/') + '/' + artifactId + '/' + version + "/maven-metadata.xml";
        return CombinedDownloadTask.single(
                        new MemoryDownloadTask(url)
                                .cacheable()
                                .cachePool(CacheNames.M2_METADATA))
                .andThen(new ResultProcessor<byte[], String>() {

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
                        } catch (SAXException | XPathExpressionException | ParserConfigurationException |
                                 IllegalArgumentException e) {
                            throw new IllegalArgumentException("Couldn't parse [" + url + "]\n" + str, e);
                        }
                    }
                });
    }

}
