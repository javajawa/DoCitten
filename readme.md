DoCitten
========

License
-------

DoCitten is licensed under a BSD 3-Clause Lincense

Copyright (c) 2011-12, Harcourt Programming
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 - Neither the name of the Harcourt Programming nor the names of its
 contributors may be used to endorse or promote products derived from this
 software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Compiling
---------

First, make sure that the submodule is initialised:

    git submodule update --init lib/irc

The project can be make from the command line using
make

- ```make compile```    - Creates the class files in ```build/```
- ```make package```    - Creates/copies JAR archives to ```dist/```
- ```make test-build``` - Creates class files for the tests
- ```make test```       - Runs test classes that have been compiled
- ```make clean```      - Removes all class and JAR files

It can also be imported into an IDE of your choice.
Two projects will likely need to be created - one at the root of the working
tree (DoCitten), and one in ```lib/irc``` (InternetRelayCats).
In both cases, Netbeans Java Project with existing sources project wizard
will correctly create the projects with

- ```src/``` listed as a source folder
- ```test/``` listed as a test sources folder

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

- patches
- pull requests
- git hub issues
- direct email to me
- moaning on an irc channel I'm in
- inline comments
- IPoAC

in descending order of preference.
