

==========================================
               ATTENTION
==========================================
THIS PROGRAM ONLY WORKS WITH WINDOWS SYSTEMS, THAT MEANS THAT IT EXECUTES SOME COMMANDS THAT ARE EXCLUSIVE TO WINDOWS OS SO IT CAN'T BE RUN ANYWHERE ELSE.
IF YOU DON'T HAVE A WINDOWS OPERATING SYSTEM, YOU CAN USE PARALLELS ON MAC OS TO GET A WINDOWS VIRTUAL MACHINE AND IN LINUX YOU CAN USE QEMU OR WHATEVER.
IF YOU ARE NOT SURE INSTALL VIRTUALBOX AND FOLLOW AN INSTALLATION GUIDE.
==========================================
==========================================
               GAME
==========================================
The origins of the concept for the videogame comes from a youtube video (https://www.youtube.com/watch?v=k7mJugT2hfQ) that was a devlog of a guy making his game that helped you
with tasks. I took the video title literally and made the windows task manager a fun interactive video game.

==========================================
              Controls
==========================================
Type the name of a window that you want to close and press the button (it's normal that the window might become unresponsive and you'll have to wait a bit).

Once inside the game:
You move around with the arrow keys,
Shoot the big red icon with space.
==========================================
              Mechanics
==========================================
The enemies will head straight towards you, if you touch them you lose 1 health.
If you press space, your weapon will become bigger and stay in place for a few seconds, if enemies touch this weapon they die and you win points.
Picking up the health packs that are scrolling from right to left adds one health to your bar.

==========================================
          Objective and Story
==========================================
The idea of the game is that after trying to stop a faulty program that isn't working, corrupt programs in the form of monsters start to appear that are trying to destroy your pc.
You must eliminate 30 of these programs so that you can close the faulty program and your pc survives.

==========================================
          Known faults
==========================================
Your pc might suffer high cpu usage when launching the program, once the program finds the open window and you enter into the game it should go away (see preGame() function to see why).
I have encountered some errors that make the program crash but it seems to pop up in the gameEngine.java
The program might have difficulties finding the application that you are specifying.
Before you put the application that you want to use, the gameWindow has a white background, I just can't set a background image.

PD: I poured my hearth out doing this game, hope you enjoy the concept!