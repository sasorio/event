plugins {
  id("event.conventions")
  id("me.champeau.jmh") version "0.7.3"
}

dependencies {
  compileOnlyApi("org.jetbrains:annotations:26.1.0")
  compileOnlyApi("org.jspecify:jspecify:1.0.1")
  testImplementation("com.google.guava:guava-testlib:33.6.0-jre")
}
