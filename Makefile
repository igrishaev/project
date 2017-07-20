
project := project
res_dir := $(CURDIR)/resources

db-user:
	createuser -s -P $(project)

db-database:
	createdb -O $(project) $(project)

db-create-migration:
	@read -p "Enter migration name: " migration \
	&& lein migratus create $$migration

db-migrate:
	lein migratus migrate

db-rollback:
	lein migratus rollback
