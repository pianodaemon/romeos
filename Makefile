ECHO = /bin/echo

BASE		:= $(shell /bin/pwd)
OPERATIONAL	:= $(BASE)/DOS

engage: spin_up build_imgs

disengage:
	docker-compose stop

build_imgs:
	docker-compose build

spin_up:
	docker-compose up -d
