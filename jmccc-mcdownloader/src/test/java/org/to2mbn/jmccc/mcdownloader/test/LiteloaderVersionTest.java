package org.to2mbn.jmccc.mcdownloader.test;

import org.junit.Test;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.provider.liteloader.LiteloaderVersion;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class LiteloaderVersionTest {

    @Test
    public void testSerialization() throws ClassNotFoundException, IOException {
        Set<JSONObject> libs = new LinkedHashSet<JSONObject>();
        JSONObject lib;

        lib = new JSONObject();
        lib.put("name", "net.minecraft:launchwrapper:1.11");
        libs.add(lib);

        lib = new JSONObject();
        lib.put("name", "org.spongepowered:mixin:0.5.5-SNAPSHOT");
        lib.put("url", "https://repo.spongepowered.org/maven/");
        libs.add(lib);

        SerializationTestUtils.serializeAndDeserialize(
                new LiteloaderVersion("1.9", "1.9", "tweakClass", "repoUrl", libs));
    }

    @Test
    public void testNotEq() {
        Set<JSONObject> libsA = new LinkedHashSet<JSONObject>();
        JSONObject lib;

        lib = new JSONObject();
        lib.put("name", "net.minecraft:launchwrapper:1.11");
        libsA.add(lib);

        lib = new JSONObject();
        lib.put("name", "org.spongepowered:mixin:0.5.5-SNAPSHOT");
        lib.put("url", "https://repo.spongepowered.org/maven/");
        libsA.add(lib);

        Set<JSONObject> libsB = new LinkedHashSet<JSONObject>();

        lib = new JSONObject();
        lib.put("name", "net.minecraft:launchwrapper:1.11");
        libsB.add(lib);

        lib = new JSONObject();
        lib.put("name", "org.spongepowered:mixin:0.5.5-SNAPSHOT");
        libsB.add(lib);

        assertEquals(false, new LiteloaderVersion("1.9", "1.9", null, null, libsA).equals(new LiteloaderVersion("1.9", "1.9", null, null, libsB)));
        assertEquals(false, new LiteloaderVersion("1.9", "1.9", null, null, libsB).equals(new LiteloaderVersion("1.9", "1.9", null, null, libsA)));
    }

    @Test
    public void testEq() {
        Set<JSONObject> libsA = new LinkedHashSet<JSONObject>();
        JSONObject lib;

        lib = new JSONObject();
        lib.put("name", "net.minecraft:launchwrapper:1.11");
        libsA.add(lib);

        lib = new JSONObject();
        lib.put("name", "org.spongepowered:mixin:0.5.5-SNAPSHOT");
        lib.put("url", "https://repo.spongepowered.org/maven/");
        libsA.add(lib);

        Set<JSONObject> libsB = new LinkedHashSet<JSONObject>();

        lib = new JSONObject();
        lib.put("name", "net.minecraft:launchwrapper:1.11");
        libsB.add(lib);

        lib = new JSONObject();
        lib.put("name", "org.spongepowered:mixin:0.5.5-SNAPSHOT");
        lib.put("url", "https://repo.spongepowered.org/maven/");
        libsB.add(lib);

        assertEquals(true, new LiteloaderVersion("1.9", "1.9", null, null, libsA).equals(new LiteloaderVersion("1.9", "1.9", null, null, libsB)));
        assertEquals(true, new LiteloaderVersion("1.9", "1.9", null, null, libsB).equals(new LiteloaderVersion("1.9", "1.9", null, null, libsA)));
    }

}
