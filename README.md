# MOEX Монитор

Pet-проект под Android: мониторинг Московской биржи на публичном **MOEX ISS API** — котировки акций и паёв фондов, топ роста/падения, график цены, лента новостей и избранное.

Проект специально собран как «учебный полигон»: полный стек (multi-module + MVVM + **чистый Dagger 2** + Room + Retrofit + **RxJava2**), XML-разметка с ViewBinding, кастомные View со своими атрибутами и unit-тесты. Корутин и Flow в проекте нет вообще: вся асинхронность — на `Flowable` / `Single` / `Completable`, а граф зависимостей собирается руками, без Hilt-магии. Внизу — список TODO-заданий разной сложности, чтобы было что допиливать.

## Что уже работает

| Экран | Что показывает |
|---|---|
| Рынок | Индекс МосБиржи в шапке, вкладки «Все акции / Топ роста / Топ падения / Фонды», поиск по тикеру и названию, pull-to-refresh |
| Бумага | Цена, изменение в %, график цены за неделю/месяц/год, статистика дня (открытие, максимум, минимум, оборот, лот, режим), звёздочка «в избранное» |
| Новости | Лента новостей MOEX, детальный экран с HTML-текстом |
| Избранное | Отслеживаемые бумаги с актуальными котировками |

Приложение **offline-first**: UI всегда подписан на Room, сеть только обновляет кэш. Поэтому при запуске без интернета видны последние загруженные данные.

## Запуск

1. Открой папку проекта в Android Studio (Ladybug или новее).
2. Gradle wrapper без бинарника `gradle-wrapper.jar` — Android Studio допишет его сам при первом Sync. Если собираешь из консоли, один раз выполни:
   ```bash
   gradle wrapper --gradle-version 8.9
   ```
3. `./gradlew :app:installDebug` или обычный Run в IDE.

Никаких API-ключей не нужно: MOEX ISS отдаёт данные анонимно.

Тесты:
```bash
./gradlew test                                  # все unit-тесты
./gradlew :core:database:connectedDebugAndroidTest  # Room-тест на устройстве/эмуляторе
```

## Структура модулей

```
:app                  Application, MainActivity, BottomNavigation, корневой nav-граф
:core:model           Чистый Kotlin/JVM: доменные модели, без Android
:core:common          SchedulerProvider, Optional-хелперы, TimeProvider, форматтеры, квалификаторы
:core:ui              Дизайн-система: цвета, темы, стили, кастомные View, общий адаптер
:core:network         Retrofit + kotlinx.serialization, парсер формата ISS
:core:database        Room: entity, DAO, конвертеры
:core:data            Репозитории и мапперы «сеть → база → домен»
:core:di              Корневой Dagger-граф: CoreComponent, ViewModelFactory, ViewModelKey
:feature:market       Экран «Рынок»
:feature:security     Карточка бумаги с графиком
:feature:news         Лента и детальный экран новостей
:feature:watchlist    Избранное
```

Правило зависимостей: `feature:*` → `core:di` / `core:ui` → `core:data` → `core:network`, `core:database` → `core:common` → `core:model`. Фичи **не знают друг о друге** и общаются через deep link’и.

## Ключевые решения (и почему так)

**ISS отдаёт не JSON-объекты, а таблицы.** Ответ выглядит так:

```json
{ "securities": { "columns": ["SECID", "LAST"], "data": [["SBER", 281.4]] } }
```

Поэтому в `:core:network` есть `IssBlock`/`IssRow`: они читают значения **по имени колонки**, а не по индексу — MOEX меняет набор колонок, и код от этого не ломается. `IssRow.double("LAST", "CURRENTVALUE")` берёт первое непустое значение, что позволяет одним кодом разбирать и акции, и индексы. Разбор покрыт тестами в `IssBlockTest`.

**Фонды и акции живут в одном режиме торгов.** Отдельного борда `TQTF` больше нет — он отдаёт пустой список. Паи отличаются от акций только колонкой `SECTYPE` (`J`, `A`, `B`, `9`), поэтому сегмент определяется в маппере через `MarketSegment.classify()`, а один сетевой запрос наполняет сразу вкладки «Акции» и «Фонды».

