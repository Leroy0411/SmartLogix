#!/bin/bash
# ═══════════════════════════════════════════════════════════════
#  setup-git.sh — Inicializa el monorepo Donaton con Git Flow
#  Uso: chmod +x setup-git.sh && ./setup-git.sh
# ═══════════════════════════════════════════════════════════════

set -e

REPO_URL="${1}"

if [ -z "$REPO_URL" ]; then
  echo "Uso: ./setup-git.sh https://github.com/TU_USUARIO/donaton.git"
  exit 1
fi

echo "🚀 Inicializando repositorio Git — Donaton Parcial 3"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# ── 1. Inicializar repositorio ─────────────────────────────────
git init
git remote add origin "$REPO_URL"

# ── 2. Commit inicial en main ──────────────────────────────────
git add .
git commit -m "chore: estructura inicial del monorepo Donaton Parcial 3"
git branch -M main

# ── 3. Crear rama develop ──────────────────────────────────────
git checkout -b develop
git push -u origin main
git push -u origin develop

echo "✅ Ramas main y develop creadas"

# ── 4. Feature: MS-Donaciones Repository Pattern ───────────────
git checkout -b feature/ms-donaciones-repository
git add ms-donaciones/src/main/java/donaton/msdonaciones/repository/
git add ms-donaciones/src/main/java/donaton/msdonaciones/model/
git commit -m "feat(ms-donaciones): implementar Repository Pattern con DonacionRepository (Spring Data JPA)"
git checkout develop
git merge --no-ff feature/ms-donaciones-repository \
  -m "merge: integrar Repository Pattern en ms-donaciones"
git push origin develop
echo "✅ feature/ms-donaciones-repository mergeada"

# ── 5. Feature: MS-Donaciones Factory Method ───────────────────
git checkout -b feature/ms-donaciones-factory
git add ms-donaciones/src/main/java/donaton/msdonaciones/factory/
git commit -m "feat(ms-donaciones): agregar Factory Method para tipos de donación (ROPA, ALIMENTO, MEDICO, HIGIENE)"
git checkout develop
git merge --no-ff feature/ms-donaciones-factory \
  -m "merge: integrar Factory Method para tipos de donación"
git push origin develop
echo "✅ feature/ms-donaciones-factory mergeada"

# ── 6. Feature: MS-Donaciones persistencia JPA + Swagger ───────
git checkout -b feature/ms-donaciones-jpa-persistence
git add ms-donaciones/pom.xml
git add ms-donaciones/src/main/resources/
git add ms-donaciones/src/main/java/donaton/msdonaciones/MsDonacionesApplication.java
git add ms-donaciones/src/main/java/donaton/msdonaciones/service/
git add ms-donaciones/src/main/java/donaton/msdonaciones/controller/
git commit -m "feat(ms-donaciones): migrar persistencia a Spring Data JPA + H2 (archivo) y agregar Swagger/OpenAPI"
git checkout develop
git merge --no-ff feature/ms-donaciones-jpa-persistence \
  -m "merge: integrar persistencia JPA real y documentación OpenAPI en ms-donaciones"
git push origin develop
echo "✅ feature/ms-donaciones-jpa-persistence mergeada"

# ── 7. Feature: MS-Donaciones pruebas ───────────────────────────
git checkout -b feature/ms-donaciones-tests
git add ms-donaciones/src/test/
git add ms-donaciones/README.md
git commit -m "test(ms-donaciones): agregar pruebas de Service, Controller, Factories y Repository JPA con JUnit5+Mockito"
git checkout develop
git merge --no-ff feature/ms-donaciones-tests \
  -m "merge: integrar pruebas unitarias e integración de ms-donaciones"
git push origin develop
echo "✅ feature/ms-donaciones-tests mergeada"

# ── 8. Feature: MS-Logística Repository Pattern ────────────────
git checkout -b feature/ms-logistica-repository
git add ms-logistica/src/main/java/donaton/mslogistica/repository/
git add ms-logistica/src/main/java/donaton/mslogistica/model/
git commit -m "feat(ms-logistica): implementar Repository Pattern para CentroAcopio y Envio (Spring Data JPA)"
git checkout develop
git merge --no-ff feature/ms-logistica-repository \
  -m "merge: integrar Repository Pattern en ms-logistica"
git push origin develop
echo "✅ feature/ms-logistica-repository mergeada"

# ── 9. Feature: MS-Logística Observer Pattern ──────────────────
git checkout -b feature/ms-logistica-observer
git add ms-logistica/src/main/java/donaton/mslogistica/observer/
git commit -m "feat(ms-logistica): agregar Observer Pattern con AuditoriaEnvioObserver y NotificacionEnvioObserver"
git checkout develop
git merge --no-ff feature/ms-logistica-observer \
  -m "merge: integrar Observer Pattern para notificación de cambios de estado"
git push origin develop
echo "✅ feature/ms-logistica-observer mergeada"

# ── 10. Feature: MS-Logística persistencia JPA + Swagger ───────
git checkout -b feature/ms-logistica-jpa-persistence
git add ms-logistica/pom.xml
git add ms-logistica/src/main/resources/
git add ms-logistica/src/main/java/donaton/mslogistica/MsLogisticaApplication.java
git add ms-logistica/src/main/java/donaton/mslogistica/service/
git add ms-logistica/src/main/java/donaton/mslogistica/controller/
git commit -m "feat(ms-logistica): migrar persistencia a Spring Data JPA + H2 (archivo) y agregar Swagger/OpenAPI"
git checkout develop
git merge --no-ff feature/ms-logistica-jpa-persistence \
  -m "merge: integrar persistencia JPA real y documentación OpenAPI en ms-logistica"
