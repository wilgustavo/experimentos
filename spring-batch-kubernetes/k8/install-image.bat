
@echo Crear imagen de Docker
docker build . -t books:0.1

@echo Cargar imagen de Docker en Kind
kind load docker-image books:0.1 --name kind-cluster
