plugins {
  id("com.diffplug.spotless")
  id("net.kyori.indra")
  id("net.kyori.indra.checkstyle")
  id("net.kyori.indra.git")
  id("net.kyori.indra.publishing")
}

indra {
  github("sasorio", "event") {
    ci(true)
  }

  apache2License()

  javaVersions {
    target(17)
  }

  configurePublications {
    pom {
      developers {
        developer {
          id.set("kashike")
          name.set("Riley Park")
          timezone.set("America/Vancouver")
        }
      }
    }
  }

  signWithKeyFromProperties("signingKey", "signingPassword")
}


spotless {
  java {
    endWithNewline()
    importOrderFile(rootProject.file(".spotless/sasorio.importorder"))
    leadingTabsToSpaces(2)
    licenseHeaderFile(rootProject.file("license_header.txt"))
    trimTrailingWhitespace()
  }
}

tasks {
  named<Jar>(JavaPlugin.JAR_TASK_NAME) {
    indraGit.applyVcsInformationToManifest(manifest)
  }
}

repositories {
  mavenCentral()
}

dependencies {
  annotationProcessor("ca.stellardrift:contract-validator:1.0.1")
  testImplementation(platform("org.junit:junit-bom:6.0.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
