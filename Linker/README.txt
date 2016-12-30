----------------------
How to build:
----------------------

> OS
> cd linker
> cd srcs
> javac oslab1.java


// File hiearchy//
Linker >
  Test Text Files (1.txt, 2.txt, etc ..)
  README.txt
  src
    > oslab1.java

------------------------
How to run:
-----------------------

> java oslab1

"Please input filename:"

> [enter file name to be ran]

Example:
> java oslab1
Please input filename
> 7.txt















————————— * notes to myself please ignore * ———————————



Error detection:

1. DONE If a symbol is multiply defined, print an error message and use the value given in the first definition.
2. DONE If a symbol is used but not defined, print an error message and use the value zero.
3. DONE - If a symbol is defined but not used, print a warning message and continue.
4. If an address appearing in a definition exceeds the size of the module, print an error message and treat
the address given as 0 (relative).
5. DONE If a symbol appears in a use list but it not actually used in the module (i.e., not referred to in an external
address), print a warning message and continue.
6. DONE - If an external address is too large to reference an entry in the use list, print an error message and treat
the address as immediate.
7. DONE - If an absolute address exceeds the size of the machine, print an error message and use the value zero.
8. DONE -  If a relative address exceeds the size of the module, print an error message and use the value zero (absolute).

TESTS:
1 - PASS
2 - PASS
3 - PASS
4 - PASS
5 - PASS
6 - PASS
7 - PASS
8 - PASS
