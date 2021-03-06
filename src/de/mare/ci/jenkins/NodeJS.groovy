#!/usr/bin/groovy
package de.mare.ci.jenkins


def npm(runTarget, opts = null) {
    def prefix = ""
    if (opts != null) {
        prefix = opts + " "
    }
   sh """#!/bin/bash -e
        ${prefix}npm ${runTarget}"""
}

def npmRun(runTarget, opts = null) {
    def prefix = ""
    if (opts != null) {
        prefix = opts + " "
    }
    sh """#!/bin/bash -e
        ${prefix}npm run ${runTarget}"""
}

def npmNode(command, opts = null) {
    def prefix = ""
    if (opts != null) {
        prefix = opts + " "
    }
    sh """#!/bin/bash -e
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
		stash 	name: "artifact-${context.application}-${targetBranch}-${currentVersion}" , includes: "**"
		archiveArtifacts 	artifacts: artifact, onlyIfSuccessful: true
		echo "PUBLISH: ${this.name()} artifact  to ${nexusURL} "
		nexusPublisher {
					targetURL = nexusURL
					tarball = this.name()
				}
		} catch (error) {
 			echo "Failed to publish artifact to Nexus"
 		}
}
return this;
