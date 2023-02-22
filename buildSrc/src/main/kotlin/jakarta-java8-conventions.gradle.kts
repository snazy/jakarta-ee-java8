import org.gradle.api.JavaVersion
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType

if (project != rootProject) {
  apply<JavaLibraryPlugin>()
}

repositories {
  mavenCentral()
  if (System.getProperty("withMavenLocal").toBoolean()) {
    mavenLocal()
  }
}

tasks.withType<Jar>().configureEach {
  manifest {
    attributes["Implementation-Title"] = "Jakarta EE APIs for Java 8 - ${project.name}"
    attributes["Implementation-Version"] = project.version
    attributes["Implementation-Vendor"] = "Authors of Jakarta APIs for Java 8"
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
  options.release.set(8)
}

tasks.withType<Javadoc>().configureEach {
  val opt = options as CoreJavadocOptions
  // don't spam log w/ "warning: no @param/@return"
  opt.addStringOption("Xdoclint:-reference", "-quiet")
}

plugins.withType<JavaPlugin>().configureEach {
  configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    modularity.inferModulePath.set(true)
  }
}

if (project != rootProject) {
  tasks.withType<Jar>().configureEach { duplicatesStrategy = DuplicatesStrategy.WARN }
}
