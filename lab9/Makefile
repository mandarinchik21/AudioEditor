.PHONY: build
build:
	@mvn -q compile

.PHONY: run
run:
	@mvn -q exec:java -Dexec.mainClass=app.soundlab.ui.MainWindow

.PHONY: server
server:
	@echo "Starting Audio Editor Server on port 8888..."
	@mvn -q exec:java -Dexec.mainClass=app.soundlab.server.AudioEditorServer

.PHONY: client
client:
	@echo "Starting Audio Editor Client..."
	@mvn -q exec:java -Dexec.mainClass=app.soundlab.ui.MainWindow

.PHONY: install
install:
	@mvn clean install