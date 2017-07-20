
res_dir := $(CURDIR)/resources

.phony: config
config:
	cp $(res_dir)/config.edn.example $(res_dir)/config.edn
