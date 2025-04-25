package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.annotations.type.string.length.MinLength
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route

/**
 * Роуты работы с автором
 */
fun NormalOpenAPIRoute.author() {
    route(path = "/author") {
        route(path = "/add").post<Unit, AuthorRecordResponse, AuthorRecord>(info(summary = "Добавить автора")) { param, body ->
            respond(response = AuthorService.addRecord(body))
        }
    }
}

data class AuthorRecord(
    @MinLength(value = 3) val fio: String
)

data class AuthorRecordResponse(
    val fio: String,
    val creationTime: String
)