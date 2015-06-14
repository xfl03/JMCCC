package com.darkyoooooo.jmccc.launch;

import lombok.Getter;

public class WindowSize {
	@Getter private boolean isFullSize;
	@Getter private int height, width;
	
	public WindowSize(boolean isFullSize) {
		this.isFullSize = isFullSize;
		this.height = this.width = 0;
	}
	
	public WindowSize(int height, int width) {
		this.isFullSize = false;
		this.height = height;
		this.width = width;
	}
}
