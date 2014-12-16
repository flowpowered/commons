# Flow Commons [![License](http://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat)][License] [![Flattr this](http://img.shields.io/badge/flattr-donate-lightgrey.svg?style=flat)][Donate] [![Build Status](http://img.shields.io/travis/flow/commons/develop.svg?style=flat)](https://travis-ci.org/flow/commons) [![Coverage Status](http://img.shields.io/coveralls/flow/commons/develop.svg?style=flat)](https://coveralls.io/r/flow/commons)

A library for Java that provides re-usable components commonly used by Flow libraries.

## Getting Started
* [Examples and code snippets](https://github.com/flow/examples/tree/master/commons)
* [Official documentation](#documentation)
* [IRC support chat](http://kiwiirc.com/client/irc.esper.net/flow)
* [Issues tracker](https://github.com/flow/commons/issues)

## Source Code
The latest and greatest source can be found here on [GitHub](https://github.com/flow/commons). If you are using Git, use this command to clone the project:

    git clone git://github.com/flow/commons.git

Or download the latest [development archive](https://github.com/flow/commons/archive/develop.zip) or the latest [stable archive](https://github.com/flow/commons/archive/master.zip).

## Dependencies
We love open-source libraries! This project uses are few of them to make things easier. If you aren't using Maven or Gradle, you'll need these!
* [com.flowpowered:flow-math](https://oss.sonatype.org/#nexus-search;gav~com.flowpowered~flow-math~~~)
* [com.github.wolf480pl:jline-log4j2-appender](https://oss.sonatype.org/#nexus-search;gav~com.github.wolf480pl~jline-log4j2-appender~~~)
* [jline:jline](https://oss.sonatype.org/#nexus-search;gav~jline~jline~~~)
* [net.sf.trove4j:trove4j](https://oss.sonatype.org/#nexus-search;gav~net.sf.trove4j~trove4j~~~)
* [org.apache.commons:commons-lang3](https://oss.sonatype.org/#nexus-search;gav~org.apache.commons~commons-lang3~~~)
* [org.slf4j:slf4j-api](https://oss.sonatype.org/#nexus-search;gav~org.slf4j~slf4j-api~~~)
* [uk.org.lidalia:lidalia-slf4j-ext](https://oss.sonatype.org/#nexus-search;gav~uk.org.lidalia~lidalia-slf4j-ext~~~)

## Test Dependencies
The following dependencies are only needed if you compiling the tests included with this project. Gotta test 'em all!
* [junit:junit](https://oss.sonatype.org/#nexus-search;gav~junit~junit~~~)
* [org.hamcrest:hamcrest-library](https://oss.sonatype.org/#nexus-search;gav~org.hamcrest~hamcrest-library~~~)
* [org.powermock:powermock-api-mockito](https://oss.sonatype.org/#nexus-search;gav~org.powermock~powermock-api-mockito~~~)
* [org.powermock:powermock-module-junit4](https://oss.sonatype.org/#nexus-search;gav~org.powermock~powermock-module-junit4~~~)

## Building from Source
This project can be built with the _latest_ [Java Development Kit](http://oracle.com/technetwork/java/javase/downloads) and [Maven](https://maven.apache.org/) or [Gradle](https://www.gradle.org/). Maven and Gradle are used to simplify dependency management, but using either of them is optional.

For Maven, the command `mvn clean package` will build the project and will put the compiled JAR in `target`, and `mvn clean install` will copy it to your local Maven repository.

For Gradle, the command `gradlew` will build the project and will put the compiled JAR in `~/build/distributions`, and `gradlew install` will copy it to your local Maven repository.

## Contributing
Are you a talented programmer looking to contribute some code? We'd love the help!

* Open a pull request with your changes, following our [guidelines and coding standards](CONTRIBUTING.md).
* Please follow the above guidelines for your pull request(s) accepted.
* For help setting up the project, keep reading!

Love the project? Feel free to [donate] to help continue development! Flow projects are open-source and powered by community members, like yourself. Without you, we wouldn't be here today!

Don't forget to watch and star our repo to keep up-to-date with the latest Flow development!

## Usage
If you're using [Maven](https://maven.apache.org/download.html) to manage project dependencies, simply include the following in your `pom.xml` file:

    <dependency>
        <groupId>com.flowpowered</groupId>
        <artifactId>flow-commons</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </dependency>

If you're using [Gradle](https://www.gradle.org/) to manage project dependencies, simply include the following in your `build.gradle` file:

    repositories {
        mavenCentral()
    }
    dependencies {
        compile 'com.flowpowered:flow-commons:0.1.0-SNAPSHOT'
    }

If you plan on using snapshots and do not already have the snapshot repo in your repository list, you will need to add this as well:

    https://oss.sonatype.org/content/groups/public/

If you'd prefer to manually import the latest .jar file, you can get it [here](https://github.com/flow/commons/releases).

## Documentation
Want to get friendly with the project and put it to good use? Check out the latest [Javadocs](https://flowpowered.com/commons).

To generate Javadocs with Maven, use the `mvn javadoc:javadoc` command. To view the Javadocs simply go to `target/site/apidocs/` and open `index.html` in a web browser.

To generate Javadocs with Gradle, use the `gradlew javadoc` command. To view the Javadocs simply go to `build/docs/javadoc/` and open `index.html` in a web browser.

## Version Control
We've adopted the [git flow branching model](http://nvie.com/posts/a-successful-git-branching-model/) in our projects. The creators of git flow released a [short intro video](http://vimeo.com/16018419) to explain the model.

The `master` branch is production-ready and contains the latest tagged releases. Before a release is made, it is stagged in `release/x` branches before being pushed and tagged in the `master` branch. Small patches from `hotfix/x` branches are also pushed to `master`, and will always have a release version. The `develop` branch is pre-production, and is where we push `feature/x` branches for testing.

## Legal Stuff
Flow Commons is licensed under the [MIT License][License]. Basically, you can do whatever you want as long as you include the original copyright. Please see the `LICENSE.txt` file for details.

## Credits
* [Spout](https://spout.org/) and contributors - *where we all began, and for much of the re-licensed code.*
* All the people behind [Java](http://www.oracle.com/technetwork/java/index.html), [Maven](https://maven.apache.org/), and [Gradle](https://www.gradle.org/).

[Donate]: https://flattr.com/submit/auto?user_id=spout&url=https://github.com/flow/commons&title=Flow+Commons&language=Java&tags=github&category=software
[License]: https://tldrlegal.com/l/mit
