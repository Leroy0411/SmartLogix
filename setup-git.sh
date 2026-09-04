#!/bin/bash
# ═══════════════════════════════════════════════════════════════
#  setup-git.sh — Inicializa el monorepo SmartLogix con Git Flow
#  Uso: chmod +x setup-git.sh && ./setup-git.sh https://github.com/TU_USUARIO/smartlogix.git
#
#  Nota: esta entrega YA incluye un historial de Git real generado con la
#  misma secuencia de este script (revísalo con `git log --oneline --graph
#  --all`). Este archivo queda como referencia/documentación de la
#  estrategia de branching y por si necesitas reconstruir el historial
#  contra un repositorio remoto nuevo desde cero.
# ═══════════════════════════════════════════════════════════════

set -e

REPO_URL="${1}"

if [ -z "$REPO_URL" ]; then
  echo "Uso: ./setup-git.sh https://github.com/TU_USUARIO/smartlogix.git"
  exit 1
fi

echo "🚚 Inicializando repositorio Git — SmartLogix Parcial 3"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

commit() { git commit -q -m "$1"; }
merge_into_develop() {
  git checkout -q develop
  git merge -q --no-ff "$1" -m "$2"
  git push origin develop
  echo "✅ $1 mergeada"
}

# ── 1. Inicializar repositorio ─────────────────────────────────
git init
git remote add origin "$REPO_URL"

git add .gitignore .gitattributes
commit "chore: inicializar repositorio SmartLogix (Evaluación Parcial 3)"
git branch -M main
git push -u origin main

git checkout -b develop
git push -u origin develop
echo "✅ Ramas main y develop creadas"

# ── 2. Arquetipo Maven ──────────────────────────────────────────
git checkout -b feature/arquetipos-maven
git add arquetipos-maven/
commit "feat(arquetipos): agregar arquetipo Maven donaton-microservice-archetype"
merge_into_develop feature/arquetipos-maven "merge: integrar arquetipo Maven base"

# ── 3. MS-Inventario: Repository Pattern ─────────────────────────
git checkout -b feature/ms-inventario-repository
git add ms-inventario/src/main/java/donaton/msinventario/model/ \
        ms-inventario/src/main/java/donaton/msinventario/repository/
commit "feat(ms-inventario): implementar Repository Pattern con ProductoRepository (Spring Data JPA)"
merge_into_develop feature/ms-inventario-repository "merge: integrar Repository Pattern en ms-inventario"

# ── 4. MS-Inventario: Factory Method ─────────────────────────────
git checkout -b feature/ms-inventario-factory
git add ms-inventario/src/main/java/donaton/msinventario/factory/
commit "feat(ms-inventario): agregar Factory Method para tipos de producto"
merge_into_develop feature/ms-inventario-factory "merge: integrar Factory Method en ms-inventario"

# ── 5. MS-Inventario: persistencia JPA + Swagger + API ───────────
git checkout -b feature/ms-inventario-jpa-persistence
git add ms-inventario/pom.xml ms-inventario/README.md \
        ms-inventario/src/main/resources/ \
        ms-inventario/src/main/java/donaton/msinventario/MsInventarioApplication.java \
        ms-inventario/src/main/java/donaton/msinventario/service/ \
        ms-inventario/src/main/java/donaton/msinventario/controller/ \
        ms-inventario/src/main/java/donaton/msinventario/dto/
commit "feat(ms-inventario): persistencia JPA + H2 (archivo), Swagger/OpenAPI y contrato REST"
merge_into_develop feature/ms-inventario-jpa-persistence "merge: integrar persistencia JPA real y API REST en ms-inventario"

# ── 6. MS-Inventario: pruebas ────────────────────────────────────
git checkout -b feature/ms-inventario-tests
git add ms-inventario/src/test/java/donaton/msinventario/controller/ \
        ms-inventario/src/test/java/donaton/msinventario/factory/ \
        ms-inventario/src/test/java/donaton/msinventario/repository/ \
        ms-inventario/src/test/java/donaton/msinventario/service/ \
        ms-inventario/src/test/resources/
commit "test(ms-inventario): agregar pruebas de Service, Controller, Factories y Repository JPA"
merge_into_develop feature/ms-inventario-tests "merge: integrar pruebas unitarias de ms-inventario"

