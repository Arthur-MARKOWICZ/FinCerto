FROM python:3.12-slim

WORKDIR /app

# Evita cache e deixa logs mais limpos
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

# Copia o pacote Python "app" para dentro do container
COPY app /app/app

# Sobe o servidor FastAPI apontando para o módulo de pacote "app.main"
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]

