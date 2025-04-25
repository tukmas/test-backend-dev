package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.lowerCase

/**
 * Сервис по работе с бюджетом
 */
object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(context = Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = if (body.author != null) AuthorEntity[body.author] else null
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(context = Dispatchers.IO) {
        transaction {
            var query = BudgetTable.select { BudgetTable.year eq param.year }

            val total = query.count()

            val sumByType = BudgetEntity.wrapRows(rows = query)
                .map { it.toResponse() }
                .groupBy { it.type.name }
                .mapValues { it.value.sumOf { v -> v.amount } }

            if (param.fio != null) {
                val authorIds = AuthorTable
                    .select { AuthorTable.fio.lowerCase() like "%${param.fio.toLowerCase()}%" }
                    .map { it[AuthorTable.id] }
                query = query.andWhere { BudgetTable.authorId inList authorIds }
            }

            val data = BudgetEntity.wrapRows(
                rows = query
                    .limit(n = param.limit, offset = param.offset)
                    .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
            ).map { it.toResponseWithAuthor() }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}