package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

/**
 * @author
 */
object AuthorTable : IntIdTable("author") {
    val fio = text("fio")
    val created = datetime("created")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fio by AuthorTable.fio
    var created by AuthorTable.created

    fun toResponse(): AuthorRecord {
        return AuthorRecord(fio)
    }
}