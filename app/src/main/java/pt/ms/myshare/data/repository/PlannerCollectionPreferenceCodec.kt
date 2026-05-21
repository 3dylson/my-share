package pt.ms.myshare.data.repository

import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.GoalType
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayRuleType
import java.math.BigDecimal
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

object PlannerCollectionPreferenceCodec {

    fun encodeGoals(goals: List<Goal>): String = goals.joinToString(RECORD_SEPARATOR) { goal ->
        encodeFields(
            goal.id,
            goal.name,
            goal.targetAmount.toPlainString(),
            goal.currentProgress.toPlainString(),
            goal.type.name,
            goal.createdAt.toString(),
            goal.isCompleted.toString()
        )
    }

    fun decodeGoals(raw: String?): List<Goal> = decodeRecords(raw).mapNotNull { fields ->
        runCatching {
            val id = fields.getOrNull(0)?.takeIf(String::isNotBlank) ?: return@runCatching null
            val targetAmount = fields.getOrNull(2)?.toBigDecimalOrNull() ?: return@runCatching null
            Goal(
                id = id,
                name = fields.getOrNull(1).orEmpty(),
                targetAmount = targetAmount,
                currentProgress = fields.getOrNull(3)?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                type = fields.getOrNull(4)?.let(GoalType::valueOf) ?: GoalType.CUSTOM,
                createdAt = fields.getOrNull(5)?.let(LocalDate::parse) ?: LocalDate.now(),
                isCompleted = fields.getOrNull(6).toBoolean()
            )
        }.getOrNull()
    }

    fun encodeRules(rules: List<PaydayRule>): String = rules.joinToString(RECORD_SEPARATOR) { rule ->
        encodeFields(
            rule.id,
            rule.name,
            rule.amount.toPlainString(),
            rule.isPercentage.toString(),
            rule.type.name,
            rule.createdAt.toString()
        )
    }

    fun decodeRules(raw: String?): List<PaydayRule> = decodeRecords(raw).mapNotNull { fields ->
        runCatching {
            val id = fields.getOrNull(0)?.takeIf(String::isNotBlank) ?: return@runCatching null
            val amount = fields.getOrNull(2)?.toBigDecimalOrNull() ?: return@runCatching null
            PaydayRule(
                id = id,
                name = fields.getOrNull(1).orEmpty(),
                amount = amount,
                isPercentage = fields.getOrNull(3).toBoolean(),
                type = fields.getOrNull(4)?.let(PaydayRuleType::valueOf) ?: PaydayRuleType.OTHER,
                createdAt = fields.getOrNull(5)?.let(LocalDate::parse) ?: LocalDate.now()
            )
        }.getOrNull()
    }

    fun encodeReviews(reviews: List<ManualReview>): String = reviews.joinToString(RECORD_SEPARATOR) { review ->
        encodeFields(
            review.id,
            review.actualFlexibleSpend.toPlainString(),
            review.actualGoalContribution.toPlainString(),
            review.plannedFlexibleSpend?.toPlainString().orEmpty(),
            review.plannedGoalContribution?.toPlainString().orEmpty(),
            review.createdAt.toString(),
            review.paydayDate?.toString().orEmpty()
        )
    }

    fun decodeReviews(raw: String?): List<ManualReview> = decodeRecords(raw).mapNotNull { fields ->
        runCatching {
            val id = fields.getOrNull(0)?.takeIf(String::isNotBlank) ?: return@runCatching null
            val actualFlexibleSpend = fields.getOrNull(1)?.toBigDecimalOrNull() ?: return@runCatching null
            val actualGoalContribution = fields.getOrNull(2)?.toBigDecimalOrNull() ?: return@runCatching null
            ManualReview(
                id = id,
                actualFlexibleSpend = actualFlexibleSpend,
                actualGoalContribution = actualGoalContribution,
                plannedFlexibleSpend = fields.getOrNull(3)?.takeIf(String::isNotBlank)?.toBigDecimalOrNull(),
                plannedGoalContribution = fields.getOrNull(4)?.takeIf(String::isNotBlank)?.toBigDecimalOrNull(),
                createdAt = fields.getOrNull(5)?.let(LocalDate::parse) ?: LocalDate.now(),
                paydayDate = fields.getOrNull(6)?.takeIf(String::isNotBlank)?.let(LocalDate::parse)
            )
        }.getOrNull()
    }

    private fun encodeFields(vararg fields: String): String =
        fields.joinToString(FIELD_SEPARATOR) { field ->
            URLEncoder.encode(field, StandardCharsets.UTF_8.name())
        }

    private fun decodeRecords(raw: String?): List<List<String>> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.lineSequence()
            .filter(String::isNotBlank)
            .map { record ->
                record.split(FIELD_SEPARATOR).map { field ->
                    URLDecoder.decode(field, StandardCharsets.UTF_8.name())
                }
            }
            .toList()
    }

    private const val RECORD_SEPARATOR = "\n"
    private const val FIELD_SEPARATOR = "|"
}
