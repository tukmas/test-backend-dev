package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

/**
 * Таблица Автор
 */
object AuthorTable : IntIdTable(name = "author") {
    val fio = varchar(name = "fio",255)
    val creation_time = datetime(name = "creation_time")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fio by AuthorTable.fio
    var creationTime by AuthorTable.creation_time

    fun toResponse(): AuthorRecordResponse  {
        return AuthorRecordResponse(fio, creationTime = creationTime.toString())
    }
}