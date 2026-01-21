#!/bin/bash

# Script para executar testes do FinanCerto Analytics
# Uso: ./run_tests.sh [opção]

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funções
print_header() {
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}════════════════════════════════════════${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Verificar se pytest está instalado
check_pytest() {
    if ! command -v pytest &> /dev/null; then
        print_error "pytest não está instalado"
        echo "Instale com: pip install -r requirements-dev.txt"
        exit 1
    fi
    print_success "pytest encontrado"
}

# Rodar todos os testes
run_all_tests() {
    print_header "Executando TODOS os testes"
    pytest -v --tb=short
}

# Rodar com cobertura
run_with_coverage() {
    print_header "Executando testes COM COBERTURA"
    pytest --cov=app --cov-report=term-missing --cov-report=html -v
    echo ""
    print_success "Relatório HTML gerado em: htmlcov/index.html"
}

# Rodar apenas testes rápidos
run_fast_tests() {
    print_header "Executando apenas testes RÁPIDOS"
    pytest -v -m "not slow" --tb=short
}

# Rodar apenas um arquivo
run_single_file() {
    local file=$1
    if [ -f "tests/$file" ]; then
        print_header "Executando testes de: $file"
        pytest "tests/$file" -v --tb=short
    else
        print_error "Arquivo não encontrado: tests/$file"
        echo "Arquivos disponíveis:"
        ls -la tests/test_*.py | awk '{print "  - " $NF}'
        exit 1
    fi
}

# Rodar teste específico
run_specific_test() {
    local test=$1
    print_header "Executando teste específico: $test"
    pytest -v -k "$test" --tb=short
}

# Exibir ajuda
show_help() {
    cat << EOF
${BLUE}FinanCerto Analytics - Test Runner${NC}

Uso: $0 [opção]

Opções:
  all              Executar todos os testes
  coverage         Executar com relatório de cobertura
  fast             Executar apenas testes rápidos
  file <name>      Executar arquivo específico (ex: test_report_service.py)
  test <pattern>   Executar teste com padrão (ex: "test_init")
  watch            Executar em modo watch (auto-reload)
  lint             Executar verificação de código (pylint)
  format           Formatar código (black)
  clean            Limpar arquivos de cache
  help             Mostrar esta mensagem

Exemplos:
  $0 all                          # Rodar tudo
  $0 coverage                     # Com cobertura
  $0 file test_report_service.py  # Arquivo específico
  $0 test test_init               # Testes matching "test_init"

EOF
}

# Modo watch
run_watch_mode() {
    print_header "Executando em MODO WATCH"
    if ! command -v ptw &> /dev/null; then
        print_warning "pytest-watch não está instalado"
        echo "Instale com: pip install pytest-watch"
        echo "Depois execute: ptw"
        exit 1
    fi
    ptw
}

# Lint
run_lint() {
    print_header "Verificando código com pylint"
    if ! command -v pylint &> /dev/null; then
        print_warning "pylint não está instalado"
        echo "Instale com: pip install pylint"
        exit 1
    fi
    pylint app/
}

# Format
run_format() {
    print_header "Formatando código com black"
    if ! command -v black &> /dev/null; then
        print_warning "black não está instalado"
        echo "Instale com: pip install black"
        exit 1
    fi
    black app/ tests/
    print_success "Código formatado"
}

# Clean
run_clean() {
    print_header "Limpando arquivos de cache"
    find . -type d -name __pycache__ -exec rm -rf {} + 2>/dev/null || true
    find . -type f -name "*.pyc" -delete
    rm -rf .pytest_cache htmlcov .coverage junit.xml
    print_success "Cache limpo"
}

# Main
main() {
    case "${1:-all}" in
        all)
            check_pytest
            run_all_tests
            ;;
        coverage)
            check_pytest
            run_with_coverage
            ;;
        fast)
            check_pytest
            run_fast_tests
            ;;
        file)
            check_pytest
            run_single_file "$2"
            ;;
        test)
            check_pytest
            run_specific_test "$2"
            ;;
        watch)
            check_pytest
            run_watch_mode
            ;;
        lint)
            run_lint
            ;;
        format)
            run_format
            ;;
        clean)
            run_clean
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Opção desconhecida: $1"
            show_help
            exit 1
            ;;
    esac
}

# Executar
main "$@"