# ── 7. MS-Inventario: seguridad ───────────────────────────────────
git checkout -b feature/ms-inventario-security
git add ms-inventario/src/main/java/donaton/msinventario/config/ \
        ms-inventario/src/main/java/donaton/msinventario/exception/ \
        ms-inventario/src/main/java/donaton/msinventario/security/ \
        ms-inventario/src/test/java/donaton/msinventario/security/
commit "feat(ms-inventario): restringir CORS, exigir X-API-KEY en escrituras y manejar excepciones globalmente"
merge_into_develop feature/ms-inventario-security "merge: endurecer seguridad de ms-inventario"

# ── 8. MS-Pedidos: Repository Pattern ─────────────────────────────
git checkout -b feature/ms-pedidos-repository
git add ms-pedidos/src/main/java/donaton/mspedidos/model/ \
        ms-pedidos/src/main/java/donaton/mspedidos/repository/
commit "feat(ms-pedidos): implementar Repository Pattern para CentroDistribucion y Pedido"
merge_into_develop feature/ms-pedidos-repository "merge: integrar Repository Pattern en ms-pedidos"

# ── 9. MS-Pedidos: Observer Pattern ───────────────────────────────
git checkout -b feature/ms-pedidos-observer
git add ms-pedidos/src/main/java/donaton/mspedidos/observer/
commit "feat(ms-pedidos): agregar Observer Pattern (auditoría + notificaciones)"
merge_into_develop feature/ms-pedidos-observer "merge: integrar Observer Pattern en ms-pedidos"

# ── 10. MS-Pedidos: persistencia JPA + reglas de negocio ─────────
git checkout -b feature/ms-pedidos-jpa-persistence
git add ms-pedidos/pom.xml ms-pedidos/README.md \
        ms-pedidos/src/main/resources/ \
        ms-pedidos/src/main/java/donaton/mspedidos/MsPedidosApplication.java \
        ms-pedidos/src/main/java/donaton/mspedidos/service/ \
        ms-pedidos/src/main/java/donaton/mspedidos/controller/
commit "feat(ms-pedidos): persistencia JPA + H2, reserva de capacidad del centro y transiciones de estado válidas"
merge_into_develop feature/ms-pedidos-jpa-persistence "merge: integrar persistencia JPA y reglas de negocio en ms-pedidos"

# ── 11. MS-Pedidos: pruebas ───────────────────────────────────────
git checkout -b feature/ms-pedidos-tests
git add ms-pedidos/src/test/java/donaton/mspedidos/repository/ \
        ms-pedidos/src/test/java/donaton/mspedidos/observer/ \
        ms-pedidos/src/test/java/donaton/mspedidos/service/PedidoServiceTest.java \
        ms-pedidos/src/test/java/donaton/mspedidos/controller/PedidoControllerTest.java \
        ms-pedidos/src/test/resources/
commit "test(ms-pedidos): agregar pruebas de Service, Controller, Observers y Repository JPA"
merge_into_develop feature/ms-pedidos-tests "merge: integrar pruebas unitarias de ms-pedidos"

# ── 12. Integración MS-Pedidos -> MS-Inventario (Circuit Breaker) ─
git checkout -b feature/ms-pedidos-inventario-integration
git add ms-pedidos/src/main/java/donaton/mspedidos/client/ \
        ms-pedidos/src/test/java/donaton/mspedidos/client/
commit "feat(ms-pedidos): descontar stock en MS-Inventario al despachar un pedido (Circuit Breaker)"
merge_into_develop feature/ms-pedidos-inventario-integration "merge: integrar descuento de stock real al despachar pedidos"

# ── 13. MS-Pedidos: seguridad ──────────────────────────────────────
git checkout -b feature/ms-pedidos-security
git add ms-pedidos/src/main/java/donaton/mspedidos/config/ \
        ms-pedidos/src/main/java/donaton/mspedidos/exception/ \
        ms-pedidos/src/main/java/donaton/mspedidos/security/ \
        ms-pedidos/src/test/java/donaton/mspedidos/security/
commit "feat(ms-pedidos): restringir CORS, exigir X-API-KEY en escrituras y manejar excepciones globalmente"
merge_into_develop feature/ms-pedidos-security "merge: endurecer seguridad de ms-pedidos"

