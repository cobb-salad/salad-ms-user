pipeline {
    agent any
    parameters {
        string(name : 'TAG', defaultValue : '', description : '')
        string(name : 'BUILD_TYPE', defaultValue : '', description : '')
        string(name : 'SCALA_VERSION', defaultValue : '', description : '')
        string(name : 'ASSEMBLY', defaultValue : '', description : '')
    }

    stages {
        stage("Parameter Check") {
            steps {
                println "TAG = " + ${params.TAG}
                println "BUILD_TYPE = " + ${params.BUILD_TYPE}
                println "SCALA_VERSION = " + ${params.SCALA_VERSION}
                println "ASSEMBLY = " + ${params.ASSEMBLY}
            }
        }
    }
}