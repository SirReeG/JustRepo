package ru.reeg.moexmonitor.core.common

/**
 * Сообщение об ошибке для пользователя. Технический текст исключения
 * (например, «Unable to resolve host») тоже показываем — на этапе учёбы
 * это полезнее, чем безликое «Что-то пошло не так».
 *
 * TODO (упражнение): раскладывать Throwable по типам (UnknownHostException,
 * SocketTimeoutException, HttpException) и брать строки из ресурсов.
 */
fun Throwable.userMessage(): String =
    message?.takeIf { it.isNotBlank() } ?: "Не удалось обновить данные"
