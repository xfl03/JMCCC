package com.github.to2mbn.jmccc.ext;

public interface IGameListener {
    void onLog(String log);
    void onErrorLog(String log);
    void onExit(int code);
}
