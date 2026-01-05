<div align="center">

# 🌊 kMobWaves

**Плагин для управления волнами мобов на Minecraft серверах**

[![Build Status](https://img.shields.io/github/actions/workflow/status/Kanorto/kMobWaves/build.yml?branch=main&style=for-the-badge&logo=github&label=Build)](https://github.com/Kanorto/kMobWaves/actions/workflows/build.yml)
[![Latest Release](https://img.shields.io/github/v/release/Kanorto/kMobWaves?style=for-the-badge&logo=github&label=Release)](https://github.com/Kanorto/kMobWaves/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Kanorto/kMobWaves/total?style=for-the-badge&logo=github&label=Downloads)](https://github.com/Kanorto/kMobWaves/releases)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red?style=for-the-badge)](LICENSE)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-green?style=for-the-badge&logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![Paper](https://img.shields.io/badge/Paper-1.21+-blue?style=for-the-badge)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21+-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)

---

**[📥 Скачать](https://github.com/Kanorto/kMobWaves/releases/latest)** • 
**[📖 Документация](#-документация)** • 
**[🐛 Баг-репорты](https://github.com/Kanorto/kMobWaves/issues)** • 
**[💬 Поддержка](https://t.me/kapybarkaaa)**

</div>

---

## ✨ Особенности

<table>
<tr>
<td width="50%">

### 🎮 Геймплей
- 🌊 **Бесконечные волны** — настраиваемые волны мобов
- 🎲 **Шанс спавна** — гибкая настройка шансов появления мобов
- 🗺️ **Мультикоординаты** — множество точек спавна
- ❤️ **Здоровье мобов** — настраиваемый множитель HP
- 🔄 **Автоперезапуск** — автоматический рестарт цикла волн

</td>
<td width="50%">

### ⚙️ Система
- 📊 **BossBar** — красивый индикатор прогресса волны
- 🔦 **Подсветка мобов** — визуализация активных мобов
- 🎵 **Звуковые эффекты** — настраиваемые звуки событий
- 🏆 **Награды** — команды при завершении волны
- 📝 **PlaceholderAPI** — интеграция с другими плагинами

</td>
</tr>
</table>

---

## 📋 Требования

| Компонент | Версия | Обязательно |
|-----------|--------|:-----------:|
| **Minecraft** | 1.21+ | ✅ |
| **Paper** (или форки) | 1.21+ | ✅ |
| **Java** | 21+ | ✅ |
| **MythicMobs** | 5.6.1+ | ✅ |
| **PlaceholderAPI** | 2.11.6+ | ❌ |
| **ProtocolLib** | 5.4.0+ | ❌ |

> **Примечание:** PlaceholderAPI требуется для работы плейсхолдеров, ProtocolLib — для приватной подсветки мобов.

---

## 📥 Установка

1. **Скачайте** последнюю версию из [Releases](https://github.com/Kanorto/kMobWaves/releases/latest)
2. **Поместите** JAR-файл в папку `plugins/` вашего сервера
3. **Установите** MythicMobs (обязательно)
4. **Перезапустите** сервер
5. **Настройте** плагин в `plugins/kMobWaves/config.yml`

---

## 📖 Документация

### 🎯 Команды

| Команда | Описание | Право |
|---------|----------|-------|
| `/kmobwaves reload` | Перезагрузка конфигурации | `kmobwaves.admin` |
| `/kmobwaves start` | Запуск волн мобов | `kmobwaves.admin` |
| `/kmobwaves stop` | Остановка волн | `kmobwaves.admin` |
| `/kmobwaves force_start <номер>` | Запуск с определённой волны | `kmobwaves.admin` |
| `/kmobwaves info` | Информация о текущей волне | Настраивается в конфиге |
| `/kmobwaves highlight` | Подсветка активных мобов | Настраивается в конфиге |

> **Алиас:** Все команды также работают через `/kmw`

### 🔑 Права

| Право | Описание |
|-------|----------|
| `kmobwaves.admin` | Административные команды |
| `kmobwaves.user` | Команда info (по умолчанию) |
| `kmobwaves.highlight` | Команда highlight (режим ALL) |
| `kmobwaves.bossbar` | Видеть BossBar (режим ADMIN) |

---

### ⚙️ Конфигурация

<details>
<summary><b>📁 config.yml — Основные настройки</b></summary>

```yaml
# Режим отладки
debug: false

# Система цветов: LEGACY, LEGACY_ADVANCED, MINIMESSAGE
serializer: LEGACY

# Право для команды info
info_command_permission: "kmobwaves.user"

# Автоматический перезапуск волн
auto_restart: true

# Радиус разброса спавна мобов (в блоках)
spawn_radius: 5

# Множитель здоровья мобов по умолчанию
default_health_multiplier: 1.0
```

</details>

<details>
<summary><b>💬 Сообщения волн</b></summary>

```yaml
wave_messages:
  enabled: true
  start: "&e&l>>> &6Волна %wave% началась! &e&l<<<"
  complete: "&a&l>>> &2Волна %wave% завершена! Следующая через %delay% сек. &a&l<<<"
  all_complete: "&6&l>>> Все волны завершены! Перезапуск... &6&l<<<"
```

**Плейсхолдеры:**
- `%wave%` — номер текущей волны
- `%next_wave%` — номер следующей волны
- `%delay%` — задержка до следующей волны

</details>

<details>
<summary><b>🎵 Звуковые эффекты</b></summary>

```yaml
sounds:
  enabled: true
  wave_start:
    sound: "ENTITY_ENDER_DRAGON_GROWL"
    volume: 1.0
    pitch: 1.0
  wave_complete:
    sound: "UI_TOAST_CHALLENGE_COMPLETE"
    volume: 1.0
    pitch: 1.0
  mob_death:
    sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
    volume: 0.5
    pitch: 1.2
```

</details>

<details>
<summary><b>📊 BossBar</b></summary>

```yaml
bossbar:
  # Режим: ALL, ADMIN, NONE
  mode: "ALL"
  title: "&6Волна %wave% &7- &eОсталось: %remaining%/%total%"
  color: "YELLOW"  # BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW
  style: "SEGMENTED_10"  # SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
```

**Плейсхолдеры:**
- `%wave%` — номер волны
- `%remaining%` — осталось мобов
- `%total%` — всего мобов в волне

</details>

<details>
<summary><b>🔦 Подсветка мобов</b></summary>

```yaml
highlight:
  # Режим: ALL, ADMIN
  # ALL — любой игрок с kmobwaves.highlight может подсвечивать (видно всем)
  # ADMIN — только администраторы (с ProtocolLib видно только им)
  mode: "ADMIN"
```

</details>

<details>
<summary><b>🌊 Настройка волн</b></summary>

```yaml
Waves:
  - count: 1                    # Номер волны
    mobs:                       # Список мобов
      - "MobName:50"            # Формат: "Имя_Моба:шанс"
      - "AnotherMob:50"
    coordinates:                # Координаты спавна
      - "100,100,100"           # Формат: x,y,z
      - "world,200,100,200"     # Формат: world,x,y,z
    mobs-count: 15              # Количество мобов
    exceptions: 10              # Задержка (секунды)
    # health-multiplier: 1.5    # Необязательно: множитель HP
    # title: "&cПервая волна!"  # Необязательно: заголовок BossBar
    # rewards:                  # Необязательно: команды
    #   - "say Волна завершена!"
    #   - "give @a diamond 1"
```

</details>

---

### 🔗 PlaceholderAPI

После установки PlaceholderAPI доступны следующие плейсхолдеры:

| Плейсхолдер | Описание |
|-------------|----------|
| `%kMobWaves_wave%` | Номер текущей волны |
| `%kMobWaves_count%` | Количество оставшихся мобов |

---

## 🔧 Сборка из исходников

```bash
# Клонирование репозитория
git clone https://github.com/Kanorto/kMobWaves.git
cd kMobWaves

# Сборка с Gradle
./gradlew clean build

# Или с Maven
mvn clean package
```

Собранный JAR будет в `build/libs/` (Gradle) или `target/` (Maven).

---

## 📜 История изменений

### 📦 v2026.01.04.2223 (Последняя)
- 🧹 Очистка кодовой базы
- 📄 Удаление неактуальных markdown файлов

### 📦 v2025.11.13.1956
- 🐛 Исправлены предупреждения null serializer
- 🐛 Исправлена рассинхронизация счётчика мобов в BossBar
- ♻️ Рефакторинг логики обновления BossBar

### 📦 v2025.11.12.2024
- 🐛 Исправлен краш null serializer в ProtocolLib
- 🔧 Улучшена обработка подсветки мобов

### 📦 v2025.11.12.1954
- 🐛 Исправлена ClassCastException в ProtocolLib highlight
- 🔧 Корректное использование metadata serializers

### 📦 v2025.11.12.1908
- ✨ Добавлен автоматический Maven релиз при пуше в main
- 🔧 Улучшена CI/CD интеграция

### 📦 v1.1.2
- 🐛 Исправления ошибок
- 🔧 Улучшения стабильности

### 📦 v1.1.1
- ✨ Расширенная система конфигурации
- ✨ Режимы BossBar (ALL, ADMIN, NONE)
- ✨ Команда highlight для администраторов
- ✨ Улучшенное управление волнами

---

## 🤝 Поддержка

<div align="center">

| Канал | Ссылка |
|-------|--------|
| 💬 **Telegram** | [@kapybarkaaa](https://t.me/kapybarkaaa) |
| 🐛 **Issues** | [GitHub Issues](https://github.com/Kanorto/kMobWaves/issues) |
| 📧 **Автор** | vv0t3afa9 |

</div>

---

<div align="center">

**Сделано с ❤️ для Minecraft сообщества**

[![GitHub stars](https://img.shields.io/github/stars/Kanorto/kMobWaves?style=social)](https://github.com/Kanorto/kMobWaves/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/Kanorto/kMobWaves?style=social)](https://github.com/Kanorto/kMobWaves/network/members)

</div>
