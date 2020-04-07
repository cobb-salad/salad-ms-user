node {

    stage('Parameter Check'){

        TAG = "${params.TAG}"
        BUILD_TYPE = "${params.BUILD_TYPE}"
        SCALA_VERSION = "${params.SCALA_VERSION}"
        ASSEMBLY = "${params.ASSEMBLY}"
        SERVICE = "${params.SERVICE}"


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
                throw new Exception("There is not TAG")
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
                            userRemoteConfigs                : [[url: 'git@github.com:TheWeatherCompany/sun-ms-${SERVICE}']]
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


        }else{

            BUILD_DIR="build/libs"
            GRADLE_TASK="clean install"

        }
    }
}