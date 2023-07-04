
@echo Create cluster Kind
kind create cluster --name kind-cluster --config kind/cluster.yaml

@echo "---> Agregar repositorio de operador MariaDB"
helm repo add mariadb-operator https://mariadb-operator.github.io/mariadb-operator

helm repo update

@echo "---> Instalar operador MariaDB"
helm install mariadb-operator mariadb-operator/mariadb-operator
