AMI_VERSION = 0


node {
    stage('Parameter Check'){

        TAG = "${params.TAG}"
        BUILD_TYPE = "${params.BUILD_TYPE}"
        SCALA_VERSION = "${params.SCALA_VERSION}"
        ASSEMBLY = "${params.ASSEMBLY}"
        SERVICE = "${params.SERVICE}"
        AMI_VERSION = "${params.AMI_VERSION}"


        println "${TAG}"
        println "${BUILD_TYPE}"
        println "${SCALA_VERSION}"
        println "${ASSEMBLY}"
        println "${SERVICE}"

//         some_flag = params.TAG != "" ? true : false
//         println "${some_flag}"

        try{
            if (params.SERVICE == "") {
                throw new Exception("You must select Service")
            }
            if (params.TAG == "") {
                throw new Exception("Enter the artifact version for the build in TAG")
            }
        }catch(e){
            currentBuild.result = "FAILURE"
//             throw(e)
            println(e)
        }

    }

    stage("Git CheckOut", {
            println "Git CheckOut Started"
            checkout(
                    [
                            $class                           : 'GitSCM',
                            branches                         : [[name: 'master']],
                            doGenerateSubmoduleConfigurations: false,
                            extensions                       : [],
                            submoduleCfg                     : [],
                            userRemoteConfigs                : [[credentialsId: 'github_mjkong_ssh', url: 'https://github.com/cobb-salad/salad-ms-${SERVICE}.git']]
                    ]
            )
            println "Git CheckOut End"
    })

    stage("Build Artifact") {

        if (params.BUILD_TYPE == "SBT"){

            BUILD_DIR="target/scala-${SCALA_VERSION}"
            SBT_LAUNCHER="1.5"
            JVM_OPTION="-Djavax.net.ssl.trustStore=/etc/pki/ca-trust/extracted/java/cacerts"
            SBT_OPTION="-Dsbt.log.noformat=true"
            SBT_ACTION="clean assembly"

            sh "ls -la"


        }else{

            BUILD_DIR="build/libs"
            GRADLE_TASK="clean install"

        }
    }

    stage("Build AMI") {

        userInput = input(
            id: 'Proceed1', message: 'AMI_VERSION is "${AMI_VERSION}" : Do you want to use this version?', parameters: [
            [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Please confirm you agree with this']
         ])

        println "${userInput}"

        env.outfile = "output.out"

        def returnString = ""

        println "${AMI_VERSION}"
        AMI_VERSION++
        println "${AMI_VERSION}"
        AMI_VERSION = AMI_VERSION + 1
        println "${AMI_VERSION++}"
        println "${AMI_VERSION}"

        sh '''
            echo $JENKINS_HOME
            ls -la
            pwd
            echo $outfile
        '''

        def CurrentDir = sh(script:'pwd > $outfile', returnStdout:true).trim()

        println "${CurrentDir}"


        println "${returnString}"
        def scout = readFile(outfile)

        println "${scout}"

        println "call check"

        check()

        println "end check"

    }

    stage("after build ami"){

        env.TESTVAL = "ttt"

        sh '''
            echo $TESTVAL
           export TESTVAL=test
           echo $TESTVAL
        '''

        println "env start"

        println "${env.TESTVAL}"

        println "env end"

        AFTER="${env.TESTVAL}"

        println "${TESTVAL}"

        println "end end"

    }
}

def check() {

    println "in check"

    println "${AMI_VERSION}"
    println "test"

    int VERSION = 0
    def exist = true

    while(exist == true) {

        println "${VERSION}"
        if(VERSION == 3){
            exist = false
        }else{
            VERSION = VERSION + 1
        }
    }

    println "${VERSION}"
}


