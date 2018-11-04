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
        ${prefix}npm run ${runTarget}"""
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

def publishNexus(String targetBranch, config){
    def currentVersion = getVersionFromPackageJSON()
    String nexusURL = config.nexus.url ?: 'http://invalid.url/'
    String customCredentials = config.nexus.credentials ?: null
	try{
		stash 	name: "artifact-${context.application}-${targetBranch}" , includes: "**"
		archiveArtifacts 	artifacts: artifact, onlyIfSuccessful: true
		echo "PUBLISH: ${this.name()} artifact  to ${nexusURL} "
		nexusPublisher {
					targetURL = nexusURL
					tarball = artifact
				}
		} catch (error) {
 			echo "Failed to publish artifact to Nexus"
 		}
}
return this;
