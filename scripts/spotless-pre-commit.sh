#!/bin/sh
set -e

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Trap pour afficher un message d'erreur en cas d'échec
trap 'echo "${RED}❌ Spotless formatting FAILED! Please check the errors above.${NC}" >&2; exit 1' ERR

echo "${BLUE}➕ Spotless pre-commit (Kotlin only on staged files)${NC}"

# 1. Récupérer les fichiers .kt/.kts déjà stagés
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep -E '\.(kt|kts)$' || true)

if [ -z "$STAGED_FILES" ]; then
  echo "${YELLOW}No Kotlin files staged. Skipping Spotless.${NC}"
  exit 0
fi

echo "${BLUE}Staged Kotlin files:${NC}"
echo "$STAGED_FILES"

# 2. Lancer spotlessApply sur tout le projet
./gradlew spotlessApply

# 3. Ré-ajouter uniquement les fichiers stagés au départ
echo "$STAGED_FILES" | xargs git add

echo "${GREEN}✅ Spotless done and staged files updated.${NC}"
