#!/usr/bin/groovy
package de.mare.ci.jenkins

def npm(runTarget, opts = null, config) {
    def prefix = ""
    if (opts != null) {
        prefix = opts + " "
    }
   sh """#!/bin/bash -e
	NVM_DIR=
	export PATH=/usr/local/bin:$PATH
        ${prefix}npm ${runTarget}"""
}

def npmRun(runTarget, opts = null, config) {
    def prefix = ""
    if (opts != null) {
        prefix = opts + " "
    }
    sh """#!/bin/bash -e
        NVM_DIR=
	export PATH=/usr/local/bin:$PATH
        ${prefix}npm run ${runTarget}""";
 	{
    		stash   name: "artifact-${config.application}-${config.targetEnv}-${currentVersion}" , includes: "**"
   		archiveArtifacts        artifacts: artifact, onlyIfSuccessful: true 
	}
}

def npmNode(command, opts = null, config) {
    def prefix = ""
    if (opts != null) {
        prefix = opts + " "
    }
    sh """#!/bin/bash -e
        NVM_DIR=
        ${prefix}node ${command}"""
}

def readJson(text) {
    def response = new groovy.json.JsonSlurperClassic().parseText(text)
    jsonSlurper = null
    echo "response:$response"
    return response
}

def getVersionFromPackageJSON() {
    dir(".") {
        def packageJson = readJSON file: 'package.json'
        return packageJson.version
    }
}

def currentVersion = getVersionFromPackageJSON()




def publishSnapshot(directory, buildNumber, name) {
    dir(directory) {
        // get current package version
        def currentVersion = getVersionFromPackageJSON()
        // add build number for maven-like snapshot
        def prefix = name.replaceAll('/','-').replaceAll('_','-').replaceAll('@','')
        def newVersion = "${currentVersion}-${prefix}-${buildNumber}"
        // publish snapshot to NPM
        sh """#!/bin/bash -e
            NVM_DIR=
            source ~/.nvm/nvm.sh
            npm version ${newVersion} --no-git-tag-version && npm publish --tag next"""
    }
}

return this;
