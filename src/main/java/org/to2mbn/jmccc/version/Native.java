package org.to2mbn.jmccc.version;

import java.util.Objects;
import java.util.Set;

public class Native extends Library {

	private static final long serialVersionUID = 1L;

	private String arch;
	private Set<String> extractExcludes;

	/**
	 * Creates a native.
	 * 
	 * @param domain the domain of the native
	 * @param name the name of the native
	 * @param version the version of the native
	 * @param downloadInfo the library download info, can be null
	 * @param arch the arch of the native
	 * @param extractExcludes the extract excludes list of the native, null if no excludes
	 * @throws NullPointerException if <code>domain==null||name==null||version==null||arch==null</code>
	 */
	public Native(String domain, String name, String version, LibraryInfo downloadInfo, String arch, Set<String> extractExcludes) {
		this(domain, name, version, downloadInfo, arch, extractExcludes, null, null);
	}

	/**
	 * Creates a native with the custom download url and checksums.
	 * 
	 * @param domain the domain of the native
	 * @param name the name of the native
	 * @param version the version of the native
	 * @param downloadInfo the library download info, can be null
	 * @param arch the arch of the native
	 * @param extractExcludes the extract excludes list of the native, null if no excludes
	 * @param customUrl the custom maven repository url
	 * @param checksums the checksums
	 * @throws NullPointerException if <code>domain==null||name==null||version==null||arch==null</code>
	 * @deprecated <code>customUrl</code> and <code>checksums</code> may be removed in future versions
	 */
	@Deprecated
	public Native(String domain, String name, String version, LibraryInfo downloadInfo, String arch, Set<String> extractExcludes, String customUrl, String[] checksums) {
		super(domain, name, version, downloadInfo, customUrl, checksums);
		Objects.requireNonNull(arch);
		this.arch = arch;
		this.extractExcludes = extractExcludes;
	}

	/**
	 * Gets the arch of the native
	 * 
	 * @return the arch of the native
	 */
	public String getArch() {
		return arch;
	}

	/**
	 * Gets the extract excludes list of the native, null if no excludes.
	 * 
	 * @return the extract excludes list of the native, null if no excludes
	 */
	public Set<String> getExtractExcludes() {
		return extractExcludes;
	}

	@Override
	public String toString() {
		return super.toString() + "@" + arch;
	}

	@Override
	public String getPath() {
		return getDomain().replace('.', '/') + "/" + getName() + "/" + getVersion() + "/" + getName() + "-" + getVersion() + "-" + arch + ".jar";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Native && super.equals(obj)) {
			Native another = (Native) obj;
			return arch.equals(another.arch) && Objects.equals(extractExcludes, another.extractExcludes);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), arch, extractExcludes);
	}
}
