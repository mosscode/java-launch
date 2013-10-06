PROJECT-GOAL
===========

Create a generic means of specifying how to launch a java program.  

BACKGROUND/CONSIDERATIONS
===========

To launch a java program, you need:

  1. A java virtual machine
  1. Java bytecode
  1. Native code (optionally)
  1. Other classpath resources
   
A java machine can be launched with standardized parameters such as Xmx, and can take non-standard launch params as well.

A java program starts with a main() method, which takes a list of arguments.

Java has a concept of "system properties": a key+value map, the entries of which can be initialized at launch-time.
   
Java code is typically bundled in JARs.  A new component model is in the works, but details are not yet available.

When native code is present in a java app, it typically is present in different versions, where each version matches one or more hardware platforms.

USEFULNESS
===========

A good launch spec should be something from which the following can be created:

- server side app packages (daemons, etc)
 - JavaServiceWrapper
- standalone launches
     - shell scripts
     - windows installers
     - macos java app wrappers
     - RPMs & dpkgs
- JNLP files/setups
- Applets
- Constellation services
- Must be able to handle:
      - Jars
- future module specs
      - native libraries

BUILD-PROCESS/PACKAGING IMPLICATIONS
===========

The output of a build should be the application descriptor and individual modules.  Final packaging should be a separate step?  From whom?

DEPLOYMENT CHANNELS
===========

build->maven-repo->constellation

build->maven-repo->dpkg

build->

IMPLEMENTATION CONCEPTS
===========

To launch, you need:

1. a computer
1. a jvm
1. a launch descriptor
1. the app components (jars, native libs, etc)
  
THE IMPLICIT REPOSITORY
===========

The launch spec doesn't cover where/how to get the application components.  However, the implication is that these components are 
deposited /somewhere/ (else the spec would not be very useful).  This implicit somewhere can be called a 'repository'.  
A repository could be a local directory, a zip file, a remote http service, etc.  The components could be spread across one
or more such repositories.  As far as the launch spec is concerned, it doesn't matter: how to find and get the components
is a launch-time consideration, while the launch spec is more of a compile-time consideration.

BUNDLES
===========

Modular programs sometimes are partioned into separate bundles, where each bundle is loaded by special code-loading features of
 the program itself (e.g. custom classloaders).
In such cases, not all of the components used by the program should be put on the classpath.  At the same time, they do need
to be made available to the application so that the app can load them if/when needed.  There ought to be a mechanism for
making a launched-program aware of bundles.

DYNAMIC DOWNLOAD?
===========

Sometimes an app may want to download components dynamically.  Should this be handled by the launch-spec in any way?

SUPPORT APPS
===========

TODO:
===========
  Icon support?
  Description?

