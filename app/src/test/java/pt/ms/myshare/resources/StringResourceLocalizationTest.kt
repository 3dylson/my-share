package pt.ms.myshare.resources

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class StringResourceLocalizationTest {

    private val resourceDirectory = File("src/main/res")
    private val defaultStrings = resourceDirectory.resolve("values/strings.xml")
    private val allowedSharedVisibleKeys = setOf(
        "premium_badge",
        "price_per_period",
        "price_period_suffix",
        "home_review_score_percent",
        "home_review_history_target_delta",
        "home_more_version_label"
    )
    private val localizedStringFiles = listOf(
        "values-pt-rPT/strings.xml",
        "values-es/strings.xml",
        "values-fr/strings.xml",
        "values-de/strings.xml",
        "values-ar/strings.xml"
    ).map(resourceDirectory::resolve)

    @Test
    fun `localized string files expose the same keys as default strings`() {
        val defaultKeys = defaultStrings.readStringResources().keys

        localizedStringFiles.forEach { file ->
            assertEquals(
                "String keys must match default resources for ${file.parentFile?.name}",
                defaultKeys,
                file.readStringResources().keys
            )
        }
    }

    @Test
    fun `amount placeholders are generated from locale instead of fixed string resources`() {
        val allStringNames = sequenceOf(defaultStrings)
            .plus(localizedStringFiles)
            .flatMap { it.readStringResources().keys.asSequence() }
            .toSet()

        assertFalse(allStringNames.contains("amount_placeholder_decimal"))
    }

    @Test
    fun `visible localized strings do not fall back to default English values`() {
        val defaultValues = defaultStrings.readStringResources()

        localizedStringFiles.forEach { file ->
            val localizedValues = file.readStringResources()
            val untranslatedKeys = defaultValues.keys
                .filterNot(allowedSharedVisibleKeys::contains)
                .filter { localizedValues.getValue(it) == defaultValues.getValue(it) }

            assertTrue(
                "Visible strings still match default English in ${file.parentFile?.name}: $untranslatedKeys",
                untranslatedKeys.isEmpty()
            )
        }
    }

    @Test
    fun `critical paywall strings are localized in supported locale files`() {
        val defaultValues = defaultStrings.readStringResources()
        val criticalKeys = defaultValues.keys.filter {
            it.startsWith("paywall_") ||
                it == "home_more_account_manage_subscription" ||
                it == "home_more_account_manage_subscription_desc"
        }

        localizedStringFiles.forEach { file ->
            val localizedValues = file.readStringResources()
            criticalKeys.forEach { key ->
                assertTrue("Missing $key in ${file.parentFile?.name}", localizedValues.containsKey(key))
                assertFalse(
                    "$key is still using the default English value in ${file.parentFile?.name}",
                    localizedValues.getValue(key) == defaultValues.getValue(key)
                )
            }
        }
    }

    private fun File.readStringResources(): Map<String, String> {
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = builder.parse(this)
        val nodes = document.getElementsByTagName("string")
        return buildMap {
            for (index in 0 until nodes.length) {
                val element = nodes.item(index) as Element
                if (element.getAttribute("translatable") != "false") {
                    put(element.getAttribute("name"), element.textContent)
                }
            }
        }
    }
}
