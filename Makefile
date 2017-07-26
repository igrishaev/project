
project := project
datomic_dir := $(CURDIR)/datomic

db-transactor:
	$(datomic_dir)/bin/transactor $(CURDIR)/conf/datomic-dev.properties

db-console:
	$(datomic_dir)/bin/console -p 8081 datomic "datomic:dev://localhost:4334"
