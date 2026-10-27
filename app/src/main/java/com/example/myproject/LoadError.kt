package com.example.myproject

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

sealed interface LoadError {
    data object NoNetwork : LoadError
    data object Timeout : LoadError
    data class Server(val code: Int?) : LoadError
    data object BadResponse : LoadError
    data object NoLocationPermission : LoadError
    data object LocationDisabled : LoadError
    data object LocationUnavailable : LoadError
    data object Unknown : LoadError
}

class WikiApiException(message: String?) : Exception(message)

class BadResponseException : Exception()

class LocationPermissionException : Exception()
class LocationDisabledException : Exception()
class LocationUnavailableException : Exception()

fun Throwable.toLoadError(): LoadError = when (this) {
    is HttpException -> LoadError.Server(code())
    is WikiApiException -> LoadError.Server(null)
    is SerializationException, is BadResponseException -> LoadError.BadResponse
    is SocketTimeoutException -> LoadError.Timeout
    is IOException -> LoadError.NoNetwork
    is LocationPermissionException, is SecurityException -> LoadError.NoLocationPermission
    is LocationDisabledException -> LoadError.LocationDisabled
    is LocationUnavailableException -> LoadError.LocationUnavailable
    else -> LoadError.Unknown
}

val LoadError.message: String
    get() = when (this) {
        LoadError.NoNetwork -> "Немає з'єднання з інтернетом. Перевірте мережу або вимкніть авіарежим."
        LoadError.Timeout -> "Вікіпедія не відповідає. Можливо, з'єднання надто повільне."
        is LoadError.Server ->
            if (code != null) "Сервер Вікіпедії тимчасово не працює (код $code). Спробуйте пізніше."
            else "Сервер Вікіпедії повернув помилку. Спробуйте пізніше."
        LoadError.BadResponse -> "Вікіпедія надіслала дані в незнайомому форматі. Спробуйте ще раз пізніше."
        LoadError.NoLocationPermission -> "Щоб показати місця поруч, потрібен доступ до місцезнаходження."
        LoadError.LocationDisabled -> "Геолокацію вимкнено. Увімкніть її в налаштуваннях телефону."
        LoadError.LocationUnavailable -> "Не вдалося визначити, де ви зараз. Спробуйте ще раз."
        LoadError.Unknown -> "Щось пішло не так. Спробуйте ще раз."
    }
