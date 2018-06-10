jCHIP-8
=======

A WIP [CHIP-8](https://en.wikipedia.org/wiki/CHIP-8) interpreter in Java with a Swing GUI.

<img src="img/SCREEN0.png?raw=true" width=60%>
<img src="img/SCREEN1.png?raw=true" width=60%>

Instructions:
-------------
The hexadecimal input keypad is binded to the following keys on the QWERTY keyboard:

<table><tbody>
<tr><td><tt>1</tt></td><td><tt>2</tt></td><td><tt>3</tt></td><td><tt>4</tt></td></tr>
<tr><td><tt>Q</tt></td><td><tt>W</tt></td><td><tt>E</tt></td><td><tt>R</tt></td></tr>
<tr><td><tt>A</tt></td><td><tt>S</tt></td><td><tt>D</tt></td><td><tt>F</tt></td></tr>
<tr><td><tt>Z</tt></td><td><tt>X</tt></td><td><tt>C</tt></td><td><tt>V</tt></td></tr>
</tbody></table>

Mac OS X 10.12+ Users Only:
---------------------------
In Sierra and High Sierra, the [feature](https://support.apple.com/en-us/HT201586) that allows users to type accented characters by holding down a keystroke also causes keyboard input to not register in [many](http://mail.openjdk.java.net/pipermail/awt-dev/2017-December/013435.html) [Swing](https://community.oracle.com/thread/4115318) [Applications](https://stackoverflow.com/questions/41393525/java-swing-keybindings-stop-working-only-on-mac). A temporary fix for the issue is to execute the following command in your terminal:

`defaults write -g ApplePressAndHoldEnabled -bool false`

To re-enable typing accented characters like normal, simply execute the same command with true as the final argument:

`defaults write -g ApplePressAndHoldEnabled -bool true`


Acknowledgements:
-----------------
I would like to recognize the following resources for their great utility:

[Cowgod's Technical Reference](http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#Dxyn)

[Lawrence Muller's How-to Guide](http://www.multigesture.net/articles/how-to-write-an-emulator-chip-8-interpreter/)

[Matthew Mikolay's Mastering Guide](http://mattmik.com/files/chip8/mastering/chip8.html)