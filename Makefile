
project := project
resources := $(CURDIR)/resources

all: build

.PHONY: clean
clean:
	lein clean

uberjar-build:
	rm -rf target
	lein uberjar

uberjar-run:
	java -jar ./target/$(project).jar

build: clean cljsbuild-prod uberjar-build

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

mail-stub:
	mailhog

cljsbuild-prod:
	rm -rf $(resources)/public/ui
	lein cljsbuild once prod

cljsbuild-dev:
	lein cljsbuild once dev

figwheel:
	lein figwheel

trash:
	find . -name "*.DS_Store" -delete
	find . -name "*.retry" -delete
	find . -name "*.pyc" -delete

deps-tree:
	lein deps :tree

ansible-common:
	ansible-playbook -i ansible/inventory ansible/playbooks/common.yaml