**Навигация между модулями — через deep link.** `:feature:market` открывает карточку бумаги строкой `moexmonitor://security/SBER`, не зная про классы `:feature:security`. Так модули остаются независимыми.

**Кастомные View вместо сторонних библиотек.** `DeltaView` (изменение цены: сам выбирает цвет и рисует чип через `GradientDrawable`) и `SparklineView` (график на `Path` в `onDraw`) написаны руками — их удобно расширять и на них видно, как работают `declare-styleable`, атрибуты темы и `obtainStyledAttributes`.

**Чистый Dagger 2 вместо Hilt.** Корневой `CoreComponent` (`:core:di`) живёт столько же, сколько процесс, и создаётся в `MoexApp` через `DaggerCoreComponent.factory().create(this)`. Наружу он отдаёт **только** репозитории и `SchedulerProvider` — фича физически не может получить `Dao` или `OkHttpClient`. У каждой фичи свой `@Component(dependencies = [CoreComponent::class])`, который создаётся в `onAttach` фрагмента.

**ViewModel без Hilt.** Вместо `@HiltViewModel` — классическая multibinding-карта: `@Binds @IntoMap @ViewModelKey(MarketViewModel::class)` наполняет `Map<Class<out ViewModel>, Provider<ViewModel>>`, а `ViewModelFactory` (`:core:di`) достаёт из неё нужный провайдер. Для `SecurityViewModel` и `NewsDetailViewModel`, которым нужен рантайм-параметр (`secId`, `newsId`), сделаны ручные assisted-фабрики — это тот же приём, что и `@AssistedInject`, только без дополнительной библиотеки.

**RxJava2 вместо Flow.** Чтение из базы — `Flowable` (Room сам присылает новый список при изменении таблицы), однократные операции — `Single`/`Maybe`, запись — `Completable`. Состояние экрана держится в `BehaviorSubject` (Rx-аналог `StateFlow`), а наружу отдаётся как `.observeOn(ui).replay(1).refCount()` — при повороте экрана подписка пересоздаётся, но цепочка к базе не собирается заново.

**`SchedulerProvider`, а не `Schedulers.io()` по коду.** Планировщики спрятаны за интерфейсом, поэтому в тестах вместо них подставляется один `TestScheduler` и время становится управляемым: `debounce` в поиске тестируется через `advanceTimeBy`, без ожиданий и flaky.

**`Optional` вместо nullable.** RxJava2 бросает NPE при попытке передать `null` в `onNext`, поэтому «значения, которых может не быть» (котировка бумаги, текст ошибки) заворачиваются в `java.util.Optional` — он доступен с API 24, а `minSdk` здесь 26. По той же причине запросы Room на одну строку возвращают `Flowable<List<T>>`: для пустого результата Room просто не пришлёт элемент, и экран навсегда остался бы без состояния «не найдено».

**Подписки View живут в `CompositeDisposable`.** Базовый `RxFragment` (`:core:ui`) держит `viewDisposables` и вызывает `clear()` в `onDestroyView` — ручной аналог `lifecycleScope`. Это чуть грубее, чем `repeatOnLifecycle(STARTED)`: подписка не приостанавливается, когда экран уходит в фон (см. TODO-задание).

## Что стоит почитать в коде первым

1. `core/network/.../iss/IssBlock.kt` — самая нестандартная часть проекта.
2. `core/data/.../repository/MarketRepositoryImpl.kt` — как устроен offline-first.
3. `feature/market/.../MarketViewModel.kt` — `combineLatest` + `switchMap` + `debounce` в одном месте.
   А ещё `core/di/.../ViewModelFactory.kt` — как ViewModel собираются без Hilt.
4. `core/ui/.../widget/DeltaView.kt` и `SparklineView.kt` — кастомные атрибуты и `onDraw`.
5. `app/src/main/res/navigation/nav_graph_main.xml` — сборка графов фич через `<include>`.

