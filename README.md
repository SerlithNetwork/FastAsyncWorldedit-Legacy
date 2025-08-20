<p align="center">
  <img src="assets/fawe_logo.png">
</p>

---

# This is the legacy version of FAWE (1.8 - 1.12 and other platforms). It is no longer maintained. The focus lays on the newer versions of minecraft. You can find the new version of FAWE [here](https://github.com/IntellectualSites/FastAsyncWorldEdit).

FAWE is a fork of WorldEdit that has huge speed and memory improvements and considerably more features

It is available for Bukkit, Forge, Sponge and Nukkit.

## Additions
* [Java 17 compatibility](https://github.com/DawningW/FastAsyncWorldedit-Legacy/commit/41c4ef8c245b4a8cf212606a00e6d093a47a9b6d)
* [Ytnoos security patch](https://github.com/ytnoos/FastAsyncWorldedit-Legacy/commit/12a01983c9650e4b1a4dbd067344d88cd89be1b1)

## Installation
Drop the FAWE jar in your `/plugins` folder and install [WorldEdit 6.1.7.2](https://dev.bukkit.org/projects/worldedit/files/2431372). \
FAWE will attempt to download WorldEdit but at the moment of writing this is very likely to fail.

To run in Java 17 you have to add `--add-opens=java.base/java.lang=ALL-UNNAMED` and `--add-opens=java.base/java.lang.reflect=ALL-UNNAMED` before the `-jar` flag. \
These flags will allow reflection to edit the `final` [modifier](https://bugs.openjdk.org/browse/JDK-8210522) of fields without breaking compatibility for Java 8.
```sh
# Example server startup script
$ java -jar -Xms2G -Xmx2G --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -jar server.jar
```

## Building
FAWE now uses kotlin gradle to build

```sh
$ gradlew build
```

**Compiled builds** will **no longer** be located at `/target` as the upstream project (Maven standard). \
These builds **can now be found** at `/bukkit/build/libs/` as Gradle standard.

## TODO
Not likely to be added anytime soon:
1. ~~Either bundle `WorldEdit` or just change the download link.~~ ✅
2. Use a slightly more updated `WorldEdit` version like 6.1.9 🕒
3. Fix [chunk duplication](https://www.youtube.com/watch?v=hdEVYZ8DWEs) error 🕒
4. Drop ProtocolLib in favor of packetevents

## Links 

* [Spigot Page](https://www.spigotmc.org/threads/fast-async-worldedit.100104/)
* [Discord](https://discord.gg/ngZCzbU)
* [Wiki](https://github.com/boy0001/FastAsyncWorldedit/wiki)

## Downloads
### <1.12.2
* [Download](https://ci.athion.net/job/FastAsyncWorldEdit-legacy/)
* [Jenkins](https://ci.athion.net/job/FastAsyncWorldEdit/)
* [JavaDoc](https://ci.athion.net/job/FastAsyncWorldEdit/javadoc/)

### 1.13+
* [Download](https://intellectualsites.github.io/download/fawe.html)
* [Jenkins](https://ci.athion.net/job/FastAsyncWorldEdit-1.13/)
* [JavaDoc](https://ci.athion.net/job/FastAsyncWorldEdit-1.13/javadoc/)
* [Repository](https://github.com/IntellectualSites/FastAsyncWorldEdit-1.13)

## Developer Resources
* [Maven Repo](http://ci.athion.net/job/FastAsyncWorldEdit/ws/mvn/)
* [API Documentation](https://github.com/boy0001/FastAsyncWorldedit/wiki/API)

## Contributing
Have an idea for an optimization, or a cool feature?
 - I'll accept most PR's
 - Let me know what you've tested / what may need further testing
 - If you need any help, create a ticket or discuss on [Discord](https://discord.gg/ngZCzbU)
