package com.darkyoooooo.jmccc.ext;

public interface IGameListener {
    void onLog(String log);
    void onErrorLog(String log);
    void onExit(int code);
}
