.PHONY: package compile clean test-build test
.DEFAULT_GOAL: package

SPACE:=
SPACE+=

JAVAC=javac
JAR=jar
JUNIT=/usr/share/java/junit4.jar

SRC=src
TEST=test

BUILD=build
TBUILD=tb
PACKAGE=dist

PACKAGEJAR=$(PACKAGE)/docitten.jar
LIBS=lib/irc/dist/irc.jar lib/irc/dist/mewler.jar $(wildcard lib/*.jar)

CP=$(SRC):$(subst $(SPACE),:,$(LIBS))
TCP=$(TEST):$(BUILD):$(JUNIT):$(subst $(SPACE),:,$(LIBS)):lib/irc/test:lib/irc/lib/mewler/test

FILES=$(wildcard $(SRC)/uk/co/harcourtprogramming/docitten/*.java)
CLASS=$(patsubst $(SRC)/%.java,$(BUILD)/%.class,$(FILES))

TFILES=$(wildcard $(TEST)/uk/co/harcourtprogramming/docitten/*.java)
TCLASS=$(patsubst $(TEST)/%.java,$(TBUILD)/%.class,$(TFILES))

TESTABLE=$(wildcard $(TEST)/uk/co/harcourtprogramming/docitten/*Test.java)
TESTS=$(patsubst $(TEST).%.java,%,$(subst /,.,$(TESTABLE)))

package: $(PACKAGEJAR)
compile: $(CLASS)
test-build: $(TCLASS)
test: test-build
	java -cp $(TBUILD):$(TCP) org.junit.runner.JUnitCore $(TESTS)

java-check:
	./java-major-version 7 $(JAVAC)
	touch $@

$(BUILD)/%.class : $(SRC)/%.java $(LIBS) $(BUILD) java-check
	$(JAVAC) -classpath $(CP) -d $(BUILD) $<

$(TBUILD)/%.class : $(TEST)/%.java $(LIBS) compile $(TBUILD) java-check
	$(JAVAC) -classpath $(TCP) -d $(TBUILD) $<

$(PACKAGEJAR): $(BUILD) $(PACKAGE) $(CLASS) $(LIBS) Manifest.mf
	$(JAR) cfm $(PACKAGEJAR) Manifest.mf -C $(BUILD) .
	cp $(LIBS) $(PACKAGE)
	cp lib/irc/dist/*.jar $(PACKAGE)

lib/irc/dist/irc.jar lib/irc/dist/mewler.jar::
	$(MAKE) --directory=lib/irc package

$(BUILD):
	-mkdir $@
$(TBUILD):
	-mkdir $@
$(PACKAGE):
	-mkdir $@

clean:
	$(MAKE) --directory=lib/irc clean
	-rm -f $(BUILD) -r
	-rm -f $(TBUILD) -r
	-rm -f $(PACKAGE) -r

