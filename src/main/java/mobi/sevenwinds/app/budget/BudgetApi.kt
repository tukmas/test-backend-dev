package mobi.sevenwinds.app.budget

import com.fasterxml.jackson.annotation.JsonInclude
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.annotations.type.number.integer.max.Max
import com.papsign.ktor.openapigen.annotations.type.number.integer.min.Min
import com.papsign.ktor.openapigen.annotations.type.string.length.MinLength
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import mobi.sevenwinds.app.author.AuthorRecordResponse

/**
 * Роуты работы с бюджетом
 */
fun NormalOpenAPIRoute.budget() {
    route(path = "/budget") {
        route(path = "/add").post<Unit, BudgetRecord, BudgetRecord>(info(summary = "Добавить запись")) { _, body ->
            respond(response = BudgetService.addRecord(body))
        }

        route("/year/{year}/stats") {
            get<BudgetYearParam, BudgetYearStatsResponse>(info(summary = "Получить статистику за год")) { param ->
                respond(response = BudgetService.getYearStats(param))
            }
        }
    }
}

data class BudgetRecord(
    @Min(value = 1900) val year: Int,
    @Min(value = 1) @Max(value = 12) val month: Int,
    @Min(value = 1) val amount: Int,
    val type: BudgetType,
    @Min(value = 1) @JsonInclude(value = JsonInclude.Include.NON_NULL) val author: Int? = null
)

data class BudgetRecordWithAuthor(
    @Min(value = 1900) val year: Int,
    @Min(value = 1) @Max(value = 12) val month: Int,
    @Min(value = 1) val amount: Int,
    val type: BudgetType,
    val author: AuthorRecordResponse? = null
)

data class BudgetYearParam(
    @PathParam(description = "Год") val year: Int,
    @QueryParam(description = "Лимит пагинации") val limit: Int,
    @QueryParam(description = "Смещение пагинации") val offset: Int,
    @QueryParam(description = "ФИО автора") @MinLength(value = 3) val fio: String?
)

class BudgetYearStatsResponse(
    val total: Int,
    val totalByType: Map<String, Int>,
    val items: List<BudgetRecordWithAuthor>
)

enum class BudgetType {
    Приход, Расход, Комиссия
}