## TODO: задания для прокачки

### Разминка
- [ ] Добавить сортировку списка (по обороту / по проценту / по алфавиту) через `BottomSheetDialogFragment`.
- [ ] Показывать время последнего обновления кэша (`SecurityEntity.updatedAtMillis`) в шапке экрана «Рынок».
- [ ] Добавить `ShimmerDrawable` или простой skeleton-плейсхолдер вместо пустого списка при первой загрузке.
- [ ] Вынести цвета роста/падения в атрибуты темы и сделать альтернативную «дальтоник-friendly» тему.

### Средний уровень
- [ ] Подключить **Safe Args** вместо `bundleOf` и строковых ключей аргументов.
- [ ] Заменить `<deepLink>`-навигацию на типизированный интерфейс-навигатор, реализованный в `:app`.
- [ ] Нормализовать базу: вынести котировки в таблицу `quotes`, связать через `@Relation`, написать `Migration` с version 1 → 2 и тест миграции.
- [ ] Добавить `WorkManager`, который раз в 15 минут обновляет избранное в фоне и присылает уведомление при изменении цены больше чем на N %.
- [ ] Написать тесты для `SecurityViewModel` и `NewsViewModel` по образцу `MarketViewModelTest`.
- [ ] Заменить ручной `clear()` в `RxFragment` на `LifecycleObserver`, который отписывается в `onStop` и переподписывается в `onStart` — полноценный аналог `repeatOnLifecycle(STARTED)`.
- [ ] Перевести фичи с `@Component(dependencies = ...)` на `@Subcomponent` + `@Scope`-аннотацию `FragmentScope` и сравнить, что удобнее.
- [ ] Заменить ручные assisted-фабрики на `@AssistedInject` из `dagger.assisted` и убрать boilerplate.
- [ ] Добавить `RxJavaPlugins.setErrorHandler` и глобальный логгер `UndeliverableException` — классическая ловушка RxJava2.
- [ ] Добавить `MockWebServer` и тесты на `MoexRemoteDataSource` с реальными сохранёнными ответами ISS.

### Посложнее
- [ ] Сделать `SparklineView` интерактивным: тач-скраб с подсказкой цены и даты, анимация появления линии через `PathMeasure` + `ValueAnimator`.
- [ ] Полноценные свечи (OHLC) вместо линии: свой `CandleChartView` с зумом и панорамированием.
- [ ] Вынести Gradle-конфигурацию в **convention plugins** (`build-logic`) — сейчас каждый модуль дублирует один и тот же блок `android {}`.
- [ ] Заменить `LinearLayout` + `TabLayout` на `ViewPager2` с фрагментом на вкладку и сохранением скролла.
- [ ] Добавить второй источник данных (например, дивиденды через `/iss/securities/{secid}/dividends.json`) и объединить его с котировками в одном UI-состоянии.
- [ ] Сделать альтернативную реализацию экрана «Рынок» на Jetpack Compose в отдельном модуле, чтобы сравнить с XML-версией.
- [ ] Заменить `Optional` на свой `sealed interface Loaded<out T>` с состояниями `Absent`/`Present` и посмотреть, насколько читаемее станут цепочки.

### Инфраструктура
- [ ] Подключить ktlint/detekt и настроить pre-commit проверку.
- [ ] Добавить GitHub Actions: `assembleDebug` + `test` на каждый PR.
- [ ] Настроить baseline profile и посмотреть на разницу в скорости старта.

## Источники

- Документация MOEX ISS API: https://iss.moex.com/iss/reference/
- Пример запроса котировок: https://iss.moex.com/iss/engines/stock/markets/shares/boards/TQBR/securities.json?iss.meta=off
- Свечи: https://iss.moex.com/iss/engines/stock/markets/shares/boards/TQBR/securities/SBER/candles.json?interval=24&from=2026-08-01
- Новости: https://iss.moex.com/iss/sitenews.json?iss.meta=off&limit=50

Данные MOEX предоставляются с задержкой и предназначены для ознакомления, не для торговых решений.
