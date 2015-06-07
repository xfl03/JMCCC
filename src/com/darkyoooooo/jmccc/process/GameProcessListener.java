package com.darkyoooooo.jmccc.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GameProcessListener {
	public static final List<IGameListener> LISTENERS = new ArrayList<IGameListener>();
	private Process process;
	
	public Process getProcess() {
		return this.process;
	}
	
	public void monitor(Process process) {
		this.process = process;
		new Thread(new Runnable() {
			@Override
			public void run() {
				GameProcessListener.this.monitorGameLog();
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
	
	private void monitorGameLog() {
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			isr = new InputStreamReader(this.process.getInputStream(), "GBK");
			br = new BufferedReader(isr);
			String log;
		    while((log = br.readLine()) != null && this.process.isAlive()) {
		    	for(IGameListener listener : LISTENERS) {
		    		listener.onLog(log);
		    	}
		    }
		} catch (IOException e) {
		} finally {
			try {
				isr.close();
				br.close();
			} catch (Exception e) {
			}
		}
	}
}
