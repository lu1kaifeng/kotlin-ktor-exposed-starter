package spa

import java.io.File

fun String.notContains(regex: Regex) = !contains(regex)
fun File.notExists() = !exists()