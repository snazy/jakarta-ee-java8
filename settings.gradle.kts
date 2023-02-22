
val baseVersion = file("version.txt").readText().trim()

pluginManagement {
  repositories {
    mavenCentral() // prefer Maven Central, in case Gradle's repo has issues
    gradlePluginPortal()
    if (System.getProperty("withMavenLocal").toBoolean()) {
      mavenLocal()
    }
  }
}

plugins { id("com.gradle.enterprise") version ("3.12") }

gradleEnterprise {
  if (System.getenv("CI") != null) {
    buildScan {
      termsOfServiceUrl = "https://gradle.com/terms-of-service"
      termsOfServiceAgree = "yes"
      // Add some potentially interesting information from the environment
      listOf(
          "GITHUB_ACTION_REPOSITORY",
          "GITHUB_ACTOR",
          "GITHUB_BASE_REF",
          "GITHUB_HEAD_REF",
          "GITHUB_JOB",
          "GITHUB_REF",
          "GITHUB_REPOSITORY",
          "GITHUB_RUN_ID",
          "GITHUB_RUN_NUMBER",
          "GITHUB_SHA",
          "GITHUB_WORKFLOW"
        )
        .forEach { e ->
          val v = System.getenv(e)
          if (v != null) {
            value(e, v)
          }
        }
      val ghUrl = System.getenv("GITHUB_SERVER_URL")
      if (ghUrl != null) {
        val ghRepo = System.getenv("GITHUB_REPOSITORY")
        val ghRunId = System.getenv("GITHUB_RUN_ID")
        link("Summary", "$ghUrl/$ghRepo/actions/runs/$ghRunId")
        link("PRs", "$ghUrl/$ghRepo/pulls")
      }
    }
  }
}

rootProject.name = "jakarta-java8"

gradle.beforeProject {
  group = "org.projectnessie.jakarta-j8"
  version = baseVersion
}

file("subprojects").listFiles { f -> f.isDirectory }
  .filter { dir -> dir.resolve("README.md").exists() || dir.resolve("src").exists() }
  .forEach { dir ->
    include(dir.name)
    val prj = project(":${dir.name}")
    prj.projectDir = dir
  }

include("jakarta-ee-apis-for-java8")
project(":jakarta-ee-apis-for-java8").projectDir = file("combined")
