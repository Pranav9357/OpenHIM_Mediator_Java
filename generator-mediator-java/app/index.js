'use strict';

var util = require('util');
var path = require('path');
var yeoman = require('yeoman-generator');
var chalk = require('chalk');
var yosay = require('yosay');
var uuid = require('node-uuid');

var mediatorJavaGenerator = yeoman.generators.Base.extend({
  
  prompting: function () {
    var done = this.async();

    // Have Yeoman greet the user.
    this.log(yosay(
      'Welcome to the marvelous ' + chalk.red('MediatorJava') + ' generator!'
    ));

    var prompts = [{ 
        type: 'input', 
        name: 'mediatorName', 
        message: 'What is your Mediator\'s name?', 
        default: 'Yeoman Generated Mediator',
        validate: function(mediatorName){ 
          if(mediatorName !== ''){ return true; }else{ return 'Please supply a Mediator Name'; } 
        } 
      }, { 
        type: 'input', 
        name: 'mediatorDesc', 
        message: 'What does your Mediator do?',
        default: 'Brief Description'
      }, { 
        type: 'input', 
        name: 'configGroupID', 
        message: 'What is your group ID?', 
        default: 'com.mycompany',
        validate: function(groupID){ 
          if(groupID !== ''){ return true; }else{ return 'Please supply a group ID'; } 
        } 
      }, { 
        type: 'input', 
        name: 'configArtifactID', 
        message: 'What artifact ID do you want to use?', 
        default: 'java-mediator',
        validate: function(artifactID){ 
          if(artifactID !== ''){ return true; }else{ return 'Please supply an artifact ID'; } 
        } 
      }, { 
        type: 'input', 
        name: 'configNamespace', 
        message: 'What package do you want to use for the source code?', 
        default: 'com.mycompany.mediator',
        validate: function(namespace){ 
          if(namespace !== ''){ return true; }else{ return 'Please supply a package'; } 
        } 
      }, { 
        type: 'input', 
        name: 'configPort', 
        message: 'Under what port number should the mediator run?', 
        default: 3000 
      }, { 
        type: 'input', 
        name: 'mediatorRoutePath', 
        message: 'What is your primary route path?', 
        default: '/mediator',
        validate: function(mediatorRoutePath){ 
          if(mediatorRoutePath !== ''){ return true; }else{ return 'Please supply a primary route path'; } 
        } 
      }];

    this.prompt(prompts, function (props) {
      this.configGroupID = props.configGroupID;
      this.configArtifactID = props.configArtifactID;
      this.configNamespace = props.configNamespace;

      this.configPort = props.configPort;

      this.mediatorName = props.mediatorName;
      this.mediatorDesc = props.mediatorDesc;
      this.mediatorRoutePath = props.mediatorRoutePath;

      done();
    }.bind(this));
  },
    
  scaffoldFolders: function(){

    var folders = this.configNamespace.split(".");
    var folderStructure = '';
    for ( var i=0; i<folders.length; i++ ){
      folderStructure += folders[i]+'/';
    }

    this.mkdir("src/main/java");
    this.mkdir("src/main/resources");
    this.mkdir("src/test/java");

    // dynamic folder structure
    this.mainFolderStructure = "src/main/java/"+folderStructure;
    this.mkdir( this.mainFolderStructure );
    this.testFolderStructure = "src/test/java/"+folderStructure;
    this.mkdir( this.testFolderStructure );

  },

  copyMainFiles: function(){

    var context = { 
      configGroupID: this.configGroupID,
      configArtifactID: this.configArtifactID,
      configNamespace: this.configNamespace,

      configPort: this.configPort,

      mediatorUUID: "urn:uuid:"+uuid.v1(),
      appName: this.mediatorName.replace(/ /g,"-"),
      mediatorName: this.mediatorName,
      mediatorDesc: this.mediatorDesc,
      mediatorRoutePath: this.mediatorRoutePath,

      defaultPermission: this.mediatorName.replace(/ /g,"").toLowerCase()
    };

    // copy the templates and override placeholders
    this.template("_pom.xml", "pom.xml", context);
    this.template("_mediator.properties", "src/main/resources/mediator.properties", context);
    this.template("_mediator-registration-info.json", "src/main/resources/mediator-registration-info.json", context);
    this.template("_DefaultOrchestrator.java", this.mainFolderStructure+"DefaultOrchestrator.java", context);
    this.template("_MediatorMain.java", this.mainFolderStructure+"MediatorMain.java", context);
    this.template("_DefaultOrchestratorTest.java", this.testFolderStructure+"DefaultOrchestratorTest.java", context);

  },

  install: function () {
    this.installDependencies({
      bower: false,
      npm: true,
      skipInstall: true,
      skipMessage: true,
      callback: function () {
        // Have Yeoman greet the user.
        console.log(yosay(
          'Scaffolding Complete!\r' +
          'To build your project run ' + chalk.green('mvn install\r') +
          'Also remember to check your config in src/main/resources\r' +
          '\r Goodbye!'
        ));
      }
    });
  }
});

module.exports = mediatorJavaGenerator;
