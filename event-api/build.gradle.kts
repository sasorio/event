plugins {
  id("event.conventions")
}

dependencies {
  compileOnlyApi("org.jetbrains:annotations:26.0.2-1")
  compileOnlyApi("org.jspecify:jspecify:1.0.0")
  testImplementation("com.google.guava:guava-testlib:33.5.0-jre")
}
