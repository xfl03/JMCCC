package jmccc.cli.launch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jmccc.microsoft.entity.AuthenticationToken;
import org.to2mbn.jmccc.option.MinecraftDirectory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CliConfig {
    public AuthenticationToken token;

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
