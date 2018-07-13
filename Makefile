
project := project
resources := $(CURDIR)/resources

db-create-user:
	createuser -s -P $(project)

db-create-db:
	createdb -O $(project) $(project)

db-drop-db:
	dropdb $(project)

db-migrate:
	lein migratus migrate

db-create-migration:
	@read -p "Enter migration name: " migration \
	&& lein migratus create $$migration

repl:
	lein repl

py-server:
	python py/server.py

py-install:
	pip install -r py/requirements.txt

mail-stub:
	mailhog

cljsbuild-prod:
	lein cljsbuild once prod

cljsbuild-dev:
	lein cljsbuild once dev

figwheel:
	lein figwheel

trash:
	find . -name "*.DS_Store" -delete
	find . -name "*.retry" -delete
	find . -name "*.pyc" -delete
