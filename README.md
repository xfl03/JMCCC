# jmccc-jyal-authenticator

### Download
You can get the latest maven release from [here](https://search.maven.org/#search|ga|1|g%3A%22com.github.to2mbn%22%20a%3A%22jmccc-jyal-authenticator%22).

The snapshot repository:
```xml
<repository>
	<id>ossrh</id>
	<url>https://oss.sonatype.org/content/groups/public/</url>
	<snapshots>
		<enabled>true</enabled>
	</snapshots>
</repository>
```
Or see [Jenkins](http://ci.infinity-studio.org/job/jmccc-jyal-authenticator/).

### Dependencies
* jmccc
* jyal

### Compile
```
	mvn clean install
```

### Usage
For password login:<br/>
```java
new YggdrasilPasswordAuthenticator("<email>", "<password>");
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

