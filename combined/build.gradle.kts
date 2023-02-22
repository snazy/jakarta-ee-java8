import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin

plugins {
  signing
  `maven-publish`
  `jakarta-java8-conventions`
  alias(libs.plugins.nessie.build.publishing)
}

description = "Jakarta EE libraries built ONLY for Java 8 runtimes - " +
        "NEVER EVER use this dependency in Java 11 or newer runtimes. " +
        "This dependency is required in situations when you have jakarta.* annotations on your classes, " +
        "but Java 8 fails to ignore those due to JDK-8152174 never been backported to Java 8."

apply<ShadowPlugin>()

plugins.withType<ShadowPlugin>().configureEach {
  val shadowJar =
    tasks.named<ShadowJar>("shadowJar") {
      outputs.cacheIf { false } // do not cache uber/shaded jars
      archiveClassifier.set("")
      mergeServiceFiles()
    }

  tasks.named<Jar>("jar") {
    dependsOn(shadowJar)
    archiveClassifier.set("raw")
  }
}
