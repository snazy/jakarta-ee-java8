
plugins {
  signing
  `maven-publish`
  alias(libs.plugins.nessie.build.ide.integration)
  alias(libs.plugins.nessie.build.publishing)
  alias(libs.plugins.nexus.publish)
  `jakarta-java8-conventions`
}

tasks.named<Wrapper>("wrapper") { distributionType = Wrapper.DistributionType.ALL }

// Pass environment variables:
//    ORG_GRADLE_PROJECT_sonatypeUsername
//    ORG_GRADLE_PROJECT_sonatypePassword
// OR in ~/.gradle/gradle.properties set
//    sonatypeUsername
//    sonatypePassword
// Call targets:
//    publishToSonatype
//    closeAndReleaseSonatypeStagingRepository
nexusPublishing {
  transitionCheckOptions {
    // default==60 (10 minutes), wait up to 60 minutes
    maxRetries.set(360)
    // default 10s
    delayBetween.set(java.time.Duration.ofSeconds(10))
  }
  repositories { sonatype() }
}

publishingHelper {
  nessieRepoName.set("jakarta-java8")
  inceptionYear.set("2023")
}

val dependenciesMap = mapOf(
  "cdi" to listOf("common-annotations-api", "expression-language", "injection-api", "interceptors"),
  "jaxb-api" to listOf("jaf-api"),
  "rest" to listOf("jaxb-api")
)

val sourceDirs = mapOf(
  "cdi" to listOf("api", "lang-model"),
  "common-annotations-api" to listOf("api"),
  "expression-language" to listOf("api"),
  "injection-api" to listOf("."),
  "interceptors" to listOf("api"),
  "jaf-api" to listOf("activation"),
  "jaxb-api" to listOf("jaxb-api"),
  "rest" to listOf("jaxrs-api"),
  "servlet" to listOf("api"),
  "servlet" to listOf("api"),
  "validation" to listOf(".")
)

subprojects {
  plugins.apply("jakarta-java8-conventions")

  if (project.name == "combined") {
    dependencies {
      sourceDirs.keys.forEach { p ->
        add("implementation", project(":$p"))
      }
    }
  }
  if (sourceDirs.containsKey(project.name)) {
    val fixupSource = file("build/generated/java")

    val fixupJavaSource by tasks.registering(Sync::class) {
      destinationDir = fixupSource
      from(sourceDirs[project.name]?.map { dir -> "$dir/src/main/java" })
      exclude("module-info.java")
      filter {
        it
          .replace(Regex("@Deprecated[(][^)]*[)]"), "@Deprecated")
          .replace(Regex("new ThreadLocal[<][>][(][)]"), "new ThreadLocal()")
          .replace(Regex("[!]method[.]canAccess[(]instance[)]"), "false")
      }
    }

    extensions.getByType(SourceSetContainer::class.java).named("main") {
      java {
        setSrcDirs(listOf(fixupSource))
      }
      resources {
        setSrcDirs(sourceDirs[project.name]?.map { dir -> file("$dir/src/main/resources") }!!)
      }
    }

    tasks.named<JavaCompile>("compileJava") {
      dependsOn(fixupJavaSource)
    }

    dependencies {
      dependenciesMap[project.name]?.forEach { dep ->
        add("api", project(":$dep"))
      }
    }
  }
}
