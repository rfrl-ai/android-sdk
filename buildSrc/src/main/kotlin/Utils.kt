import org.gradle.api.Project
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.*


fun PluginDependenciesSpec.google(module: String): PluginDependencySpec = id("com.google.$module")
fun PluginDependenciesSpec.firebase(module: String) = google("firebase.$module")


fun Project.getGitHash(): String {
  val stdout = ByteArrayOutputStream()
  val res = exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
    standardOutput = stdout
  }

  return if (res.exitValue == 0)
    stdout.toString().trim()
  else
    "XXXXXXX"
}


fun Project.loadSigningProps() = Properties().also { props ->
  val file = File(rootDir, "signing.properties")

  if (file.canRead()) {
    println("${file.absolutePath} found, using contents to sign")
    props.load(FileInputStream(file))
  } else {
    println("${file.absolutePath} not found, using ENV variables to sign")
    props["storePassword"] = System.getenv("storePassword")
    props["keyAlias"] = System.getenv("keyAlias")
    props["keyPassword"] = System.getenv("keyPassword")
  }
}

fun Project.taskAlias(pair: Pair<String, List<String>>) {
  tasks.register(pair.first) {
    pair.second.forEach {
      dependsOn(":${this@taskAlias.name}:$it")
    }
  }
}
