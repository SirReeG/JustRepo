package ru.reeg.moexmonitor.core.common.rx

import java.util.Optional

/**
 * RxJava2 не умеет пропускать null через onNext — при попытке получим NPE.
 * Поэтому «значения, которых может не быть» заворачиваем в Optional.
 * java.util.Optional доступен с API 24, а minSdk у нас 26.
 */
fun <T : Any> T?.toOptional(): Optional<T> = Optional.ofNullable(this)

fun <T : Any> Optional<T>.orNull(): T? = if (isPresent) get() else null
