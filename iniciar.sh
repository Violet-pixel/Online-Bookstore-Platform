#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if ! command -v docker >/dev/null 2>&1; then
  echo "Instale o Docker (com Docker Compose) e rode de novo: https://docs.docker.com/get-docker/"
  exit 1
fi

aleatorio() { head -c 96 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c "$1"; }

if [ ! -f .env ]; then
  cp .env.example .env
  # a senha sempre leva maiúscula, minúscula e número (exigência do SQL Server)
  sed -i.bak \
    -e "s|^DB_PASSWORD=.*|DB_PASSWORD=Pr$(aleatorio 30)9|" \
    -e "s|^JWT_SECRET=.*|JWT_SECRET=$(aleatorio 64)|" .env
  rm -f .env.bak
  echo "Arquivo .env criado com senha do banco e chave do JWT aleatórias."
fi

# mantém o perfil do Compose igual ao banco escolhido
banco=$(grep -E '^DB_VENDOR=' .env | cut -d= -f2 | tr -d '\r')
banco=${banco:-postgresql}
sed -i.bak -e "s|^COMPOSE_PROFILES=.*|COMPOSE_PROFILES=${banco}|" .env && rm -f .env.bak
echo "Banco escolhido: ${banco}"

docker compose up -d --build

porta=$(grep -E '^APP_PORT=' .env | cut -d= -f2 | tr -d '\r')
porta=${porta:-8080}
printf "Aguardando o Prelo subir"
for _ in $(seq 1 180); do
  if curl -fsS "http://localhost:${porta}/api/health" >/dev/null 2>&1; then
    echo
    echo "Pronto!"
    echo "  Loja:             http://localhost:${porta}"
    echo "  Documentação API: http://localhost:${porta}/api/docs"
    echo "  Contas: leitora@prelo.dev ou admin@prelo.dev, senha Prelo@2026"
    exit 0
  fi
  printf "."
  sleep 2
done
echo
echo "O Prelo ainda está subindo (o Oracle e o SQL Server demoram mais na primeira vez). Acompanhe com: docker compose logs -f api"
