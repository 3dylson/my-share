package pt.ms.myshare.resources

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class HardcodedUserCopyTest {

    private val sourceDirectory = File("src/main/java/pt/ms/myshare")
    private val hardcodedUserCopyPatterns = listOf(
        Regex("""\bText\(\s*"(?!\$)[^"]*[A-Za-z][^"]*""""),
        Regex("""contentDescription\s*=\s*"(?!\$)[^"]*[A-Za-z][^"]*""""),
        Regex("""placeholder\s*=\s*\{\s*Text\(\s*"(?!\$)[^"]*[A-Za-z][^"]*""""),
        Regex("""label\s*=\s*\{\s*Text\(\s*"(?!\$)[^"]*[A-Za-z][^"]*""""),
        Regex("""title\s*=\s*\{\s*Text\(\s*"(?!\$)[^"]*[A-Za-z][^"]*""""),
        Regex("""message\s*=\s*"(?!\$)[^"]*[A-Za-z][^"]*""""),
        Regex("""confirmText\s*=\s*"(?!\$)[^"]*[A-Za-z][^"]*""""),
        Regex("""dismissText\s*=\s*"(?!\$)[^"]*[A-Za-z][^"]*"""")
    )

    @Test
    fun `production UI copy is loaded from string resources`() {
        val violations = sourceDirectory
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap(::hardcodedUserCopyViolations)
            .toList()

        assertTrue(
            "User-facing copy must use string resources instead of hardcoded literals: $violations",
            violations.isEmpty()
        )
    }

    private fun hardcodedUserCopyViolations(file: File): Sequence<String> {
        return file.readLines().asSequence()
            .mapIndexedNotNull { index, line ->
                if (hardcodedUserCopyPatterns.any { it.containsMatchIn(line) }) {
                    "${file.path}:${index + 1}"
                } else {
                    null
                }
            }
    }
}
