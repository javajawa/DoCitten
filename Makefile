.PHONY: package compile clean test-build test
.DEFAULT: package

JAVAC=javac
JAR=jar
JUNIT=/usr/share/java/junit4.jar

SRC=src
TEST=test

BUILD=build
TBUILD=tb
PACKAGE=dist

PACKAGEJAR=$(PACKAGE)/docitten.jar
LIBS=$(wildcard lib/*.jar) lib/irc/dist/irc.jar

CP=$(SRC):$(LIBS: =:)
TCP=$(TEST):$(BUILD):$(JUNIT):$(LIBS: =:):lib/irc/test

FILES=$(wildcard $(SRC)/uk/co/harcourtprogramming/docitten/*.java)
CLASS=$(patsubst $(SRC)/%.java,$(BUILD)/%.class,$(FILES))

TFILES=$(wildcard $(TEST)/uk/co/harcourtprogramming/docitten/*.java)
TCLASS=$(patsubst $(TEST)/%.java,$(TBUILD)/%.class,$(TFILES))

TESTABLE=$(wildcard $(TEST)/uk/co/harcourtprogramming/docitten/*Test.java)
TESTS=$(patsubst $(TEST).%.java,%,$(subst /,.,$(TESTABLE)))

package: $(PACKAGEJAR)
compile: $(CLASS)
test-build: $(TCLASS)
test:
	java -cp $(TBUILD):$(TCP) org.junit.runner.JUnitCore $(TESTS)

$(BUILD)/%.class : $(SRC)/%.java $(LIBS) $(BUILD)
	$(JAVAC) -classpath $(CP) -d $(BUILD) $<

$(TBUILD)/%.class : $(TEST)/%.java $(LIBS) compile $(TBUILD)
	$(JAVAC) -classpath $(TCP) -d $(TBUILD) $<

$(PACKAGEJAR): $(BUILD) $(PACKAGE) $(CLASS) $(LIBS) Manifest.mf
	$(JAR) cfm $(PACKAGEJAR) Manifest.mf -C $(BUILD) .
	cp $(LIBS) $(PACKAGE)
	cp lib/irc/dist/*.jar $(PACKAGE)

lib/irc/dist/irc.jar::
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

