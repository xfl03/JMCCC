package jmccc.cli.launch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jmccc.microsoft.entity.MicrosoftSession;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.UUIDUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CliConfig {
    public MicrosoftSession token;
    public String clientId = UUIDUtils.randomUnsignedUuidBase64();

    private static CliConfig instance;
    private static Path configFile;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static CliConfig getConfig() {
        return instance;
    }

    public static void initConfig(MinecraftDirectory dir) throws IOException {
        configFile = dir.get("jmccc-cli-config.json");
        if (Files.exists(configFile)) {
            instance = gson.fromJson(Files.newBufferedReader(configFile), CliConfig.class);
        } else {
            instance = new CliConfig();
        }
    }

    public void writeToFile() throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(configFile)) {
            bw.write(gson.toJson(this));
            bw.flush();
        }
    }
}
