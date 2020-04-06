pipeline {
    agent any

    stages {
        stage("Parameter Check") {
            steps {
                println "TAG = " + $TAG
                println "BUILD_TYPE = " + $BUILD_TYPE
                println "SCALA_VERSION = " + $SCALA_VERSION
                println "ASSEMBLY = " + $ASSEMBLY
            }
        }
    }
}