pipeline {
    agent any

    stages {
        stage("Parameter Check") {
            steps {
                println "TAG = " + "${params.TAG}"
                println "BUILD_TYPE = " + "${params.BUILD_TYPE}"
                println "SCALA_VERSION = " + "${params.SCALA_VERSION}"
                println "ASSEMBLY = " + "${params.ASSEMBLY}"

                try{

                    if(${params.TAG} == "") {
                        currentBuild.result = "FAILURE"
                        throw new Exception("There is no TAG")
                    }
                }catch (e) {
                    currentBuild.result = "FAILURE"
                    println(e)
                }

            }
        }
    }
}