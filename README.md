# Hollow Flayer (Forge 1.20.1)

Хоррор-моб: крик, атака, захват и разрыв игрока, вторая фаза со срыванием кожи, надевание скина жертвы.

## Сборка
- Локально: `gradle build` (Gradle 8.x, JDK 17), jar появится в `build/libs/`.
- GitHub Actions: пуш в `main` запускает `.github/workflows/build.yml`, jar лежит в Actions -> run -> Artifacts.
  Тег `v*` (например `git tag v1.0.0 && git push --tags`) прикрепит jar к GitHub Release.

## Запуск
`gradle runClient`. Моб: яйцо призыва в креативе или `/summon hollowflayer:flayer`.

## Модель и текстуры
- `python3 tools/gen_model.py` перегенерирует `FlayerRawModel.java` и `flayer.bbmodel`.
- `python3 tools/gen_textures.py` перегенерирует текстуры (нужен Pillow).
