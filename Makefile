setup-mongo:
	helm install mongo -f k8s/helm/mongo/values.yaml k8s/helm/mongo --create-namespace --namespace maintenance

setup-maintenance:
	helm install maintenance -f k8s/helm/maintenance/values.yaml k8s/helm/maintenance --create-namespace --namespace maintenance

remove-mongo:
	helm delete mongo --namespace maintenance

package:
	mvn package

run-package:
	java -jar target/spring-maintenance-0.0.1-SNAPSHOT.jar

local-run:
	make package
	make run-package

docker-build:
	docker build -t rburch4/maintenanceapp:1.0.0 -f docker/springboot/Dockerfile .
	docker push rburch4/maintenanceapp:1.0.0

build:
	helm delete maintenance -n maintenance
	make package
	make docker-build
	make setup-maintenance