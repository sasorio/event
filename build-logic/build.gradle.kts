plugins {
  `kotlin-dsl`
}

dependencies {
  implementation("com.diffplug.spotless:spotless-plugin-gradle:8.2.0")
  implementation("net.kyori:indra-common:4.0.0")
  implementation("net.kyori:indra-git:4.0.0")
  implementation("net.kyori:indra-publishing-sonatype:4.0.0")
}
