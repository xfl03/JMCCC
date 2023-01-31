package org.to2mbn.jmccc.version.parsing;

import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONException;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.version.*;

import java.util.Set;
import java.util.Stack;

public interface VersionParser {

    /**
     * Parses the DownloadInfo.
     *
     * <pre>
     * {
     * 	"url": "[url]",
     * 	"sha1": "[checksum]",
     * 	"size": [size]
     * }
     * </pre>
     *
     * @param json the json
     * @return the parsed DownloadInfo
     * @throws JSONException if the json is invalid
     */
    DownloadInfo parseDownloadInfo(JSONObject json) throws JSONException;

    /**
     * Parses the AssetIndexInfo.
     * <p>
     * Extends from {@link #parseDownloadInfo(JSONObject)}.
     *
     * <pre>
     * {
     * 	......
     * 	"id": "{id}",
     * 	"totalSize": "[totalSize]"
     * }
     * </pre>
     *
     * @param json the json
     * @return the parsed AssetIndexInfo
     * @throws JSONException if the json is invalid
     */
    AssetIndexInfo parseAssetIndexInfo(JSONObject json) throws JSONException;

    /**
     * Parses the LibraryInfo.
     * <p>
     * Extends from {@link #parseDownloadInfo(JSONObject)}.
     *
     * <pre>
     * {
     * 	......
     * 	"path": "[path]"
     * }
     * </pre>
     *
     * @param json the json
     * @return the parsed LibraryInfo
     * @throws JSONException if the json is invalid
     */
    LibraryInfo parseLibraryInfo(JSONObject json) throws JSONException;

    /**
     * Parses the Library.
     * <p>
     * If the json contains 'rules',
     * {@link #checkAllowed(JSONArray, PlatformDescription)} will be called to
     * check if the library is allowed to load on the platform. If the library
     * is not allowed, this method will return {@code null}<br>
     * If the json contains 'natives', the library will be parsed as a
     * {@link Native}. The classifier will be filled according to 'natives'.<br>
     * If the json contains 'clientreq' and the value is {@code false}, this
     * method will return {@code null}.
     *
     * <pre>
     * {
     * 	"name": "{groupId}:{artifactId}:{version}",
     * 	"natives": {
     * 		"[lower-case platform name]": "[classifier]",
     * 		......
     *    },
     * 	"rules": [
     * 		...... // see {@link #checkAllowed(JSONArray, PlatformDescription)}
     * 	],
     * 	"extract": {
     * 		"exclude": [
     * 			"[extractExcludes]", ......
     * 		]
     *    },
     * 	"downloads": {
     * 		"classifiers": {
     * 			"[classifier]": {
     * 				...... // see {@link #parseDownloadInfo(JSONObject)}
     *            }
     *        },
     * 		"artifact": {
     * 			...... // see {@link #parseLibraryInfo(JSONObject)}
     *        }
     *    },
     * 	"clientreq": [#allow on client?],
     * 	"url": "[customizedUrl]",
     * 	"checksums": [
     * 		"[checksums]", ......
     * 	]
     * }
     * </pre>
     *
     * @param json                the json
     * @param platformDescription the platform
     * @return the parsed Library
     * @throws JSONException if the json is invalid
     */
    Library parseLibrary(JSONObject json, PlatformDescription platformDescription) throws JSONException;

    /**
     * Parses the asset index.
     *
     * <pre>
     * {
     * 	"objects": {
     * 		"{virtualPath}": {
     * 			"hash": "{hash}",
     * 			"size": "{size}"
     *        }, ......
     *    }
     * }
     * </pre>
     *
     * @param json the json
     * @return the parsed assets
     * @throws JSONException if the json is invalid
     */
    Set<Asset> parseAssetIndex(JSONObject json) throws JSONException;

    /**
     * Parses the version hierarchy.
     * <p>
     * At the top of the 'jsons' stack is the root version. At the bottom of the
     * 'jsons' stack is the child version. This method first loads the root
     * version, and then the child version. So the child's data can override its
     * parent's.<br>
     * Assets will be set to 'legacy' if no 'assets' is defined. Legacy will be
     * set to true if 'assets' is 'legacy' or undefined.<br>
     * Libraries and downloads are inheritable. The libraries in child version
     * override those which have the same groupId and artifactId in its parent
     * version. Downloads are the same.
     *
     * @param jsons               the collection of the version jsons
     * @param platformDescription the platform
     * @return the parsed Version
     * @throws JSONException if the json is invalid
     */
    Version parseVersion(Stack<JSONObject> jsons, PlatformDescription platformDescription) throws JSONException;

    /**
     * Checks if the rules match the platform.
     * <p>
     * If 'rules' is empty, this method returns {@code true}.<br>
     * If 'rules' is not empty, this method returns the action of last rule
     * which matches the platform.<br>
     * If none of the rules matches the platform, this method return
     * {@code false}.
     *
     * <pre>
     * [
     *    {
     * 		"action": "# allow/disallow",
     * 		"os": {
     * 			"name": "# lower-case platform name",
     * 			"version": "# regex matches 'os.version'"
     *        }
     *    }, ......
     * ]
     * </pre>
     *
     * @param rules               the rules in json
     * @param platformDescription the platform
     * @return true if it's allowed to load the library
     * @throws JSONException if the json is invalid
     */
    boolean checkAllowed(JSONArray rules, PlatformDescription platformDescription) throws JSONException;

}
