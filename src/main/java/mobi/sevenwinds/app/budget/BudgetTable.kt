package mobi.sevenwinds.app.budget

import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

/**
 * Таблица Бюджета
 */
object BudgetTable : IntIdTable("budget") {
    val year = integer(name = "year")
    val month = integer(name = "month")
    val amount = integer(name = "amount")
    val type = enumerationByName(name = "type", length = 100, klass = BudgetType::class)
    val authorId = optReference(name = "author_id", foreign = AuthorTable)
}

class BudgetEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BudgetEntity>(BudgetTable)

    var year by BudgetTable.year
    var month by BudgetTable.month
    var amount by BudgetTable.amount
    var type by BudgetTable.type
    var author by AuthorEntity optionalReferencedOn BudgetTable.authorId

    fun toResponse(): BudgetRecord {
        return BudgetRecord(year, month, amount, type, author = author?.id?.value)
    }

    fun toResponseWithAuthor(): BudgetRecordWithAuthor {
        return BudgetRecordWithAuthor(year, month, amount, type, author = author?.toResponse())
    }
}