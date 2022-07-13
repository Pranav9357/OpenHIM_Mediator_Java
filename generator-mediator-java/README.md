[![OpenHIM Core](https://img.shields.io/badge/openhim--core-1.4%2B-brightgreen.svg)](http://openhim.readthedocs.org/en/latest/user-guide/versioning.html)

# generator-mediator-java

A [Yeoman](http://yeoman.io) generator for scaffolding a Java-based [OpenHIM](http://openhim.org/) mediator based on the [Java Engine](https://github.com/jembi/openhim-mediator-engine-java).


## Getting Started

It's quick to get up and running. First install yeoman and this generator, and then run the generator:
```bash
npm install -g yo
npm install -g generator-mediator-java
mkdir my-mediator
cd my-mediator
yo mediator-java
```

The generator will prompt you with several questions for your mediator and will then setup the scaffold. When done, simply run
```bash
mvn install
```
to build the mediator.

The mediator is packaged as a stand-alone jar and can be executed as follows:
```bash
java -jar mediator-x.y.x-jar-with-dependencies.jar --conf my-mediator.properties
```

The .jar can be found in `target/` and a default set of properties in `src/main/resources/mediator.properties`

## Certificate Issues

If you're using the OpenHIM with a self-signed certificate, [this tutorial](http://openhim.readthedocs.io/en/latest/tutorial/3-creating-a-passthrough-mediator.html#suncertpathbuilderexception-unable-to-find-valid-certification-path-to-requested-target) should help you setup your local JRE to accomodate the HIM's certificate.

## Compatibility

Out of the box the scaffolded mediator will be compatible with version 1.4+ of the OpenHIM Core. Note however that it will be fully compatible with 2.0+ as well. If for some reason you require the mediator to be 1.2+ compatible, you can easily allow this by disabling the heartbeats. To do so simply set `mediator.heartbeats` to `false` in `mediator.properties`.
