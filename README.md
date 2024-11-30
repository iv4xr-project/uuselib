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
