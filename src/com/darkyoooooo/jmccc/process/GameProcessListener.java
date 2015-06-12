package com.darkyoooooo.jmccc.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;
import lombok.Getter;

public class GameProcessListener {
	private static final List<IGameListener> LISTENERS = new ArrayList<IGameListener>();
	@Getter private Process process;
	
	public static void addGameListener(IGameListener listener) {
		if(listener == null) {
			throw new NullPointerException();
		}
		LISTENERS.add(listener);
	}
	
	public void stopProcess() {
		if(this.process != null) {
			process.destroy();
		}
	}
	
	public void startMonitor(Process process) {
		this.process = process;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Process p = GameProcessListener.this.getProcess();
					@Cleanup InputStreamReader isr = new InputStreamReader(p.getInputStream(), "GBK");
					@Cleanup BufferedReader br = new BufferedReader(isr);
					String log;
				    while((log = br.readLine()) != null && p.isAlive()) {
				    	for(IGameListener listener : LISTENERS) {
				    		listener.onLog(log);
				    	}
				    }
				} catch (IOException e) {
				}
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					GameProcessListener.this.process.waitFor();
					int exitCode = GameProcessListener.this.process.exitValue();
					for(IGameListener listener : GameProcessListener.LISTENERS) {
						listener.onExit(exitCode);
					}
				} catch (InterruptedException e) {
				}
			}
		}).start();
	}
}
