# language: es

Característica: Generación de alertas por temperatura
  Como administrador de SmartGuard
  Quiero evaluar las lecturas de un sensor de temperatura
  Para generar alertas cuando se supere el umbral configurado

  Escenario: Generar una alerta cuando la temperatura supera el umbral
    Dado que existe una regla activa con un umbral de 28 grados
    Cuando el sensor registra una temperatura de 30 grados
    Entonces el sistema debe generar una alerta de temperatura alta

  Escenario: No generar una alerta cuando la temperatura es inferior al umbral
    Dado que existe una regla activa con un umbral de 28 grados
    Cuando el sensor registra una temperatura de 26 grados
    Entonces el sistema no debe generar una alerta

  Escenario: No generar una alerta cuando la temperatura es igual al umbral
    Dado que existe una regla activa con un umbral de 28 grados
    Cuando el sensor registra una temperatura de 28 grados
    Entonces el sistema no debe generar una alerta

  Escenario: No generar una alerta cuando la regla está desactivada
    Dado que existe una regla desactivada con un umbral de 28 grados
    Cuando el sensor registra una temperatura de 32 grados
    Entonces el sistema no debe generar una alerta

  Escenario: No generar una alerta cuando el sensor está en mantenimiento
    Dado que existe una regla activa con un umbral de 28 grados
    Y el sensor se encuentra en mantenimiento
    Cuando el sensor registra una temperatura de 32 grados
    Entonces el sistema no debe generar una alerta