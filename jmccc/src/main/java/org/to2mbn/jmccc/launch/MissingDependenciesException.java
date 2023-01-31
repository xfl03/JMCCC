package org.to2mbn.jmccc.launch;

import org.to2mbn.jmccc.version.Library;

import java.util.Set;

/**
 * Thrown when dependent natives or libraries are missing.
 *
 * @author yushijinhun
 */
public class MissingDependenciesException extends LaunchException {

    private static final long serialVersionUID = 1L;

    private Set<Library> missingLibraries;

    public MissingDependenciesException() {
    }

    public MissingDependenciesException(String message) {
        super(message);
    }

    public MissingDependenciesException(Set<Library> missingLibraries) {
        this(missingLibraries.toString(), missingLibraries);
    }

    public MissingDependenciesException(String message, Set<Library> missingLibraries) {
        super(message);
        this.missingLibraries = missingLibraries;
    }

    public Set<Library> getMissingLibraries() {
        return missingLibraries;
    }

}
