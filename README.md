# uuselib

Warning: work in progress!

This is a library containing functions, tactics, goals, and other stuffs for driving an aplib agent for testing the Space Engineers game.

The library is written in Java, and is organized as a Maven project.

Main components you need to use this library:

* Obviously the game Space Engineers (SE) itself. The library currently only works with a specific version of SE, namely 1.202 (Beta). More instructions: TO-DO.

* The test-agent framework [aplib (Java)](https://github.com/iv4xr-project/aplib). This will be automatically included when you build `uuselib`; done through Maven dependency.

* A special _iv4xr-SE-plugin_ that would allow an external program to control SE. A binary is included in `./libs`. This works in combination with the above mentioned version of SE.

* _iv4xr-SE-jvm-client_. This is a library that allows Java programs to control SE via the above mentioned SE-plugin. The connection between the jvm-client and the SE-plugin goes via socket. For convenience, we put a jar containing this library in `./libs`.

So, if you want to build automated testing program that targets SE, your tech-stack would look like this:

```
   *------------------------------------*
   *  your automated testing algorithm  *
   *--------------------*---------------*
   *      uuselib       *               *
   *--------------------*               *
   *               aplib                *
   * -----------------------------------*
   *       iv4xr-SE-jvm-client          *
   *------------------------------------*
   *          iv4xr-SE-plugin           *
   *------------------------------------*
   *        Space Engineers game        *
   *------------------------------------*
```

### Related projects

* [aplib](https://github.com/iv4xr-project/aplib): is an agent-programming framework written in Java. It mains use-case is for game testing.
* [iv4xr-SE](https://github.com/iv4xr-project/iv4xr-se-plugin) provided by GoodAI. It provides the iv4xr-SE-plugin and the iv4xr-SE-jvm-client mentioned before.

### Notes

`uuselib` is originally a package called `uuspaceagent` added to the iv4xr-SE-jvm-client. More specifically, it was developed in the branch `uubranch3D-v2*`.

At the moment we are working on extracting this `uuspaceagent` and placing it under a separate project, namely this `uuselib`. Among other things, this would enable us to unpin `uuspaceagent` dependency on the old version of the SE-jvm-client.

### Installing iv4xr-SE-plugin

TO DO

### Working on this project from Eclipse IDE

* From Eclipse, import the this project. Import it as a Maven project.

* Open the project in Eclipse. Then open the project properties:
   * Check that the Java Compiler is set with at least version 15.
   * In the tab for _Java Build Path_, _Libraries_, add the jar `./libs/space-engineers-api-0.9.0.jar` to the classpath there.

* That was it. See, if you now can run some of the tests that come with the project.

### Where are the worlds?

There are some pre-made SE-worlds (game levels) in `assets/se-worlds` you can use for experiments. For example, some tests in the project use them.

Just be careful; by default SE will auto-save a world if you let it open for sometime, which may then mess up the saved worlds.
