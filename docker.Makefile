
# Building docker image
VERSION = "0.0.1"
PRODUCT=zooma-web
IM=ihcc/$(PRODUCT)

docker-build-no-cache:
	@docker build --no-cache -t $(IM):$(VERSION) . \
	&& docker tag $(IM):$(VERSION) $(IM):latest
	
docker-build:
	@docker build -t $(IM):$(VERSION) . \
	&& docker tag $(IM):$(VERSION) $(IM):latest

docker-run: docker-build
	docker run -p 8009:8080 -v /Users/matentzn/knocean/ihcc-config/config/zooma-config:/root/.zooma/config $(IM)

docker-run-sh: docker-build
	docker run -it $(IM) /bin/bash

docker-clean:
	docker kill $(IM) || echo not running ;
	docker rm $(IM) || echo not made 

docker-publish-no-build:
	@docker push $(IM):$(VERSION) \
	&& docker push $(IM):latest
	
docker-publish: docker-build
	@docker push $(IM):$(VERSION) \
	&& docker push $(IM):latest
	
#include dumps.Makefile