DoCitten
========

License
-------

This work is licensed under the Creative Commons Attribution 3.0 Unported License.
To view a copy of this license, visit
	http://creativecommons.org/licenses/by/3.0/
or send a letter to
	Creative Commons,
	444 Castro Street,
	Suite 900,
	Mountain View,
	California,
	94041,
	USA.

Compiling
---------

First, make sure that the submodule is iniialised
	git submodule update --init lib/irc

The project can be make from the command line using
make
	make compile    - Creates the class files in build/
	make package    - Creates/copies JAR archives to dist/
	make test-build - Creates class files for the tests
	make test       - Runs test classes that have been compiled
	make clean      - Removes all class and JAR files

It cna also be imported into an ide of your choice.
Two projects will likely need to be created - one at the root of the working
tree (DoCitten), and one in lib/irc (InternetRelayCats).
In both cases, Netbeans Java Project with existing sources project wizard
will correctly create the projects with
	src/ listed as a source folder
	test/ listed as a test sources folder

Note that you will to tell the IDE that DoCitten needs InternetRelayCats as a
library, and the DoCitten's test packages rely both of InternetRelayCats main
code _and_ test packages

Running
-------

DoCitten currently takes input only in the form of command line arguments, and
whatever processing the Services perform.
The first argument is the server to connect to; any further arguments are taken
as channels to join.

Joining using different logins, ports, with SSL, etc. are in the works.

The program can be stopped by entering the line 'quit' via standard input.

Bug Reporting
-------------

Bug reports / suggestions for improvement are accept via:
 - patches,
 - pull requests,
 - git hub issues,
 - direct email to me,
 - inline comments
 - moaning on an irc channel I'm in
 - IPoAC
in decending order of preference.
