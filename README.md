# Explore With Me

## О проекте
Приложение «Explore With Me» позволяет пользователям делиться информацией об интересных событиях и находить компанию для участия в них.

## Из чего состоит
Приложение состоит из двух модулей:
- Сервис статистики
- Основной сервис

## Сервис статистики
Позволяет собирать статистику об обращениях к эндпоинтам.

**Спецификация API:** [Stats Service API](https://github.com/galievranis/java-explore-with-me/blob/20c5e4a016a4aa2f7cd62ba3ebbd4474e95f150f/ewm-stats-service-spec.json)

## Основной сервис
Позволяет создавать, обновлять, удалять события и подборки событий. Реализован функционал по запросам на участие пользователя на конкретном событии.

**Спецификация API:** [Main Service API](https://github.com/galievranis/java-explore-with-me/blob/20c5e4a016a4aa2f7cd62ba3ebbd4474e95f150f/ewm-main-service-spec.json)

## Дополнительный функционал: Комментарии
Реализована возможность оставлять комментарии к событиям. А также возможность ставить лайки и дизлайки к комментариям.

**Спецификация API:** [Comment API](https://github.com/galievranis/java-explore-with-me/blob/20c5e4a016a4aa2f7cd62ba3ebbd4474e95f150f/ewm-main-service-feature-spec.json)

## Стек технологий
* Spring Boot
* PostgreSQL
* WebClient
* Docker
