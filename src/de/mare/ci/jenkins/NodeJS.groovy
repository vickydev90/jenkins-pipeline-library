#!/usr/bin/groovy
package de.mare.ci.jenkins

def npm(runTarget, opts = null) {
    def prefix = ""
    if (opts != null) {
        prefix = opts + " "
    }
   sh """#!/bin/bash -e
	NVM_DIR=
        ${prefix}/npm ${runTarget}"""
}

def npmRun(runTarget, opts = null) {
    def prefix = ""
    if (opts != null) {
        prefix = opts + " "
    }
    sh """#!/bin/bash -e
        NVM_DIR=
        source ~/.nvm/nvm.sh
        npm install
        ${prefix}npm run ${runTarget}"""
}

def npmNode(command, opts = null) {
    def prefix = ""
    if (opts != null) {
        prefix = opts + " "
    }
    sh """#!/bin/bash -e
        NVM_DIR=
        source ~/.nvm/nvm.sh
        nvm use
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
