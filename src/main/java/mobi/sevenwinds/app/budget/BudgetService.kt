package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SortOrder
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val author = body.authorId?.let {
                val authorQuery = AuthorTable.select { AuthorTable.id eq it }
                if (authorQuery.count() > 0) it else null
            }
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = author
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            // Базовый запрос для года
            val query = BudgetTable
                .select { BudgetTable.year eq param.year }

            val authorName = param.authorName

            // Получаем общее количество записей для года (без limit)
            val total = query.count()

            // Считаем сумму по типам для ВСЕХ записей за год (без limit)
            val sumByType = BudgetTable
                .slice(BudgetTable.type, BudgetTable.amount)
                .select { BudgetTable.year eq param.year }
                .groupBy { it[BudgetTable.type].name }
                .mapValues { (_, records) -> records.sumOf { it[BudgetTable.amount] } }

            val limitedQuery = query.limit(param.limit, param.offset)
                .orderBy(BudgetTable.month to SortOrder.ASC)
                .orderBy(BudgetTable.amount to SortOrder.DESC)

            val limitedData = getFilteredData(limitedQuery, authorName)

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = limitedData
            )

        }
    }

    /**
     * Загрузка данных Автора(если существует), фильтрация по AuthorName.
     * @param query данные из БД.
     * @param authorName String или Null.
     * @return List< BudgetRecordWithAuthorName>
     */
    private fun getFilteredData(query: Query, authorName: String?): List<BudgetRecordWithAuthorName> {

        val data = BudgetEntity.wrapRows(query)
            .map { it.toResponse() }
            .map { budgetRecord ->
                val authorId = budgetRecord.authorId

                if (authorId == null) {
                    return@map BudgetRecordWithAuthorName(budgetRecord)
                } else {
                    val authorQuery = AuthorTable.select { AuthorTable.id eq authorId }
                    val authorEntity = AuthorEntity.wrapRow(authorQuery.first())

                    val author = authorEntity.fio
                    val authorCreated = authorEntity.created

                    //форматирование Даты и Времени
                    val pattern: DateTimeFormatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")
                    val authorCreatedAtString = pattern.print(authorCreated)

                    return@map BudgetRecordWithAuthorName(budgetRecord, author, authorCreatedAtString)
                }
            }

        return if (authorName == null) {
            data
        } else {
            //Фильтруем данные по AuthorName
            data.filter { budgetRecordWithAuthorName ->
                return@filter budgetRecordWithAuthorName.authorName?.contains(authorName, ignoreCase = true) == true
            }
        }
    }
}