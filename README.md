# source-sync-maven-plugin
A maven plugin that sends diffs of your project via web sockets

Install this plugin in a profile like this:

```xml
  <profiles>
    <profile>
      <id>sync</id>
      <build>
        <plugins>
          <plugin>
            <groupId>ch.nerdin.minecraft</groupId>
            <artifactId>source-sync-maven-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <configuration>
              <serverUri>ws://localhost:7791/</serverUri>
            </configuration>
            <executions>
              <execution>
                <id>sync</id>
                <phase>validate</phase>
                <goals>
                  <goal>sync</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
    </profile>
  </profiles>
```
