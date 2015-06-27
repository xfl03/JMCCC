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
import com.darkyoooooo.jmccc.util.Utils;
import com.darkyoooooo.jmccc.version.VersionsHandler;

public class Jmccc {
	public static final String VERSION = "1.0.4";
	public static final List<String> ADV_ARGS = new ArrayList<String>();
	static {
		ADV_ARGS.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
		ADV_ARGS.add("-Dfml.ignorePatchDiscrepancies=true");
	}
	
	@Getter private long launchTime = 0;
	@Getter private final BaseOptions baseOptions;
	@Getter private final VersionsHandler versionsHandler;
	private LaunchResult launchResult = null;
	
	public Jmccc(BaseOptions baseOptions) {
		this.baseOptions = baseOptions;
		this.versionsHandler = new VersionsHandler(this.baseOptions.getGameRoot());
	}
	
	public LaunchResult launchGame(LaunchOption option) {
		long start = System.currentTimeMillis();
		LaunchArgument arg = this.genLaunchArgs(option);
		if(arg != null && this.launchResult != null && this.launchResult.isSucceed()) {
			try {
				Runtime.getRuntime().exec(arg.toString(), null, new File(this.baseOptions.gameRoot));
			} catch (IOException e) {
				this.launchResult = new LaunchResult(false, ErrorType.HANDLE_ERROR, "启动游戏进程时出错", e);
		    }
		}
		long end = System.currentTimeMillis();
		this.launchTime = end - start;
		return this.launchResult;
	}
	
	private LaunchArgument genLaunchArgs(LaunchOption option) {
		if(option == null) {
			throw new NullPointerException();
		}
		
		AuthInfo authInfo = option.getAuthenticator().run();
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
		tokens.put("game_assets", "assets");
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
			true,
			Utils.resolveRealLibPaths(this, option.getVersion().getLibraries()),
			Utils.resolvePath(this.baseOptions.getGameRoot() + "/natives")
		);
	}
	
	public static class BaseOptions {
		@Getter private final String gameRoot, javaPath;
		
		public BaseOptions(String gameRoot, String javaPath) {
			if(gameRoot == null || javaPath == null) {
				throw new NullPointerException();
			}
			this.gameRoot = gameRoot;
			this.javaPath = javaPath;
		}
		
		public BaseOptions(String gameRoot) {
			this(gameRoot, Utils.getJavaPath());
		}
		
		public BaseOptions() {
			this(".minecraft", Utils.getJavaPath());
		}
	}
}
