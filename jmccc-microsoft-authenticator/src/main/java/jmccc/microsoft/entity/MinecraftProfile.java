package jmccc.microsoft.entity;

import com.google.gson.JsonObject;

import java.util.List;

public class MinecraftProfile {
    public String id;
    public String name;
    public List<MinecraftTexture> skins;
    public List<MinecraftTexture> capes;
    public JsonObject profileActions;
}
