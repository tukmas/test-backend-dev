package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

/**
 * Сервис по добавлению автора
 */
object AuthorService {
    suspend fun addRecord(body: AuthorRecord): AuthorRecordResponse = withContext(context = Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.fio = body.fio
                this.creationTime = DateTime.now()
            }

            return@transaction entity.toResponse()
        }
    }
}