# ── 14. BFF ─────────────────────────────────────────────────────
git checkout -b feature/bff-donaton
git add bff-donaton/pom.xml bff-donaton/README.md \
        bff-donaton/src/main/java/donaton/bff/BffDonatonApplication.java \
        bff-donaton/src/main/java/donaton/bff/dto/ \
        bff-donaton/src/main/java/donaton/bff/client/ \
        bff-donaton/src/main/java/donaton/bff/service/ \
        bff-donaton/src/main/java/donaton/bff/controller/ \
        bff-donaton/src/main/resources/
commit "feat(bff): implementar Backend For Frontend con Circuit Breaker sobre MS-Inventario y MS-Pedidos"
merge_into_develop feature/bff-donaton "merge: integrar BFF con agregación de dashboard y resiliencia a fallos parciales"

# ── 15. BFF: pruebas ────────────────────────────────────────────
git checkout -b feature/bff-tests
git add bff-donaton/src/test/
commit "test(bff): agregar BffServiceTest, BffControllerTest e InventarioClientCircuitBreakerTest"
merge_into_develop feature/bff-tests "merge: integrar pruebas unitarias del BFF"

# ── 16. BFF: seguridad (CORS) ────────────────────────────────────
git checkout -b feature/bff-security
git add bff-donaton/src/main/java/donaton/bff/config/
commit "feat(bff): restringir CORS a un origen configurable"
merge_into_develop feature/bff-security "merge: endurecer CORS del BFF"

# ── 17. Frontend ──────────────────────────────────────────────────
git checkout -b feature/frontend-smartlogix
git add frontend-donaton/package.json frontend-donaton/package-lock.json \
        frontend-donaton/index.html frontend-donaton/vite.config.js \
        frontend-donaton/README.md frontend-donaton/.env.example \
        frontend-donaton/src/App.jsx frontend-donaton/src/main.jsx \
        frontend-donaton/src/components/Dashboard.jsx \
        frontend-donaton/src/components/ProductoForm.jsx \
        frontend-donaton/src/hooks/useProductos.js \
        frontend-donaton/src/services/donatonApi.js
commit "feat(frontend): agregar SPA React con Dashboard, registro de productos y Facade/Custom Hook"
merge_into_develop feature/frontend-smartlogix "merge: integrar frontend React con Vite"

# ── 18. Frontend: pruebas ────────────────────────────────────────
git checkout -b feature/frontend-tests
git add frontend-donaton/src/test/ frontend-donaton/src/App.test.jsx \
        frontend-donaton/src/components/Dashboard.test.jsx \
        frontend-donaton/src/components/ProductoForm.test.jsx \
        frontend-donaton/src/hooks/useProductos.test.js \
        frontend-donaton/src/services/donatonApi.test.js
commit "test(frontend): agregar pruebas con Vitest + React Testing Library (~97% cobertura)"
merge_into_develop feature/frontend-tests "merge: integrar pruebas unitarias del frontend"

# ── 19. Docker Compose ────────────────────────────────────────────
git checkout -b feature/docker-compose
git add docker-compose.yml \
        ms-inventario/Dockerfile ms-inventario/.dockerignore \
        ms-pedidos/Dockerfile ms-pedidos/.dockerignore \
        bff-donaton/Dockerfile bff-donaton/.dockerignore \
        frontend-donaton/Dockerfile frontend-donaton/.dockerignore
commit "feat(infra): agregar docker-compose para levantar toda la plataforma con un solo comando"
merge_into_develop feature/docker-compose "merge: integrar docker-compose para la demo en vivo"

# ── 20. Documentación ──────────────────────────────────────────
git checkout -b feature/documentacion
git add documentacion/ README.md setup-git.sh
commit "docs: agregar PDFs de análisis de patrones, plan de branching y README final"
merge_into_develop feature/documentacion "merge: integrar documentación final del Parcial 3"

# ── 21. Release a main ─────────────────────────────────────────
git checkout main
git merge --no-ff develop \
  -m "release: v2.1.0 Parcial 3 — BFF + 2 microservicios integrados, seguridad, cobertura mínima verificada y docker-compose"
git tag -a v2.1.0 -m "Parcial 3 entregado — Factory Method, Repository Pattern (JPA+H2), Observer, BFF, Circuit Breaker, seguridad, docker-compose"
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
git log --oneline --graph --all | head -60
