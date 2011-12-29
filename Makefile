.PHONY: package compile clean
.DEFAULT: package

JAVAC=javac
JAR=jar
JAVA=java

BUILD=build
SRC=src
PACKAGE=dist
PACKAGEJAR=$(PACKAGE)/docitten.jar
LIBS=$(wildcard lib/*.jar) lib/irc/dist/irc.jar

CP=$(SRC):$(LIBS: =:)

FILES=$(wildcard $(SRC)/uk/co/harcourtprogramming/docitten/*.java)
CLASS=$(patsubst $(SRC)/%.java,$(BUILD)/%.class,$(FILES))

package: $(PACKAGEJAR)
compile: $(CLASS)

$(BUILD)/%.class : $(SRC)/%.java $(LIBS)
	$(JAVAC) -classpath $(CP) -d $(BUILD) $<

$(PACKAGEJAR): $(BUILD) $(PACKAGE) $(CLASS) $(LIBS)
	$(JAR) cfm $(PACKAGEJAR) Manifest.mf -C $(BUILD) .
	cp $(LIBS) $(PACKAGE)
	cp lib/irc/dist/*.jar $(PACKAGE)

lib/irc/dist/netcat.jar:
	$(MAKE) --directory=lib/irc package

$(BUILD):
	-mkdir $(BUILD)

$(PACKAGE):
	-mkdir $(PACKAGE)

clean:
	-rm -f build/* -r
	-rm -f dist/* -r

