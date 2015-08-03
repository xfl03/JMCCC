package com.darkyoooooo.jmccc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.darkyoooooo.jmccc.auth.AuthInfo;
import com.darkyoooooo.jmccc.ext.Reporter;
import com.darkyoooooo.jmccc.launch.ErrorType;
import com.darkyoooooo.jmccc.launch.LaunchArgument;
import com.darkyoooooo.jmccc.launch.LaunchOption;
import com.darkyoooooo.jmccc.launch.LaunchResult;
import com.darkyoooooo.jmccc.util.Utils;
import com.darkyoooooo.jmccc.version.Library;
import com.darkyoooooo.jmccc.version.Native;
import com.darkyoooooo.jmccc.version.VersionsHandler;

public class Jmccc {
	public static final List<String> ADV_ARGS = new ArrayList<String>();
	public static final List<Library> MISSING_LIBRARIES = new ArrayList<Library>();
	public static final List<Native> MISSING_NATIVES = new ArrayList<Native>();
	
	static {
		ADV_ARGS.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
		ADV_ARGS.add("-Dfml.ignorePatchDiscrepancies=true");
	}
	
	private final BaseOptions baseOptions;
	private final VersionsHandler versionsHandler;
	private LaunchResult launchResult = null;
	
	public Jmccc() {
		this(new BaseOptions());
	}
	
	public Jmccc(BaseOptions baseOptions) {
		this.baseOptions = baseOptions;
		this.versionsHandler = new VersionsHandler(this.baseOptions.gameRoot);
	}
	
	public boolean hasMissingLibraries() {
		return !MISSING_LIBRARIES.isEmpty();
	}
	
	public boolean hasMissingNatives() {
		return !MISSING_NATIVES.isEmpty();
	}

	public LaunchResult launchGame(final LaunchArgument arg) {
		if(this.hasMissingLibraries() || this.hasMissingNatives()) {
			return new LaunchResult(false, ErrorType.DEPENDS_MISSING_ERROR, "library文件或native文件缺失", null);
		}
		if(arg != null && this.launchResult != null && this.launchResult.succeed()) {
			try {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Reporter.report(Jmccc.this, arg);
						} catch(Exception e) {e.printStackTrace();}
					}
				}).start();
				Runtime.getRuntime().exec(arg.toString(), null, new File(this.baseOptions.gameRoot));
			} catch (Exception e) {
				this.launchResult = new LaunchResult(false, ErrorType.HANDLE_ERROR, "启动游戏进程时出错", e);
		    }
		}
		return this.launchResult;
	}
	
	public LaunchArgument generateLaunchArgs(LaunchOption option) {
		return this.generateLaunchArgs(option, null);
	}
	
	public LaunchArgument generateLaunchArgs(LaunchOption option, AuthInfo authInfo) {
		if(authInfo == null) {
			authInfo = option.getAuthenticator().run();
		}
		if(authInfo.getError() != null && !authInfo.getError().isEmpty()) {
			this.launchResult = new LaunchResult(false, ErrorType.BAD_LOGIN, authInfo.getError(), null);
			return null;
		}
		for(String path : Utils.resolveRealNativePaths(this, option.getVersion().getNatives())) {
			try {
				Utils.uncompressZipFile(new File(path), this.baseOptions.getGameRoot() + "/natives");
			} catch (IOException e) {
				this.launchResult = new LaunchResult(false, ErrorType.UNCOMPRESS_ERROR, String.format("解压\'%s\'时出现错误", path), e);
				return null;
			}
		}
		Map<String, String> tokens = new HashMap<String, String>();
		tokens.put("auth_access_token", authInfo.getAccessToken());
		tokens.put("auth_session", authInfo.getAccessToken());
		tokens.put("auth_player_name", authInfo.getDisplayName());
		tokens.put("version_name", option.getVersion().getId());
		tokens.put("game_directory", ".");
		tokens.put("assets_root", "assets");
		tokens.put("assets_index_name", option.getVersion().getAssets());
		tokens.put("auth_uuid", authInfo.getUuid());
		tokens.put("user_type", authInfo.getUserType());
		tokens.put("user_properties", authInfo.getProperties());
		this.launchResult = new LaunchResult(true, ErrorType.NONE, null);
		return new LaunchArgument(
				this,
		    	option,
		    	tokens,
		    	ADV_ARGS,
			    !System.getProperty("java.version").contains("1.9."),
		    	Utils.resolveRealLibPaths(this, option.getVersion().getLibraries()),
		    	Utils.resolvePath(this.baseOptions.getGameRoot() + "/natives")
		);
	}

	public BaseOptions getBaseOptions() {
		return this.baseOptions;
	}

	public VersionsHandler getVersionsHandler() {
		return this.versionsHandler;
	}
	
	public static class BaseOptions {
		private final String gameRoot, javaPath;
		
		public BaseOptions(String gameRoot, String javaPath) {
			this.gameRoot = gameRoot;
			this.javaPath = javaPath;
		}
		
		public BaseOptions(String gameRoot) {
			this(gameRoot, Utils.getJavaPath());
		}
		
		public BaseOptions() {
			this(Utils.resolvePath(".minecraft"), Utils.getJavaPath());
		}
		
		public String getGameRoot() {
			return this.gameRoot;
		}
		
		public String getJavaPath() {
			return this.javaPath;
		}
	}
}
