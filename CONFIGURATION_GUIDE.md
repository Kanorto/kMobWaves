# Руководство по конфигурации kMobWaves

Это подробное руководство объясняет все параметры конфигурации плагина kMobWaves.

## Оглавление

- [Основные настройки](#основные-настройки)
- [Расширенные настройки](#расширенные-настройки)
- [Настройка волн](#настройка-волн)
- [Примеры конфигураций](#примеры-конфигураций)

## Основные настройки

### debug
```yaml
debug: false
```
- **Тип**: boolean (true/false)
- **По умолчанию**: false
- **Описание**: Включает режим отладки с подробным логированием
- **Рекомендуется**: Включить при возникновении проблем

### serializer
```yaml
serializer: LEGACY
```
- **Тип**: string
- **По умолчанию**: LEGACY
- **Варианты**: 
  - `LEGACY` - Стандартные коды цветов через & (&a, &c и т.д.)
  - `LEGACY_ADVANCED` - LEGACY + HEX цвета через &## (&##FF5555)
  - `MINIMESSAGE` - Современный формат <color> (требует 1.17+)
- **Описание**: Система обработки цветов для сообщений

### info_command_permission
```yaml
info_command_permission: "kmobwaves.user"
```
- **Тип**: string
- **По умолчанию**: "kmobwaves.user"
- **Описание**: Разрешение для команды /kmobwaves info
- **Важно**: Должно быть указано, иначе команда не будет работать

## Расширенные настройки

### auto_restart
```yaml
auto_restart: true
```
- **Тип**: boolean
- **По умолчанию**: true
- **Описание**: Автоматический перезапуск волн после завершения последней
- **Использование**: Установите false для однократного прохождения всех волн

### spawn_radius
```yaml
spawn_radius: 5
```
- **Тип**: integer (целое число)
- **По умолчанию**: 5
- **Диапазон**: 0 и выше
- **Описание**: Радиус случайного разброса спавна в блоках
- **Пример**: При значении 5 моб может заспавниться в радиусе ±5 блоков от координаты

### default_health_multiplier
```yaml
default_health_multiplier: 1.0
```
- **Тип**: double (дробное число)
- **По умолчанию**: 1.0
- **Описание**: Множитель здоровья мобов по умолчанию
- **Примеры**:
  - `1.0` - обычное здоровье
  - `2.0` - двойное здоровье
  - `0.5` - половина здоровья

## Настройка сообщений

### wave_messages.enabled
```yaml
wave_messages:
  enabled: true
```
- **Тип**: boolean
- **По умолчанию**: true
- **Описание**: Включает/выключает сообщения о событиях волн

### wave_messages.start
```yaml
wave_messages:
  start: "&e&l>>> &6Волна %wave% началась! &e&l<<<"
```
- **Тип**: string
- **Плейсхолдеры**:
  - `%wave%` - номер волны
- **Описание**: Сообщение при старте волны

### wave_messages.complete
```yaml
wave_messages:
  complete: "&a&l>>> &2Волна %wave% завершена! Следующая волна через %delay% секунд. &a&l<<<"
```
- **Тип**: string
- **Плейсхолдеры**:
  - `%wave%` - номер завершенной волны
  - `%delay%` - задержка до следующей волны в секундах
  - `%next_wave%` - номер следующей волны
- **Описание**: Сообщение при завершении волны

### wave_messages.all_complete
```yaml
wave_messages:
  all_complete: "&6&l>>> Все волны завершены! Перезапуск с первой волны... &6&l<<<"
```
- **Тип**: string
- **Описание**: Сообщение при завершении всех волн (если auto_restart: true)

## Настройка звуков

### sounds.enabled
```yaml
sounds:
  enabled: true
```
- **Тип**: boolean
- **По умолчанию**: true
- **Описание**: Включает/выключает звуковые эффекты

### Звук начала волны
```yaml
sounds:
  wave_start:
    sound: "ENTITY_ENDER_DRAGON_GROWL"
    volume: 1.0
    pitch: 1.0
```
- **sound**: Название звука из Bukkit Sound enum
- **volume**: Громкость (0.0 - 1.0+)
- **pitch**: Высота тона (0.5 - 2.0)

### Звук завершения волны
```yaml
sounds:
  wave_complete:
    sound: "UI_TOAST_CHALLENGE_COMPLETE"
    volume: 1.0
    pitch: 1.0
```

### Звук смерти моба
```yaml
sounds:
  mob_death:
    sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
    volume: 0.5
    pitch: 1.2
```

**Популярные звуки**:
- `ENTITY_ENDER_DRAGON_GROWL` - рычание дракона
- `ENTITY_WITHER_SPAWN` - спавн иссушителя
- `UI_TOAST_CHALLENGE_COMPLETE` - выполнение достижения
- `ENTITY_PLAYER_LEVELUP` - повышение уровня
- `ENTITY_EXPERIENCE_ORB_PICKUP` - подбор опыта

## Настройка BossBar

### bossbar.enabled
```yaml
bossbar:
  enabled: true
```
- **Тип**: boolean
- **По умолчанию**: true
- **Описание**: Включает/выключает отображение BossBar

### bossbar.title
```yaml
bossbar:
  title: "&6Волна %wave% &7- &eОсталось: %remaining%/%total%"
```
- **Тип**: string
- **Плейсхолдеры**:
  - `%wave%` - номер волны
  - `%remaining%` - количество оставшихся мобов
  - `%total%` - общее количество мобов в волне
- **Описание**: Текст на BossBar

### bossbar.color
```yaml
bossbar:
  color: "YELLOW"
```
- **Тип**: string
- **Варианты**: BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW
- **По умолчанию**: YELLOW
- **Описание**: Цвет полосы BossBar

### bossbar.style
```yaml
bossbar:
  style: "SEGMENTED_10"
```
- **Тип**: string
- **Варианты**: 
  - `SOLID` - сплошная полоса
  - `SEGMENTED_6` - 6 сегментов
  - `SEGMENTED_10` - 10 сегментов
  - `SEGMENTED_12` - 12 сегментов
  - `SEGMENTED_20` - 20 сегментов
- **По умолчанию**: SEGMENTED_10
- **Описание**: Стиль отображения BossBar

## Настройка волн

### Обязательные параметры

```yaml
Waves:
  - count: 1                    # Номер волны
    mobs:                       # Список мобов
      - "MobName:50"
    coordinates:                # Координаты спавна
      - "100,100,100"
    mobs-count: 15              # Количество мобов
    exceptions: 10              # Задержка (секунды)
```

### count
- **Тип**: integer
- **Описание**: Уникальный номер волны
- **Примечание**: Не обязательно последовательные, но используются для идентификации

### mobs
- **Тип**: список строк
- **Формат**: `"ИмяМоба"` или `"ИмяМоба:шанс"`
- **Примеры**:
  - `"Zombie"` - 100% шанс
  - `"Zombie:50"` - 50% шанс
  - Несколько мобов с шансами нормализуются автоматически

### coordinates
- **Тип**: список строк
- **Формат**: `"x,y,z"` или `"world,x,y,z"`
- **Примеры**:
  - `"100,64,100"` - координаты в дефолтном мире
  - `"world_nether,0,64,0"` - координаты в аду

### mobs-count
- **Тип**: integer
- **Описание**: Количество мобов для спавна в волне

### exceptions
- **Тип**: integer
- **Описание**: Задержка в секундах до следующей волны

### Опциональные параметры

#### health-multiplier
```yaml
health-multiplier: 2.0
```
- **Тип**: double
- **Описание**: Множитель здоровья для мобов этой волны
- **Переопределяет**: default_health_multiplier

#### title
```yaml
title: "&4&lБосс волна!"
```
- **Тип**: string
- **Описание**: Кастомный заголовок BossBar для этой волны
- **Переопределяет**: bossbar.title

#### rewards
```yaml
rewards:
  - "say Волна завершена!"
  - "give @a diamond 1"
  - "eco give @a 100"
```
- **Тип**: список строк
- **Описание**: Команды, выполняемые от консоли при завершении волны
- **Примечание**: Команды выполняются последовательно

## Примеры конфигураций

### Пример 1: Простые волны

```yaml
debug: false
serializer: LEGACY
info_command_permission: "kmobwaves.user"
auto_restart: true
spawn_radius: 3
default_health_multiplier: 1.0

wave_messages:
  enabled: true
  start: "&e>>> Волна %wave% началась!"
  complete: "&a>>> Волна %wave% завершена!"
  all_complete: "&6>>> Все волны завершены!"

sounds:
  enabled: true
  wave_start:
    sound: "ENTITY_ENDER_DRAGON_GROWL"
    volume: 1.0
    pitch: 1.0

bossbar:
  enabled: true
  title: "&6Волна %wave% &7[&e%remaining%&7/&e%total%&7]"
  color: "YELLOW"
  style: "SEGMENTED_10"

Waves:
  - count: 1
    mobs:
      - "Zombie:60"
      - "Skeleton:40"
    coordinates:
      - "0,64,0"
    mobs-count: 10
    exceptions: 10
  
  - count: 2
    mobs:
      - "Creeper:100"
    coordinates:
      - "0,64,0"
    mobs-count: 15
    exceptions: 15
```

### Пример 2: Прогрессивные волны с наградами

```yaml
debug: false
auto_restart: false  # Однократное прохождение
spawn_radius: 5
default_health_multiplier: 1.0

Waves:
  - count: 1
    mobs:
      - "BasicZombie:100"
    coordinates:
      - "world,100,64,100"
    mobs-count: 5
    exceptions: 15
    health-multiplier: 1.0
    title: "&aВолна 1: &7Разминка"
    rewards:
      - "give @a bread 5"
  
  - count: 2
    mobs:
      - "StrongZombie:70"
      - "FastSkeleton:30"
    coordinates:
      - "world,100,64,100"
      - "world,120,64,120"
    mobs-count: 10
    exceptions: 20
    health-multiplier: 1.5
    title: "&eВолна 2: &7Усиление"
    rewards:
      - "give @a iron_sword 1"
      - "give @a cooked_beef 10"
  
  - count: 3
    mobs:
      - "Boss:100"
    coordinates:
      - "world,110,64,110"
    mobs-count: 1
    exceptions: 30
    health-multiplier: 5.0
    title: "&4&lФИНАЛЬНЫЙ БОСС"
    rewards:
      - "give @a diamond 10"
      - "give @a emerald 5"
      - "say &6Поздравляем с победой!"
```

### Пример 3: Отключение визуальных эффектов

```yaml
debug: false
auto_restart: true
spawn_radius: 0  # Точный спавн без разброса

wave_messages:
  enabled: false  # Без сообщений

sounds:
  enabled: false  # Без звуков

bossbar:
  enabled: false  # Без BossBar

Waves:
  - count: 1
    mobs:
      - "SilentMob:100"
    coordinates:
      - "0,64,0"
    mobs-count: 20
    exceptions: 10
```

## Советы по настройке

1. **Используйте debug: true** при первой настройке для отслеживания проблем
2. **Начните с малого** - создайте 1-2 простые волны, протестируйте их
3. **Балансируйте сложность** - используйте health-multiplier для прогрессии
4. **Награды мотивируют** - добавляйте rewards для интересного геймплея
5. **Тестируйте звуки** - неправильные названия звуков будут проигнорированы
6. **Координаты важны** - убедитесь что координаты валидны и безопасны
7. **Шансы гибкие** - не обязательно делать сумму ровно 100

## Устранение проблем

### Звуки не воспроизводятся
- Проверьте правильность названия звука
- Убедитесь что `sounds.enabled: true`
- Проверьте логи с `debug: true`

### BossBar не отображается
- Убедитесь что `bossbar.enabled: true`
- Проверьте правильность color и style
- Игроки должны быть онлайн при старте волны

### Мобы не спавнятся
- Проверьте наличие MythicMobs
- Убедитесь что имена мобов правильные (регистр важен)
- Проверьте координаты и доступность мира
- Включите `debug: true` для деталей

### Волны не переходят
- Убедитесь что все мобы убиты
- Проверьте параметр `exceptions` (должен быть > 0)
- Используйте `/kmobwaves info` для проверки статуса
