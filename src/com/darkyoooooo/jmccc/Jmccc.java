package com.darkyoooooo.jmccc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import com.darkyoooooo.jmccc.auth.AuthInfo;
import com.darkyoooooo.jmccc.launch.ErrorType;
import com.darkyoooooo.jmccc.launch.LaunchArgument;
import com.darkyoooooo.jmccc.launch.LaunchOption;
import com.darkyoooooo.jmccc.launch.LaunchResult;
import com.darkyoooooo.jmccc.process.GameProcessListener;
import com.darkyoooooo.jmccc.process.IGameListener;
import com.darkyoooooo.jmccc.util.FilePathResolver;
import com.darkyoooooo.jmccc.util.Utils;
import com.darkyoooooo.jmccc.version.VersionsHandler;

public class Jmccc {
	public static final String VERSION = "1.0.2";
	public static final List<String> DEFAULT_ADV_ARGS = new ArrayList<String>();
	static {
		DEFAULT_ADV_ARGS.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
		DEFAULT_ADV_ARGS.add("-Dfml.ignorePatchDiscrepancies=true");
	}
	
	@Getter private final BaseOptions baseOptions;
	private LaunchResult launchResult = null;
	private GameProcessListener processListener = null;
	
	public Jmccc(BaseOptions baseOptions) {
		this.baseOptions = baseOptions;
	}
	
	public void addGameListener(IGameListener listener) {
		GameProcessListener.addGameListener(listener);
	}
	
	public LaunchResult launchGame(LaunchOption option) {
		LaunchArgument arg = this.genLaunchArgs(option);
		if(arg != null && this.launchResult != null && this.launchResult.isSucceed()) {
			try {
				Process process = Runtime.getRuntime().exec(arg.toString(), null, new File(this.baseOptions.gameRoot));
				processListener = new GameProcessListener();
				processListener.startMonitor(process);
			} catch (IOException e) {
				this.launchResult = new LaunchResult(false, ErrorType.HANDLE_ERROR, "启动游戏进程时出错");
		    }
		}
		return this.launchResult;
	}
	
	public boolean stopGameProcess() {
		if(this.processListener == null) {
			return false;
		}
		this.processListener.stopProcess();
		return true;
	}
	
	private LaunchArgument genLaunchArgs(LaunchOption option) {
		if(option == null) {
			throw new NullPointerException();
		}
		
		AuthInfo authInfo = option.getAuthenticator().run();
		if(authInfo.getError() != null && !authInfo.getError().isEmpty()) {
			this.launchResult = new LaunchResult(false, ErrorType.BAD_LOGIN, authInfo.getError());
			return null;
		}
		
		for(String path : FilePathResolver.resolveRealNativePaths(this, option.getVersion().getNatives())) {
			try {
				Utils.uncompressZipFile(new File(path), this.baseOptions.getGameRoot() + "/natives");
			} catch (IOException e) {
				this.launchResult = new LaunchResult(false, ErrorType.UNCOMPRESS_ERROR, String.format("解压%s时出现错误", path));
				return null;
			}
		}
		
		Map<String, String> tokens = new HashMap<String, String>();
		tokens.put("auth_access_token", authInfo.getAccessToken());
		tokens.put("auth_session", authInfo.getAccessToken());
		tokens.put("auth_player_name", authInfo.getDisplayName());
		tokens.put("version_name", option.getVersion().getId());
		tokens.put("game_directory", ".");
		tokens.put("game_assets", "assets");
		tokens.put("assets_root", "assets");
		tokens.put("assets_index_name", option.getVersion().getAssets());
		tokens.put("auth_uuid", authInfo.getUuid());
		tokens.put("user_type", authInfo.getUserType());
		tokens.put("user_properties", authInfo.getProperties());
		
		this.launchResult = new LaunchResult(true, ErrorType.NONE);
		
		return new LaunchArgument(
			this,
			option,
			tokens,
			DEFAULT_ADV_ARGS,
			true,
			FilePathResolver.resolveRealLibPaths(this, option.getVersion().getLibraries()),
			Utils.resolvePath(this.baseOptions.getGameRoot() + "/natives")
		);
	}
	
	public static class BaseOptions {
		@Getter private final String gameRoot, javaPath;
		@Getter private final VersionsHandler versionsHandler;
		
		public BaseOptions(String gameRoot, String javaPath) {
			if(gameRoot == null || javaPath == null) {
				throw new NullPointerException();
			}
			this.gameRoot = gameRoot;
			this.javaPath = javaPath;
			this.versionsHandler = new VersionsHandler(gameRoot);
		}
		
		public BaseOptions(String gameRoot) {
			this(gameRoot, Utils.getJavaPath());
		}
		
		public BaseOptions() {
			this(".minecraft", Utils.getJavaPath());
		}
	}
}
