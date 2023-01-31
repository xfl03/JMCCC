package org.to2mbn.jmccc.version.parsing;

import org.to2mbn.jmccc.util.Arch;
import org.to2mbn.jmccc.util.Platform;

import java.util.Objects;

public class PlatformDescription {

    private Platform platform;
    private String version;
    private Arch arch;
    public PlatformDescription(Platform platform, String version, Arch arch) {
        this.platform = Objects.requireNonNull(platform);
        this.version = version;
        this.arch = Objects.requireNonNull(arch);
    }

    public static PlatformDescription current() {
        return new PlatformDescription(Platform.CURRENT, System.getProperty("os.version"), Arch.CURRENT);
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getVersion() {
        return version;
    }

    public Arch getArch() {
        return arch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, version, arch);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PlatformDescription) {
            PlatformDescription another = (PlatformDescription) obj;
            return platform == another.platform
                    && Objects.equals(version, another.version)
                    && Objects.equals(arch, another.arch);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("PlatformDescription [platform=%s, version=%s, arch=%s]", platform, version, arch);
    }

}
