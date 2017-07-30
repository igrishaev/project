
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
