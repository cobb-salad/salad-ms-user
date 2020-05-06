import groovy.json.JsonSlurper

AMI_VERSION = 0


node {
    stage('Parameter Check'){

        TAG = "${params.TAG}"
        BUILD_TYPE = "${params.BUILD_TYPE}"
        SCALA_VERSION = "${params.SCALA_VERSION}"
        ASSEMBLY = "${params.ASSEMBLY}"
        SERVICE = "${params.SERVICE}"
        AMI_VERSION = "${params.AMI_VERSION}"
        AUTO_INCREMENT_AMI_VERSION = "${params.AUTO_INCREMENT_AMI_VERSION}"
        RUN_TASK = "${params.RUN_TASK}"

        println "${TAG}"
        println "${BUILD_TYPE}"
        println "${SCALA_VERSION}"
        println "${ASSEMBLY}"
        println "${SERVICE}"
        println "${AUTO_INCREMENT_AMI_VERSION}"
        println "${RUN_TASK}"

        taskSize = RUN_TASK.contains("chef");

        println "${taskSize}"

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

        if(AMI_VERSION > 0){
            println "AMI_VERSION is greater than 0"
        }

        env.AMI_VERSION="${AMI_VERSION}"

//         userInput = input(
//             id: 'Proceed1', message: 'AMI_VERSION is ${env.AMI_VERSION} : Do you want to use this version?', parameters: [
//             [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Please confirm you agree with this']
//          ])
//
//         println "${userInput}"



    }

    stage("after build ami"){
        env.TESTVAL2="ttt"
        env.AMI_ID="ami-123456789"
        env.ENVIRONMENT="qa"

        ENVIRON = "${env.ENVIRONMENT}"

        sh """#!/bin/bash
            set -x
            touch test2
            echo '\"ami-test\"' > test2

            touch change.sh
            chmod +x change.sh
            ls -al

            echo "#!/bin/bash" > change.sh
            cat change.sh
            #echo "sed -i \'s/ami-[^\"]*/ami-123456789/g\' test2" >> change.sh
            echo "sed -i \'s/\\bami-[^\\"]*/$AMI_ID/g\' test2" >> change.sh
            #sed -i 's/[^\"]*/'"$TESTVAL2"'/g' test2

            cat change.sh

            ./change.sh

            cat test2


            set +x
        """

//         apply_chef()

        jsontest()
    }

}

def jsontest(){

    ASGINFO=readFile("/var/lib/jenkins/jsontmp")
    def jsonSlurper = new JsonSlurper()
    def ASGINFOObj = jsonSlurper.parseText(ASGINFO)
//     def ASGINFOObj = readJSON text: ASGINFO

    MINSIZE = ASGINFOObj.get('MinSize')
    MAXSIZE = ASGINFOObj.get('MaxSize')
    ONDEMANDCAPACITY = ASGINFOObj.get('MixedInstancesPolicy').get('InstancesDistribution').get('OnDemandBaseCapacity')
    ONDEMANDRATIO = ASGINFOObj.get('MixedInstancesPolicy').get('InstancesDistribution').get('OnDemandPercentageAboveBaseCapacity')

    ONDEMANDCAPACITY = 3
    ONDEMANDRATIO = 0
    OPTIONARGS = ""

    CALCULATEDMINSIZE = (MINSIZE * 2) * ((ONDEMANDCAPACITY / MINSIZE) + 1) + 1

    CALCULATEDMINSIZE = ((int) CALCULATEDMINSIZE * 100)/100

    CALCULATEDONDEMANDRATIO = (int)((ONDEMANDCAPACITY/MINSIZE) *100)

//     if(ONDEMANDCAPACITY != 0){
//         OPTIONARGS = "--max-size \$CALCULATEDMAXSIZE --mixed-instances-policy \'{\"InstancesDistribution\":{\"OnDemandPercentageAboveBaseCapacity\":\$CALCULATEDONDEMANDRATIO}}\'"
//     }

    println "${CALCULATEDONDEMANDRATIO}"
    println "${OPTIONARGS}"

    env.OPTIONARGS = "${OPTIONARGS}"

}


def apply_chef(){

    println "${ENVIRON}"

    regex="^[qa|prod].*[1-2]\$"
    if((env.CHEF_ENVIRONMENT =~ regex).matches()){
        ATTRIBUTE="[\"platform-microservice\"][\"$SERVICE\"][\"artifact\"][\"version\"]"
    }else{
        ATTRIBUTE="[\"b2c-microservice\"][\"$SERVICE\"][\"artifact\"][\"version\"]"
    }


    println "${ATTRIBUTE}"

    sh '''#!/bin/bash
        set -x
        ./sun-ms-api-${ENVIRONMENT}_version = \"test\"
        set +x
    '''

    MIN=sh "echo test"

    println "${MIN}"

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