git push origin develop
echo "✅ feature/ms-logistica-jpa-persistence mergeada"

# ── 11. Feature: MS-Logística pruebas ───────────────────────────
git checkout -b feature/ms-logistica-tests
git add ms-logistica/src/test/
git add ms-logistica/README.md
git commit -m "test(ms-logistica): agregar pruebas de Service, Controller, Observers y Repository JPA con JUnit5+Mockito"
git checkout develop
git merge --no-ff feature/ms-logistica-tests \
  -m "merge: integrar pruebas unitarias e integración de ms-logistica"
git push origin develop
echo "✅ feature/ms-logistica-tests mergeada"

# ── 12. Feature: BFF ───────────────────────────────────────────
git checkout -b feature/bff-donaton
git add bff-donaton/src/main/java/donaton/bff/BffDonatonApplication.java
git add bff-donaton/src/main/java/donaton/bff/dto/
git add bff-donaton/src/main/java/donaton/bff/service/
git add bff-donaton/pom.xml
git commit -m "feat(bff): implementar Backend For Frontend que agrega MS-Donaciones y MS-Logistica en un solo endpoint"
git checkout develop
git merge --no-ff feature/bff-donaton \
  -m "merge: integrar BFF con DashboardResumenDTO y resiliencia a fallos parciales"
git push origin develop
echo "✅ feature/bff-donaton mergeada"

# ── 13. Feature: BFF Swagger + Controller ───────────────────────
git checkout -b feature/bff-swagger
git add bff-donaton/src/main/java/donaton/bff/controller/
git add bff-donaton/src/main/resources/
git commit -m "feat(bff): agregar Swagger/OpenAPI y anotaciones de documentación al BffController"
git checkout develop
git merge --no-ff feature/bff-swagger \
  -m "merge: integrar documentación OpenAPI en el BFF"
git push origin develop
echo "✅ feature/bff-swagger mergeada"

# ── 14. Feature: BFF pruebas ────────────────────────────────────
git checkout -b feature/bff-tests
git add bff-donaton/src/test/
git add bff-donaton/README.md
git commit -m "test(bff): agregar BffServiceTest y BffControllerTest"
git checkout develop
git merge --no-ff feature/bff-tests \
  -m "merge: integrar pruebas unitarias del BFF"
git push origin develop
echo "✅ feature/bff-tests mergeada"

# ── 15. Feature: Frontend ──────────────────────────────────────
git checkout -b feature/frontend-donaton
git add frontend-donaton/src/components/Dashboard.jsx
git add frontend-donaton/src/components/DonacionForm.jsx
git add frontend-donaton/src/hooks/
git add frontend-donaton/src/services/donatonApi.js
git add frontend-donaton/src/App.jsx
git add frontend-donaton/src/main.jsx
git add frontend-donaton/vite.config.js
git add frontend-donaton/index.html
git commit -m "feat(frontend): agregar SPA React con Dashboard (BFF) y formulario de donaciones usando Facade pattern"
git checkout develop
git merge --no-ff feature/frontend-donaton \
  -m "merge: integrar frontend React con Vite"
git push origin develop
echo "✅ feature/frontend-donaton mergeada"

# ── 16. Feature: Frontend pruebas ───────────────────────────────
git checkout -b feature/frontend-tests
git add frontend-donaton/src/test/
git add frontend-donaton/src/services/donatonApi.test.js
git add frontend-donaton/src/hooks/useDonaciones.test.js
git add frontend-donaton/src/components/Dashboard.test.jsx
git add frontend-donaton/src/components/DonacionForm.test.jsx
git add frontend-donaton/src/App.test.jsx
git add frontend-donaton/package.json
git add frontend-donaton/README.md
git commit -m "test(frontend): agregar pruebas con Vitest + React Testing Library (~97% cobertura)"
git checkout develop
git merge --no-ff feature/frontend-tests \
  -m "merge: integrar pruebas unitarias del frontend"
git push origin develop
echo "✅ feature/frontend-tests mergeada"

# ── 17. Feature: Arquetipos Maven ──────────────────────────────
git checkout -b feature/arquetipos-maven
git add arquetipos-maven/
git commit -m "feat(arquetipos): agregar arquetipo Maven para generar nuevos microservicios Donaton con estructura base"
git checkout develop
git merge --no-ff feature/arquetipos-maven \
  -m "merge: integrar arquetipo Maven donaton-microservice-archetype"
git push origin develop
echo "✅ feature/arquetipos-maven mergeada"

# ── 18. Documentación ──────────────────────────────────────────
git checkout -b feature/documentacion
git add documentacion/
git add README.md
git commit -m "docs: agregar PDFs de análisis de patrones, plan de branching, repositorios y reporte de cobertura del frontend"
git checkout develop
git merge --no-ff feature/documentacion \
  -m "merge: integrar documentación final del Parcial 3"
git push origin develop
echo "✅ feature/documentacion mergeada"

# ── 19. Release a main ─────────────────────────────────────────
git checkout main
git merge --no-ff develop \
  -m "release: v2.0.0 Parcial 3 — BFF + 2 microservicios con persistencia JPA real + Swagger + 3 patrones de diseño + pruebas unitarias con cobertura >60%"
git tag -a v2.0.0 -m "Parcial 3 entregado — Factory Method, Repository Pattern (JPA+H2), Observer, BFF, Swagger/OpenAPI"
git push origin main
git push origin --tags

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Repositorio listo en: $REPO_URL"
echo ""
echo "Ramas creadas:"
git branch -a
echo ""
echo "Historial:"
git log --oneline --graph --all | head -40
