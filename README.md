# jmccc-jyal-authenticator

### Download
Maven dependency.<br/>
```xml
<dependency>
	<groupId>com.github.to2mbn</groupId>
	<artifactId>jmccc-jyal-authenticator</artifactId>
	<version>1.0</version>
</dependency>
```

### Dependencies
* jmccc
* jyal

### Compile
Require Maven

	mvn clean install

### Usage
For password login:<br/>
```java
new YggdrasilAuthenticator("<email>", "<password>");
```
<p/>
For token login:
<br/>
```java
new YggdrasilTokenAuthenticator(<clientToken>, "<accessToken>");
```
<p/>
`YggdrasilTokenAuthenticator` is serializable. If you want to save the authentication (aka 'remember password'),
just save this YggdrasilTokenAuthenticator object.
We recommend you to use YggdrasilTokenAuthenticator because YggdrasilTokenAuthenticator only saves the access token.
It's much safer.<br/>
You should call `YggdrasilTokenAuthenticator.isValid()` first to check if the access token is valid.
If this method returns false, you should ask the user to login with password again.<br/>
```java
File passwordFile = new File("passwd.dat");
YggdrasilTokenAuthenticator authenticator = null;
if (passwordFile.exists()) {
	// read the stored token from file
	try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(passwordFile))) {
		authenticator = (YggdrasilTokenAuthenticator) in.readObject();
	}
}

if (authenticator == null || !authenticator.isValid()) {
	// no token is stored, or the stored token is invalid
	// ...... - ask user to login with password
	authenticator = YggdrasilTokenAuthenticator.loginWithToken("<email>", "<password>");
}

// store the token
try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(passwordFile))) {
	out.writeObject(authenticator);
}
```
<p/>

