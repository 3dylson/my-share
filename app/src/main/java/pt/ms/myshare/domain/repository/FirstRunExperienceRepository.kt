package pt.ms.myshare.domain.repository

interface FirstRunExperienceRepository {
    fun isHomeCoachMarksPending(): Boolean
    suspend fun setHomeCoachMarksPending(pending: Boolean)
}
