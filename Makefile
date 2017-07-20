
project := project
res_dir := $(CURDIR)/resources

.phony: config
config:
	cp $(res_dir)/config.edn.example $(res_dir)/config.edn

db-user:
	createuser -s -P $(project)

db-database:
	createdb -O $(project) $(project)
